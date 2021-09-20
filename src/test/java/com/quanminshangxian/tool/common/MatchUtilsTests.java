package com.quanminshangxian.tool.common;

import org.junit.Assert;
import org.junit.Test;

public class MatchUtilsTests {

    @Test
    public void isMatch() {
        boolean match = MatchUtils.isMatch("[0-9]*", "123456");
        Assert.assertTrue(match);

        boolean match1 = MatchUtils.isMatch("[0-9]*", "123456A");
        Assert.assertTrue(!match1);
    }

    @Test
    public void isNumber() {
        boolean bigCharAndNumComb = MatchUtils.isNumeric("123456");
        Assert.assertTrue(bigCharAndNumComb);

        boolean number = MatchUtils.isNumeric("1234S");
        Assert.assertTrue(!number);
    }

    @Test
    public void isBigCharComb() {
        boolean bigCharAndNumComb = MatchUtils.isBigCharComb("AAAA");
        Assert.assertTrue(bigCharAndNumComb);

        boolean aaaAa = MatchUtils.isBigCharComb("AAAAa");
        Assert.assertTrue(!aaaAa);

        boolean AAAA4 = MatchUtils.isBigCharComb("AAAA4");
        Assert.assertTrue(!AAAA4);
    }

    @Test
    public void isSmartCharComb() {
        boolean bigCharAndNumComb = MatchUtils.isSmartCharComb("aaaa");
        Assert.assertTrue(bigCharAndNumComb);

        boolean aaaaA = MatchUtils.isSmartCharComb("aaaaA");
        Assert.assertTrue(!aaaaA);

        boolean aaaa4 = MatchUtils.isSmartCharComb("aaaa4");
        Assert.assertTrue(!aaaa4);
    }

    @Test
    public void isBigCharAndNumComb() {
        boolean bigCharAndNumComb = MatchUtils.isBigCharAndNumComb("A444");
        Assert.assertTrue(bigCharAndNumComb);

        boolean a444 = MatchUtils.isBigCharAndNumComb("a444");
        Assert.assertTrue(!a444);

        boolean AAa = MatchUtils.isBigCharAndNumComb("AAa");
        Assert.assertTrue(!AAa);
    }

    @Test
    public void isSmartCharAndNumComb() {
        boolean bigCharAndNumComb = MatchUtils.isSmartCharAndNumComb("a444");
        Assert.assertTrue(bigCharAndNumComb);

        boolean A444 = MatchUtils.isSmartCharAndNumComb("A444");
        Assert.assertTrue(!A444);


        boolean AAa = MatchUtils.isBigCharAndNumComb("AAa");
        Assert.assertTrue(!AAa);
    }

    @Test
    public void isCharAndNumComb() {
        boolean bigCharAndNumComb = MatchUtils.isCharAndNumComb("a4A44");
        Assert.assertTrue(bigCharAndNumComb);

        boolean A = MatchUtils.isCharAndNumComb("A");
        Assert.assertTrue(!A);

        boolean a = MatchUtils.isCharAndNumComb("Aa");
        Assert.assertTrue(!a);
    }

}
