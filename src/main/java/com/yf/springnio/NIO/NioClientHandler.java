package com.yf.springnio.NIO;

/**
 * @author: yaofeng
 * @create:2019-04-29-10:15
 **/

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * 客户端线程  抓们接受服务器发来信息
 */
public class NioClientHandler implements Runnable{
    private Selector selector;

    public NioClientHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
        //循环等待新接入的对象
        while (true){
            //获取可用的channel数量
            int selectCount = selector.select();

            if (selectCount==0) continue;

            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                //移除Set中当前的selectionKey
                iterator.remove();

                //业务处理
                //可读事件
                if (selectionKey.isReadable()){
                    readHandler(selectionKey,selector);
                }
                //.....
            }
        }
        }catch (Exception e){

        }
    }

    //可读事件
    private void readHandler(SelectionKey selectionKey,Selector selector)throws IOException {
        //从selectionKey中获取已经就绪的channel
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        //创建一个Buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        //循环读取服务器发来的数据
        String response = "";
        while (channel.read(byteBuffer)>0){
            //将当前的byteBuffer是写入的状态 需要翻转成读模式
            byteBuffer.flip();
            //读取内容
            response += Charset.forName("UTF-8").decode(byteBuffer);
        }
        //将channel再次注册到selector上
        channel.register(selector,SelectionKey.OP_READ);
        //将客户端发来的信息打印到本地
        if (response.length()>0) {
            System.out.println(response);
        }

    }
}
