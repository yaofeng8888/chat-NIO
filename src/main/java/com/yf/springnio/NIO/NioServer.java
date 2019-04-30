package com.yf.springnio.NIO;

import org.apache.logging.log4j.util.Chars;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * @author: yaofeng
 * @create:2019-04-29-09:38
 **/
public class NioServer {
    /**
     * 启动服务器
     */
    public void start() throws Exception {
        //创建一个selector
        Selector selector = Selector.open();
        //创建一个ServerSocketChannel 创建channel通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //给channel绑定端口
        serverSocketChannel.bind(new InetSocketAddress(8000));
        //将channel设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        //将channel注册到selector上 监听连接事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动");
        //循环等待新接入的对象
        while (true) {
            //获取可用的channel数量
            int selectCount = selector.select();

            if (selectCount == 0) continue;

            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                //移除Set中当前的selectionKey
                iterator.remove();

                //业务处理
                //接入事件
                if (selectionKey.isAcceptable()) {
                    System.out.println("accept");
                    appecptHandler(serverSocketChannel, selector);
                }
                //可读事件
                if (selectionKey.isReadable()) {
                    try {
                        readHandler(selectionKey, selector);
                    }catch (IOException e){ //处理当客户端异常关闭 服务器任然保持read连接，导致远程主机强迫关闭了一个现有的连接。
                        serverSocketChannel.socket().close();
                        serverSocketChannel.close();
                        selectionKey.cancel();
                    }
                }
                //.....
            }
        }
    }

    public static void main(String[] strings) throws Exception {
        new NioServer().start();
    }

    //接入事件
    private void appecptHandler(ServerSocketChannel serverSocketChannel, Selector selector) throws Exception {
        //创建一个socketChannel
        SocketChannel socketChannel = serverSocketChannel.accept();
        //设置为非阻塞模式
        socketChannel.configureBlocking(false);
        //将channel注册到selector上
        socketChannel.register(selector, SelectionKey.OP_READ);
        //回复客户端提示信息
        socketChannel.write(Charset.forName("UTF-8").encode("你与聊天室其他人不是好友关系 注意个人隐私安全"));
    }

    //可读事件
    private void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {
        //从selectionKey中获取已经就绪的channel
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        //创建一个Buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        //循环读取客户端发来的数据
        String request = "";
        while (channel.read(byteBuffer) > 0) {
            //将当前的byteBuffer是写入的状态 需要翻转成读模式
            byteBuffer.flip();
            //读取内容
            request += Charset.forName("UTF-8").decode(byteBuffer);
        }
        //将channel再次注册到selector上
        channel.register(selector, SelectionKey.OP_READ);
        //将客户端发来的信息广播给其他客户端
        if (request.length() > 0) {
            System.out.println("::" + request);
            broadCast(selector, channel, request);
        }

    }

    private void broadCast(Selector selector, SocketChannel socketChannel, String request) {
        //获取已经接入的channel（客户端）
        Set<SelectionKey> keys = selector.keys();
        keys.forEach(selectionKey -> {
            Channel channel = selectionKey.channel();
            //剔除发消息的客户端

            if (channel instanceof SocketChannel && channel != socketChannel) {
                try {
                    ((SocketChannel) channel).write(Charset.forName("UTF-8").encode(request));
                } catch (IOException e) {
                }
            }


        });
        //循环向所有channe广播信息
    }
}
