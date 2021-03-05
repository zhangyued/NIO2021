package com.zy.nio;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

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
public class TestNonBlockingNIO {

    //客户端（非阻塞）
    @Test
    public void client() throws Exception{
        //获取通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));
        //切换成非阻塞模式
        socketChannel.configureBlocking(false);
        //分配缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.put((new Date().toString()).getBytes());
        buf.flip();
        socketChannel.write(buf);
        buf.clear();
        //关闭通道
        socketChannel.close();
    }


    //服务端（非阻塞）
    @Test
    public void server() throws Exception{
        //获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //切换非阻塞模式
        serverSocketChannel.configureBlocking(false);
        //绑定连接
        serverSocketChannel.bind(new InetSocketAddress(9898));
        //获取选择器
        Selector selector = Selector.open();
        //将通道注册到选择器上,指定监听接收事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //轮询获取选择器上已经准备就绪的事件
        while(selector.select() > 0){
            //获取当前选择器中所有注册的选择键（已就绪的监听事件）
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            //迭代
            while(iterator.hasNext()){
                //获取准备就绪的事件
                SelectionKey sk = iterator.next();
                //判断具体是什么事件准备就绪
                if(sk.isAcceptable()){
                    //若接收就绪，获取客户端通道
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    //切换非阻塞模式
                    socketChannel.configureBlocking(false);
                    //将该通道注册到选择器上
                    socketChannel.register(selector,SelectionKey.OP_READ);
                }else if (sk.isReadable()){
                    //获取当前选择器上读就绪状态的通道
                    SocketChannel socketChannel = (SocketChannel) sk.channel();
                    //读取数据
                    ByteBuffer buf = ByteBuffer.allocate(1024);
                    int len = 0;
                    while((len = socketChannel.read(buf)) != -1){
                        buf.flip();
                        System.out.println(new String(buf.array(),0,len));
                        buf.clear();
                    }
                }
                //取消选择键
                iterator.remove();
            }
        }
    }

    @Test
    public void client2() throws Exception{
        DatagramChannel dc = DatagramChannel.open();
        dc.configureBlocking(false);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            String str = scanner.next();
            buf.put((new Date().toString() + ":\n" + str).getBytes());
            buf.flip();
            dc.send(buf,new InetSocketAddress("127.0.0.1",9898));
            buf.clear();
        }
        dc.close();

    }

    @Test
    public void server2() throws Exception{
        DatagramChannel dc = DatagramChannel.open();
        dc.configureBlocking(false);
        dc.bind(new InetSocketAddress(9898));
        Selector selector = Selector.open();
        dc.register(selector,SelectionKey.OP_READ);
        while(selector.select() > 0){
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey sk = iterator.next();
                if(sk.isReadable()){
                    ByteBuffer buf = ByteBuffer.allocate(1024);
                    dc.receive(buf);
                    buf.flip();
                    System.out.println(new String(buf.array(),0,buf.limit()));
                    buf.clear();
                }
            }
            iterator.remove();
        }
    }
}
