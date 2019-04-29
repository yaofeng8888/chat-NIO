package com.yf.springnio.NIO;

/**
 * @author: yaofeng
 * @create:2019-04-29-10:53
 **/
public class NioClientA {
    public static void main(String[] s)throws Exception{
        new NioClient().start("clientA");
    }
}
