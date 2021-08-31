package com.qm.tool.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期处理工具类
 */
public class DateUtils {

    /**
     * 获取当前的日期
     *
     * @return
     */
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 日期转字符串 , 格式 yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String dateToYMdHms(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return format.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 日期转字符串 , 格式 yyyy-MM-dd HH:mm
     *
     * @param date
     * @return
     */
    public static String dateToYMdHm(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            return format.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 日期转字符串 , 格式 yyyy-MM-dd
     *
     * @param date
     * @return
     */
    public static String dateToYMd(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return format.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 字符串转日期，格式 yyyy-MM-dd HH:mm:ss
     *
     * @param str
     * @return
     */
    public static Date yMdHmsToDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 字符串转日期，格式 yyyy-MM-dd HH:mm
     *
     * @param str
     * @return
     */
    public static Date yMdHmToDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            return format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 字符串转日期，格式 yyyy-MM-dd
     *
     * @param str
     * @return
     */
    public static Date yMdToDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 是否同一年
     *
     * @param d1
     * @param d2
     * @return
     */
    public static boolean isSameYear(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        int y1 = c1.get(Calendar.YEAR);

        int y2 = c2.get(Calendar.YEAR);

        if (y1 == y2) {
            return true;
        }
        return false;
    }

    /**
     * 是否同一月
     *
     * @param d1
     * @param d2
     * @return
     */
    public static boolean isSameMonth(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        int y1 = c1.get(Calendar.YEAR);
        int m1 = c1.get(Calendar.MONTH);

        int y2 = c2.get(Calendar.YEAR);
        int m2 = c2.get(Calendar.MONTH);

        if (y1 == y2 && m1 == m2) {
            return true;
        }
        return false;
    }

    /**
     * 是否同一天
     *
     * @param d1
     * @param d2
     * @return
     */
    public static boolean isSameDay(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        int y1 = c1.get(Calendar.YEAR);
        int m1 = c1.get(Calendar.MONTH);
        int day1 = c1.get(Calendar.DAY_OF_MONTH);

        int y2 = c2.get(Calendar.YEAR);
        int m2 = c2.get(Calendar.MONTH);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);

        if (y1 == y2 && m1 == m2 && day1 == day2) {
            return true;
        }
        return false;
    }

    /**
     * 日期追加年
     *
     * @param date
     * @param yearNum
     */
    public static Date addYear(Date date, int yearNum) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR,yearNum);
        return cal.getTime();
    }

    /**
     * 日期追加月
     *
     * @param date
     * @param monthNum
     */
    public static Date addMonth(Date date, int monthNum) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH,monthNum);
        return cal.getTime();
    }

    /**
     * 日期追加天
     *
     * @param date
     * @param dayNum
     */
    public static Date addDay(Date date, int dayNum) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE,dayNum);
        return cal.getTime();
    }

    /**
     * 日期追加小时
     *
     * @param date
     * @param hourNum
     */
    public static Date addHour(Date date, int hourNum) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY,hourNum);
        return cal.getTime();
    }

    /**
     * 日期追加分
     *
     * @param date
     * @param minuteNum
     */
    public static Date addMinute(Date date, int minuteNum) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE,minuteNum);
        return cal.getTime();
    }

    /**
     * 日期追加秒
     *
     * @param date
     * @param secondNum
     */
    public static Date addSecond(Date date, int secondNum) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND,secondNum);
        return cal.getTime();
    }

}
