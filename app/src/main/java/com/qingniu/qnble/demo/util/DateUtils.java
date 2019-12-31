package com.qingniu.qnble.demo.util;


import android.content.Context;

import com.qingniu.qnble.demo.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 *
 * @author lufoz
 */
public class DateUtils {

    public static final String FORMAT_SHORT = "yyyy-MM-dd";

    /**
     * 获取String型系统日期 yyyy-MM-dd
     *
     * @return String格式时间
     */
    public static String currentDate() {
        return dateToString(new Date());
    }

    public static Date getCurrentDate() {
        return stringToDate(currentDate());
    }

    /**
     * 字符串转日期 yyyy-MM-dd
     *
     * @param dateStr
     * @return
     */
    public static Date stringToDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_SHORT);
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 日期转字符串，yyyy-MM-dd
     *
     * @param date
     * @return
     */
    public static String dateToString(Date date) {
        return dateToString(date, FORMAT_SHORT);
    }

    /**
     * 日期转字符串，针对任意格式
     *
     * @param date
     * @param format
     * @return
     */
    public static String dateToString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static int getAge(Date birthday) {
        if (birthday == null) {
            return 0;
        }
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        int curYear = cal.get(Calendar.YEAR);
        cal.setTime(birthday);
        int birthYear = cal.get(Calendar.YEAR);
        return curYear - birthYear;
    }

    public static String getBirthdayString(Date birthday, Context context) {
        return birthday == null ? "" : DateUtils.dateToString(birthday,
                context.getResources().getString(R.string.date_format_day));
    }


    public static Date getDate(int year, int month, int day) {
        return stringToDate(year + "-" + NumberUtils.formatIntegerTo2(month) + "-"
                + NumberUtils.formatIntegerTo2(day));
    }

}
