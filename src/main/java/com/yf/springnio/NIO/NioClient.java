package com.yf.springnio.NIO;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * @author: yaofeng
 * @create:2019-04-29-09:39
 **/
public class NioClient {
    //启动客户端方法
    public void start(String name)throws Exception{
        //连接服务器
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8000));

        //接收服务器的数据  打开一个新线程 专门负责接收服务器发送的数据
        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        Thread thread = new Thread(new NioClientHandler(selector));
        thread.start();

        //向服务器发送数据
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()){
            String string = scanner.nextLine();
            if (string!=null&&string.length()>0){
                socketChannel.write(Charset.forName("UTF-8").encode(name+":"+ string));
            }
        }

    }

    public static void main(String[] strings)throws Exception{
      //  new NioClient().start(name);
    }
}
