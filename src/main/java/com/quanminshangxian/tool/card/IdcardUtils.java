package com.quanminshangxian.tool.card;

import com.quanminshangxian.tool.core.MatchUtils;
import com.quanminshangxian.tool.date.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdcardUtils {

    /**
     * 身份证有效
     */
    public static final String VALIDITY = "0";
    /**
     * 位数不足
     */
    public static final String LACKDIGITS = "身份证号码长度应该为15位或18位。";
    /**
     * 最后一位应为数字
     */
    public static final String LASTOFNUMBER = "身份证15位号码都应为数字 ; 18位号码除最后一位外，都应为数字。";
    /**
     * 出生日期无效
     */
    public static final String INVALIDBIRTH = "身份证出生日期无效。";
    /**
     * 生日不在有效范围
     */
    public static final String INVALIDSCOPE = "身份证生日不在有效范围。";
    /**
     * 月份无效
     */
    public static final String INVALIDMONTH = "身份证月份无效";
    /**
     * 日期无效
     */
    public static final String INVALIDDAY = "身份证日期无效";
    /**
     * 身份证地区编码错误
     */
    public static final String CODINGERROR = "身份证地区编码错误。";
    /**
     * 身份证校验码无效
     */
    public static final String INVALIDCALIBRATION = "身份证校验码无效，不是合法的身份证号码";

    /**
     * 是否合法身份证
     */
    public static boolean isValid(String idCard) throws Exception {
        if (idCard == null) {
            throw new RuntimeException("idCard not null");
        }
        int len = idCard.trim().length();
        if (len != 15 && len != 18) {
            throw new RuntimeException("idCard must 15 or 18 digit");
        }
        String s = IDCardValidate(idCard);
        return "0".equals(s);
    }

    /**
     * 15位转18位
     */
    public static String convert15To18(String idCard) {
        if (idCard == null || idCard.trim().length() != 15) {
            throw new RuntimeException("idCard digit error");
        }
        return get18Ic(idCard);
    }

    /**
     * 获取生日
     */
    public static String getBirth(String idCard) {
        if (idCard == null) {
            throw new RuntimeException("idCard not null");
        }
        int len = idCard.trim().length();
        if (len != 15 && len != 18) {
            throw new RuntimeException("idCard must 15 or 18 digit");
        }
        if (len == 15) {
            idCard = convert15To18(idCard);
        }
        StringBuffer sb = new StringBuffer();
        String strYear = idCard.substring(6, 10);// 年份
        String strMonth = idCard.substring(10, 12);// 月份
        String strDay = idCard.substring(12, 14);// 日期
        sb.append(strYear);
        sb.append("-");
        sb.append(strMonth);
        sb.append("-");
        sb.append(strDay);
        return sb.toString();
    }

    /**
     * 获取年龄
     */
    public static int getAge(String idCard) {
        if (idCard == null) {
            throw new RuntimeException("idCard not null");
        }
        int len = idCard.trim().length();
        if (len != 15 && len != 18) {
            throw new RuntimeException("idCard must 15 or 18 digit");
        }
        if (len == 15) {
            idCard = convert15To18(idCard);
        }
        // 年份
        String birth = idCard.substring(6, 10);
        int age = 0;
        Date date = DateUtils.getNowDate();
        String format = DateUtils.dateToYMdHms(date);
        String substring = format.substring(0, 4);
        Integer age1 = Integer.valueOf(birth);
        Integer age2 = Integer.valueOf(substring);
        if (age1 < 2000) {
            age = 2000 - age1 + age2 - 2000;
        } else if (age1 > 2000) {
            age = age1 - age2;
        }
        return age;
    }

    /**
     * 检验身份证号码是否符合规范
     *
     * @param IDStr 身份证号码
     * @return 错误信息或成功信息
     */
    private static String IDCardValidate(String IDStr) throws Exception {
        String tipInfo = VALIDITY;// 记录错误信息
        String Ai = "";
        // 判断号码的长度 15位或18位
        if (null != IDStr && IDStr.length() != 15 && IDStr.length() != 18) {
            tipInfo = LACKDIGITS;
            return tipInfo;
        }
        // 18位身份证前17位位数字，如果是15位的身份证则所有号码都为数字
        if (null != IDStr) {
            if (IDStr.length() == 18) {
                Ai = IDStr.substring(0, 17);
            } else {
                Ai = IDStr.substring(0, 6) + "19" + IDStr.substring(6, 15);
            }
            if (!MatchUtils.isNumeric(Ai)) {
                tipInfo = LASTOFNUMBER;
                return tipInfo;
            }
        }

        // 判断出生年月是否有效
        String strYear = Ai.substring(6, 10);// 年份
        String strMonth = Ai.substring(10, 12);// 月份
        String strDay = Ai.substring(12, 14);// 日期
        if (!isDate(strYear + "-" + strMonth + "-" + strDay)) {
            tipInfo = INVALIDBIRTH;
            return tipInfo;
        }
        GregorianCalendar gc = new GregorianCalendar();
        try {
            if ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) > 150
                    || (gc.getTime().getTime() - DateUtils.yMdToDate(strYear + "-" + strMonth + "-" + strDay).getTime()) < 0) {
                tipInfo = INVALIDSCOPE;
                return tipInfo;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (Integer.parseInt(strMonth) > 12 || Integer.parseInt(strMonth) == 0) {
            tipInfo = INVALIDMONTH;
            return tipInfo;
        }
        if (Integer.parseInt(strDay) > 31 || Integer.parseInt(strDay) == 0) {
            tipInfo = INVALIDDAY;
            return tipInfo;
        }
        // 判断地区码是否有效
        Hashtable<String, String> areacode = GetAreaCode();
        // 如果身份证前两位的地区码不在Hashtable，则地区码有误
        if (areacode.get(Ai.substring(0, 2)) == null) {
            tipInfo = CODINGERROR;
            return tipInfo;
        }
        if (!isVerifyCode(Ai, IDStr)) {
            tipInfo = INVALIDCALIBRATION;
            return tipInfo;
        }
        return tipInfo;
    }

    /*
     * 判断第18位校验码是否正确 第18位校验码的计算方式：
     * 1. 对前17位数字本体码加权求和 公式为：S = Sum(Ai * Wi), i =
     * 0, ... , 16 其中Ai表示第i个位置上的身份证号码数字值，Wi表示第i位置上的加权因子，其各位对应的值依次为： 7 9 10 5 8 4
     * 2 1 6 3 7 9 10 5 8 4 2
     * 2. 用11对计算结果取模 Y = mod(S, 11)
     * 3. 根据模的值得到对应的校验码
     * 对应关系为： Y值： 0 1 2 3 4 5 6 7 8 9 10 校验码： 1 0 X 9 8 7 6 5 4 3 2
     */
    private static boolean isVerifyCode(String Ai, String IDStr) {
        String[] VarifyCode = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
        String[] Wi = {"7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7", "9", "10", "5", "8", "4", "2"};
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum = sum + Integer.parseInt(String.valueOf(Ai.charAt(i))) * Integer.parseInt(Wi[i]);
        }
        int modValue = sum % 11;
        String strVerifyCode = VarifyCode[modValue];
        Ai = Ai + strVerifyCode;
        if (IDStr.length() == 18) {
            return Ai.equals(IDStr);
        }
        return true;
    }

    /**
     * 将所有地址编码保存在一个Hashtable中
     *
     * @return Hashtable 对象
     */
    private static Hashtable<String, String> GetAreaCode() {
        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        hashtable.put("11", "北京");
        hashtable.put("12", "天津");
        hashtable.put("13", "河北");
        hashtable.put("14", "山西");
        hashtable.put("15", "内蒙古");
        hashtable.put("21", "辽宁");
        hashtable.put("22", "吉林");
        hashtable.put("23", "黑龙江");
        hashtable.put("31", "上海");
        hashtable.put("32", "江苏");
        hashtable.put("33", "浙江");
        hashtable.put("34", "安徽");
        hashtable.put("35", "福建");
        hashtable.put("36", "江西");
        hashtable.put("37", "山东");
        hashtable.put("41", "河南");
        hashtable.put("42", "湖北");
        hashtable.put("43", "湖南");
        hashtable.put("44", "广东");
        hashtable.put("45", "广西");
        hashtable.put("46", "海南");
        hashtable.put("50", "重庆");
        hashtable.put("51", "四川");
        hashtable.put("52", "贵州");
        hashtable.put("53", "云南");
        hashtable.put("54", "西藏");
        hashtable.put("61", "陕西");
        hashtable.put("62", "甘肃");
        hashtable.put("63", "青海");
        hashtable.put("64", "宁夏");
        hashtable.put("65", "新疆");
        hashtable.put("71", "台湾");
        hashtable.put("81", "香港");
        hashtable.put("82", "澳门");
        hashtable.put("91", "国外");
        return hashtable;
    }

    /**
     * 功能：判断字符串出生日期是否符合正则表达式：包括年月日，闰年、平年和每月31天、30天和闰月的28天或者29天
     *
     * @return true, 符合; false, 不符合。
     */
    public static boolean isDate(String strDate) {
        Pattern pattern = Pattern.compile(
                "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))?$");
        Matcher m = pattern.matcher(strDate);
        if (m.matches()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 将15位身份证号转化为18位返回，非15位身份证号原值返回
     *
     * @param identityCard
     * @return
     */
    public static String get18Ic(String identityCard) {
        String retId = "";
        String id17 = "";
        int sum = 0;
        int y = 0;
        // 定义数组存放加权因子（weight factor）
        int[] wf = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        // 定义数组存放校验码（check code）
        String[] cc = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
        if (identityCard.length() != 15) {
            return identityCard;
        }
        // 加上两位年19
        id17 = identityCard.substring(0, 6) + "19" + identityCard.substring(6);
        // 十七位数字本体码加权求和
        for (int i = 0; i < 17; i++) {
            sum = sum + Integer.valueOf(id17.substring(i, i + 1)) * wf[i];
        }
        // 计算模
        y = sum % 11;
        // 通过模得到对应的校验码 cc[y]
        retId = id17 + cc[y];
        return retId;
    }
}
