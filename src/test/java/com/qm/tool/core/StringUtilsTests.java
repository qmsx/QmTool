package com.qm.tool.core;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTests {

    @Test
    public void equals(){
        boolean equals = StringUtils.equals("1", "1");
        Assert.assertTrue(equals);

        boolean a = StringUtils.equals("1", "2");
        Assert.assertTrue(!a);

    }

    @Test
    public void isEmpty(){
        boolean empty = StringUtils.isEmpty("");
        Assert.assertTrue(empty);

        boolean a = StringUtils.isEmpty("a");
        Assert.assertTrue(!a);
    }
}
