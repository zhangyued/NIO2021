package com.zy.nio;

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * 一.缓冲区（Buffer）：在Java NIO中负责数据的存取，缓冲区就是数组。用于存储不同数据类型的数据
 * 根据数据类型不同（除了boolean），提供了相应类型的缓冲区：
 * ByteBuffer（最常用）
 * CharBuffer
 * ShortBuffer
 * IntBuffer
 * LongBuffer
 * FloatBuffer
 * DoubleBuffer
 *
 * 上述缓冲区的管理方式几乎一致，通过allocate()获取缓冲区
 *
 * 二.缓冲区存取数据的两个核心方法
 * put():存入数据到缓冲区中
 * get():获取缓冲区中的数据
 *
 * 三.缓冲区中的四个核心属性:
 * capacity: 容量，表示缓冲区中最大存储数据的容量，一旦声明不能改变
 * limit: 界限，表示缓冲区中可以操作数据的大小(limit后的数据是不能读写的)
 * position: 位置，表示缓冲区中正在操作数据的位置
 * mark: 标记，用来记录当前position的位置，可以通过reset()恢复到mark的位置
 * 0 <= mark <= position <= limit <= capacity
 *
 * 四.直接缓冲区与非直接缓冲区
 * 非直接缓冲区：通过allocate()方法分配缓冲区，将缓冲区建立在JVM内存中
 * 直接缓冲区：通过allocateDirect()方法分配直接缓冲区，将缓冲区建立在OS物理内存中，可以提高效率
 */
public class TestBuffer {

    @Test
    public void test1(){
        //1.分配一个指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);
        System.out.println("-------allocate--------");
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity());

        //2.利用put方法存入数据到缓冲区里
        String  str = "abcde";
        buf.put(str.getBytes());
        System.out.println("-------put--------");
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity());

        //3.切换读取数据的模式
        buf.flip();
        System.out.println("-------flip--------");
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity());

        //4.切换模式后，通过get方法来读取数据
        byte[] dst = new byte[buf.limit()];
        //读取的数据放入到字节数组中
        buf.get(dst);

        System.out.println("-------get--------");
        System.out.println(new String(dst,0,dst.length));
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity());

        //5.rewind:可重复读数据
        buf.rewind();
        System.out.println("-------rewind--------");
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity());

        //6.清空缓冲区，但是缓冲区的数据依然存在，但是处于被遗忘状态
        buf.clear();

        System.out.println("-------clear--------");
        System.out.println(buf.position());//0
        System.out.println(buf.limit());//1024
        System.out.println(buf.capacity());//1024
        System.out.println("查看缓冲区是否还有数据：" + (char)buf.get());//a
        System.out.println(buf.position());//1
        System.out.println(buf.limit());//1024
        System.out.println(buf.capacity());//1024
        buf.clear();
        /**
         * 再清空  position回归0  倘若接下来没有下面的get，则新put进去的数据会从position为0开始，
         * 若get了数据，因缓冲区数据不会真实清空只是遗忘，而此时明确获取，则position为1，后续加的数据则顺延，
         * 所以当下面get删掉则是fgh，若是打开，则是afgh
         */
        System.out.println("查看缓冲区是否还有数据：" + (char)buf.get());
        String  str2 = "fgh";
        buf.put(str2.getBytes());
        buf.flip();
        byte[] dst2 = new byte[buf.limit()];
        buf.get(dst2);
        System.out.println(new String(dst2,0,dst2.length));
    }

    @Test
    public void test2(){
        String str = "12345";
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.put(str.getBytes());
        buf.flip();
        byte[] dst = new byte[buf.limit()];
        buf.get(dst,0,2);
        System.out.println(new String(dst,0,2));
        System.out.println(buf.position());
        //mark
        buf.mark();
        buf.get(dst,2,2);
        System.out.println(new String(dst,2,2));
        System.out.println(buf.position());
        //reset:恢复到mark的位置
        buf.reset();
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity());
        //判断缓冲区还有没有
        if(buf.hasRemaining()){
            //获取缓冲区可操作的
            System.out.println(buf.remaining());
            byte[] dst2 = new byte[buf.limit()];
            buf.get(dst2,buf.position(),buf.limit()-buf.position());
            String str2 = "";
            str2 = new String(dst2);
            System.out.println("剩余数据" + str2);
            for (byte b: dst2) {
                System.out.println("数组数据" + (char)b);
            }
        }
    }

    @Test
    public void test3(){
        //直接缓冲区
        ByteBuffer buf = ByteBuffer.allocateDirect(1024);
        System.out.println(buf.isDirect());
    }
}
