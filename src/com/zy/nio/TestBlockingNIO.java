package com.zy.nio;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 一.使用NIO完成网络通信的三个核心
 * 1.通道：负责连接
 * java.nio.channels.Channel接口
 *    |--SocketChannel
 *    |--ServerSocketChannel
 *    |--DatagramChannel
 *
 *    |--Pipe.SinkChannel
 *    |--Pipe.SourceChannel
 * 2.缓冲区：负责数据的存取
 * 3.选择器：是SelectableChannel的多路复用器，用于监控SelectableChannel状况
 */
public class TestBlockingNIO {

    /**
     * 阻塞式无反馈客户端
     * @throws Exception
     */
    @Test
    public void client() throws Exception{
        //1.获取通道
        SocketChannel sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1",9898));

        //2.分配缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);

        //3.读取本地文件并发送到服务端
        FileChannel inChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
        while(inChannel.read(buf) != -1){
            buf.flip();
            sChannel.write(buf);
            buf.clear();
        }

        //4.关闭通道
        inChannel.close();
        sChannel.close();
    }

    /**
     * 阻塞式无反馈服务端
     * @throws Exception
     */
    @Test
    public void server() throws Exception{
        //1.获取通道
        ServerSocketChannel ssChannel = ServerSocketChannel.open();

        //2.绑定连接
        ssChannel.bind(new InetSocketAddress(9898));

        //3.获取客户端连接的通道
        SocketChannel socketChannel = ssChannel.accept()
                ;
        //4.分配指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);

        //5.接收客户端的数据并保存到本地
        FileChannel outChannel = FileChannel.open(Paths.get("4.jpg"), StandardOpenOption.WRITE,StandardOpenOption.READ,StandardOpenOption.CREATE_NEW);

        while(socketChannel.read(buf) != -1){
            buf.flip();
            outChannel.write(buf);
            buf.clear();
        }

        //关闭通道
        outChannel.close();
        socketChannel.close();
        ssChannel.close();
    }

    /**
     * 阻塞式有反馈客户端
     * @throws Exception
     */
    @Test
    public void client2() throws Exception {
        SocketChannel sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1",9797));
        ByteBuffer buf = ByteBuffer.allocate(1024);

        FileChannel inChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
        while(inChannel.read(buf) != -1){
            buf.flip();
            sChannel.write(buf);
            buf.clear();
        }
        sChannel.shutdownOutput();
        //接收服务端反馈
        int len = 0;
        while((len = sChannel.read(buf)) != -1){
            buf.flip();
            System.out.println(new String(buf.array(),0,len));
            buf.clear();
        }

        inChannel.close();
        sChannel.close();
    }

    /**
     * 阻塞式有反馈服务端
     * @throws Exception
     */
    @Test
    public void server2() throws Exception{
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        ssChannel.bind(new InetSocketAddress(9797));
        SocketChannel socketChannel = ssChannel.accept();
        ByteBuffer buf = ByteBuffer.allocate(1024);

        FileChannel outChannel = FileChannel.open(Paths.get("5.jpg"), StandardOpenOption.WRITE,StandardOpenOption.READ,StandardOpenOption.CREATE_NEW);
        while(socketChannel.read(buf) != -1){
            buf.flip();
            outChannel.write(buf);
            buf.clear();
        }
        //发送反馈给客户端
        buf.put("服务端数据接收成功".getBytes());
        buf.flip();
        socketChannel.write(buf);

        outChannel.close();
        socketChannel.close();
        ssChannel.close();
    }


}
