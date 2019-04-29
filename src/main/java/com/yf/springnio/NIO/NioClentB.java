package com.yf.springnio.NIO;

/**
 * @author: yaofeng
 * @create:2019-04-29-10:54
 **/
public class NioClentB {
    public static void main(String[] s)throws Exception{
        new NioClient().start("clinetB");
    }
}
