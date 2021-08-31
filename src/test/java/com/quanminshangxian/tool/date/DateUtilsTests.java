package com.quanminshangxian.tool.date;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtilsTests {


    @Test
    public void isSameYear(){
        boolean sameYear = DateUtils.isSameYear(new Date(), new Date(2020));
        Assert.assertTrue(!sameYear);

    }

    @Test
    public void isSameMonth(){
        boolean sameYear = DateUtils.isSameMonth(new Date(), new Date(2020));
        Assert.assertTrue(!sameYear);

    }

    @Test
    public void isSameDay(){
        boolean sameYear = DateUtils.isSameDay(new Date(), new Date(2020));
        Assert.assertTrue(!sameYear);

    }

    @Test
    public void addYear() throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").parse("2021-08-04 18-28-00");
        Date date1 = DateUtils.addYear(date, 1);
        String format2 = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date1);
        Assert.assertEquals("2022-08-04 18-28-00",format2);
    }
    @Test
    public void addMonth() throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").parse("2021-08-04 18-28-00");
        Date date1 = DateUtils.addMonth(date, 1);
        String format2 = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date1);
        Assert.assertEquals("2021-09-04 18-28-00",format2);
    }

    @Test
    public void addDay() throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").parse("2021-08-04 18-28-00");
        Date date1 = DateUtils.addDay(date, 2);
        String format2 = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date1);
        Assert.assertEquals("2021-08-06 18-28-00",format2);
    }

    @Test
    public void addHour() throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").parse("2021-08-04 18-28-00");
        Date date1 = DateUtils.addHour(date, 1);
        String format2 = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date1);
        Assert.assertEquals("2021-08-04 19-28-00",format2);
    }

    @Test
    public void addMinute() throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").parse("2021-08-04 18-28-00");
        Date date1 = DateUtils.addMinute(date, 1);
        String format2 = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date1);
        Assert.assertEquals("2021-08-04 18-29-00",format2);
    }

    @Test
    public void addSecond() throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").parse("2021-08-04 18-28-00");
        Date date1 = DateUtils.addSecond(date, 2);
        String format2 = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date1);
        Assert.assertEquals("2021-08-04 18-28-02",format2);
    }
}
