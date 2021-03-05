package com.zy.nio;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * 一.通道(channel): 用于源节点与目标结点的连接，在Java NIO中负责缓冲区数据的传输，
 * 本身不负责存储数据，因此需要配合缓冲区进行传输
 *
 * 二.通道主要实现类
 * java.nio.channels.Channel 接口
 *      |--FileChannel 专门操作本地文件的
 *      |--SocketChannel 以下都用于网络传输
 *      |--ServerSocketChannel
 *      |--DatagramChannel
 *
 * 三.获取通道
 * 1.Java针对支持通道的类提供getChannel()方法
 * 本地IO：
 * FileInputStream/FileOutputStream
 * RandomAccessFile
 * 网络IO：
 * Socket
 * ServerSocket
 * DatagramSocket
 * 2.在JDK1.7中的NIO.2针对各个通道提供一个静态方法open()
 * 3.在JDK1.7中的NIO.2的Files工具类的newByteChannel()
 *
 * 四.通道之间的数据传输
 * transferFrom()
 * transferTo()
 *
 * 五.分散Scatter与聚集Gather
 * 分散读取：将通道中的数据分散到多个缓冲区中
 * 聚集写入：将多个缓冲区中的数据聚集到通道中
 *
 * 六.字符集：Charset
 * 编码：字符串 -> 字节数组
 * 解码：字节数组 -> 解码
 */
public class TestChannel {

    //1.利用通道完成文件复制(非直接缓冲区)
    @Test
    public void test1() throws Exception{
//        FileInputStream fis = new FileInputStream("1.jpg");
//        FileOutputStream fos = new FileOutputStream("2.jpg");
        long start = System.currentTimeMillis();
        FileInputStream fis = new FileInputStream("d:/1.zip");
        FileOutputStream fos = new FileOutputStream("d:/3.zip");
        //获取通道
        FileChannel inChannel = fis.getChannel();
        FileChannel outChannel = fos.getChannel();

        //分配缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);

        //将inChannel通道中的数据存入缓冲区
        while(inChannel.read(buf) != -1){
            //切换成读取数据模式
            buf.flip();
            //将缓冲区的数据写入outChannel通道
            outChannel.write(buf);
            //清空缓冲区
            buf.clear();
        }
        long end = System.currentTimeMillis();
        System.out.println("耗费时间：" + (end - start));
        //关闭资源
        outChannel.close();
        inChannel.close();
        fos.close();
        fis.close();
    }

    //1.利用通道完成文件复制(直接缓冲区)
    @Test
    public void test2() throws Exception{
        long start = System.currentTimeMillis();
        //获取通道
//        FileChannel inChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
//        FileChannel outChannel = FileChannel.open(Paths.get("3.jpg"), StandardOpenOption.WRITE,StandardOpenOption.READ,StandardOpenOption.CREATE_NEW);

        FileChannel inChannel = FileChannel.open(Paths.get("d:/1.zip"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("d:/2.zip"), StandardOpenOption.WRITE,StandardOpenOption.READ,StandardOpenOption.CREATE_NEW);

        //内存映射文件
        MappedByteBuffer inMappedBuf = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        MappedByteBuffer outMappedBuf = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());

        //直接对缓冲区进行数据的读写操作
        byte[] dst = new byte[inMappedBuf.limit()];
        inMappedBuf.get(dst);
        outMappedBuf.put(dst);
        long end = System.currentTimeMillis();
        System.out.println("耗费时间：" + (end - start));
        //关闭资源
        inChannel.close();
        outChannel.close();
    }

    //通道之间的数据传输(直接缓冲区)
    @Test
    public void test3() throws Exception{
        FileChannel inChannel = FileChannel.open(Paths.get("d:/1.zip"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("d:/4.zip"), StandardOpenOption.WRITE,StandardOpenOption.READ,StandardOpenOption.CREATE_NEW);

        //inChannel.transferTo(0,inChannel.size(),outChannel);
        outChannel.transferFrom(inChannel,0,inChannel.size());
        //关闭资源
        inChannel.close();
        outChannel.close();
    }

    //分散与聚集
    @Test
    public void test4() throws Exception {
        RandomAccessFile raf = new RandomAccessFile("1.txt","rw");
        //1.获取通道
        FileChannel fileChannel = raf.getChannel();

        //分配指定大小的缓冲区
        ByteBuffer buf1 = ByteBuffer.allocate(100);
        ByteBuffer buf2 = ByteBuffer.allocate(1024);

        //3.分散读取
        ByteBuffer[] bufs = {buf1,buf2};
        fileChannel.read(bufs);

        for (ByteBuffer byteBuffer:bufs) {
            byteBuffer.flip();
        }
        System.out.println(new String(bufs[0].array(),0,bufs[0].limit()));
        System.out.println("-----------");
        System.out.println(new String(bufs[1].array(),0,bufs[1].limit()));

        //聚集写入
        RandomAccessFile raf2 = new RandomAccessFile("2.txt","rw");
        FileChannel fileChannel2 = raf2.getChannel();
        fileChannel2.write(bufs);

    }

    //解码编码
    @Test
    public void test5() throws Exception{
        SortedMap<String, Charset> stringCharsetSortedMap = Charset.availableCharsets();
        Set<Map.Entry<String, Charset>> entries = stringCharsetSortedMap.entrySet();
        for (Map.Entry<String, Charset> entry:entries) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }

    }

    @Test
    public void test6() throws Exception{
        Charset cs1 = Charset.forName("GBK");
        //获取编码器与解码器
        CharsetEncoder ce = cs1.newEncoder();
        CharsetDecoder cd = cs1.newDecoder();

        CharBuffer charBuffer = CharBuffer.allocate(1024);
        charBuffer.put("好好学习天天向上");
        charBuffer.flip();

        //编码
        ByteBuffer byteBuffer = ce.encode(charBuffer);

        System.out.println("1-----------");
        System.out.println(byteBuffer.limit());
        System.out.println(byteBuffer.capacity());
        System.out.println(byteBuffer.position());
        //不加以下循环得这样结果
        /**
         1-----------
         16
         16
         0
         2-----------
         16
         16
         0
         3-----------
         0
         16
         0
         解码
         */
        //加循环获取
        /**
         1-----------
         16
         16
         0
         -70
         -61
         -70
         -61
         -47
         -89
         -49
         -80
         -52
         -20
         -52
         -20
         -49
         -14
         -55
         -49
         2-----------
         16
         16
         16
         3-----------
         16
         16
         0
         解码
         好好学习天天向上
         */

        //造成这个的原因是byteBuffer.flip()方法内limit是直接赋值position，
        //而如果当前若是没有操作数据（不读数据），那么操作数据指针依然是0，此时flip则把limit设置成0，及最大可操作数据为0
        //难道非得先有一个读取全部的过程？？
//        for (int i = 0; i < byteBuffer.limit(); i++){
//            System.out.println(byteBuffer.get());
//        }

        System.out.println("2-----------");
        System.out.println(byteBuffer.limit());
        System.out.println(byteBuffer.capacity());
        System.out.println(byteBuffer.position());

        //解码
        byteBuffer.flip();
        System.out.println("3-----------");
        System.out.println(byteBuffer.limit());
        System.out.println(byteBuffer.capacity());
        System.out.println(byteBuffer.position());

        CharBuffer charBuffer1 = cd.decode(byteBuffer);
        System.out.println("解码");
        System.out.println(charBuffer1.toString());
    }

}
