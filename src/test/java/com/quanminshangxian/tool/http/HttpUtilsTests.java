package com.quanminshangxian.tool.http;

import org.junit.Test;

import java.util.HashMap;

public class HttpUtilsTests {


    @Test
    public void sendGet(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("aaa","aaa");
        String s = HttpUtils.get("http://localhost:8095/test/testget", map);
        System.out.println(s);
    }


    @Test
    public void sendPost(){
        String aaa = HttpUtils.doPost("http://localhost:8095/test/testpost", "a");
        System.out.println(aaa);
    }
}
