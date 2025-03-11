package com.adanxing.ad.user.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 日期处理
 */
public class DateUtils {

    private final static Logger logger = LoggerFactory.getLogger(DateUtils.class);
    public static final int DAY_SECONDS = 86400;

    /**
     * 时间格式(yyyy-MM-dd)
     */
    public final static String DATE_PATTERN = "yyyy-MM-dd";

    public final static String DATE_PATTERN_HH = "yyyy-MM-dd HH";

    public final static String DATE_PATTERN_HH_C = "yyyy年MM月dd日HH点";

    public final static String DATE_PATTERN_MM_C = "yyyy年MM月dd日HH点mm分";

    public final static String DATE_PATTERN_MM = "yyyy-MM-dd HH:mm";

    public final static String DATE_PATTERN_MIN = "yyyy-MM-dd HH:mm";

    public final static String YYYYMMDD = "yyyyMMdd";
    public final static String YYYYMMDDHH = "yyyyMMddHH";

    /**
     * 时间格式(yyyy-MM-dd HH:mm:ss)
     */
    public final static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static String format(Date date) {
        return format(date, DATE_PATTERN);
    }

    public static String format(Date date, String pattern) {
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            return df.format(date);
        }
        return null;
    }

    public static Date parse(String timeStr) {
        return parse(timeStr, DATE_PATTERN);
    }

    public static Date parseNew(String timeStr) {
        return parse(timeStr, DATE_TIME_PATTERN);
    }

    public static Date parse(String timeStr, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        try {
            return simpleDateFormat.parse(timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date convertTime(Date date, String pattern) {
        return parse(format(date, pattern), pattern);
    }

    /**
     * 计算距离现在多久，非精确
     *
     * @param date
     * @return
     */
    public static String getTimeBefore(Date date) {
        Date now = new Date();
        long l = now.getTime() - date.getTime();
        long day = l / (24 * 60 * 60 * 1000);
        long hour = (l / (60 * 60 * 1000) - day * 24);
        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        String r = "";
        if (day > 0) {
            r += day + "天";
        } else if (hour > 0) {
            r += hour + "小时";
        } else if (min > 0) {
            r += min + "分";
        } else if (s > 0) {
            r += s + "秒";
        }
        r += "前";
        return r;
    }

    /**
     * 计算距离现在多久，精确
     *
     * @param date
     * @return
     */
    public static String getTimeBeforeAccurate(Date date) {
        Date now = new Date();
        long l = now.getTime() - date.getTime();
        long day = l / (24 * 60 * 60 * 1000);
        long hour = (l / (60 * 60 * 1000) - day * 24);
        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        String r = "";
        if (day > 0) {
            r += day + "天";
        }
        if (hour > 0) {
            r += hour + "小时";
        }
        if (min > 0) {
            r += min + "分";
        }
        if (s > 0) {
            r += s + "秒";
        }
        r += "前";
        return r;
    }

    public static String resetDate(String dateTime, String orignFormat, String destFormat) {
        return DateUtils.format(DateUtils.parse(dateTime, orignFormat), destFormat);
    }

    public static String formatDate(String dateTime) {
        if (dateTime.length() == 13) {
            dateTime = dateTime + ":00:00";
        } else {
            dateTime = dateTime + " 00:00:00";
        }
        ;
        return dateTime;
    }

    public static String getYesterday() {
        Calendar c = Calendar.getInstance();
        int day1 = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day1 - 1);
        return format(c.getTime(), DATE_PATTERN);
    }

    public static Date minusDay(Date date, int day) {
        return plusDay(date, -day);
    }

    public static Date beforeDay(Date date, int days) {
        return plusDay(date, 0 - days);
    }

    public static Date plusDay(Date date, int days) {
        long index = 86400000L * days;
        return new Date(date.getTime() + index);
    }

    public static Date plusHour(Date date, int hour) {
        return new Date(date.getTime() + hour * 3600000);
    }


    public static Date plusOneDay(Date date) {
        return plusDay(date, 1);
    }

    public static Date beforeWeek(){
        LocalDate dateNow = LocalDate.now();
        LocalDate beforeWeek = dateNow.minusWeeks(1);
        Date date = Date.from(beforeWeek.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return date;
    }

    public static Date beforeDay(Date date, long time) {
        return new Date(date.getTime() - time);
    }

    public static Date plusOneHour(Date date) {
        return new Date(date.getTime() + 3600000);
    }

    public static Date plusMin(Date date, int min) {
        return new Date(date.getTime() + min * 60000);
    }

    public static Date plusSecond(Date date, int second) {
        return new Date(date.getTime() + second * 1000);
    }

    public static Date beforeMin(Date date, int min) {
        return new Date(date.getTime() - min * 60000);
    }

    public static Date beforeOneHour(Date date) {
        return new Date(date.getTime() - 3600000);
    }

    public static Date beforeTwoHour(Date date) {
        return new Date(date.getTime() - 7200000);
    }

    public static Date getHourDateByTime(Date logTime) {
        return DateUtils.parse(DateUtils.format(logTime, "yyyyMMddHH"), "yyyyMMddHH");
    }

    public static Date getDayByTime(Date logTime) {
        return DateUtils.parse(DateUtils.format(logTime, "yyyyMMdd"), "yyyyMMdd");
    }

    public static Date getMinuteByTime(Date logTime) {
        return DateUtils.parse(DateUtils.format(logTime, "yyyyMMdd HH:mm"), "yyyyMMdd HH:mm");
    }

    public static String getLastDay(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        Date date = null;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.setTime(date);
        int day = calendar.get(Calendar.DATE);
        //                      此处修改为+1则是获取后一天
        calendar.set(Calendar.DATE, day - 1);

        String lastDay = sdf.format(calendar.getTime());
        return lastDay;
    }

    public static String getMonthFirstDay(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        Date date = null;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String firstday = sdf.format(calendar.getTime());
        return firstday;
    }


    public static Date getMinTime(Date startTime, Date entTime) {
        return startTime.getTime() > entTime.getTime() ? entTime : startTime;
    }

    public static Date getMaxTime(Date startTime, Date entTime) {
        return startTime.getTime() > entTime.getTime() ? startTime : entTime;
    }

    public static String getMonthLastDay(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        Date date = null;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        String lastday = sdf.format(calendar.getTime());
        return lastday;
    }

    public static String getFirstDayOfNextMonth(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, 1);
            return sdf.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getMonthFullDay(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<String> fullDayList = new ArrayList<String>();
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7));
        int day = 1;// 所有月份从1号开始
        Calendar cal = Calendar.getInstance();// 获得当前日期对象
        cal.clear();// 清除信息
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);// 1月从0开始
        cal.set(Calendar.DAY_OF_MONTH, day);
        int count = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int j = 0; j <= (count - 1); ) {
            if (sdf.format(cal.getTime()).equals(getLastDay(year, month)))
                break;
            cal.add(Calendar.DAY_OF_MONTH, j == 0 ? +0 : +1);
            j++;
            fullDayList.add(sdf.format(cal.getTime()));
        }
        return fullDayList;
    }

    public static List<String> findDaysStr(String begintTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dBegin = null;
        Date dEnd = null;
        try {
            dBegin = sdf.parse(begintTime);
            dEnd = sdf.parse(endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<String> daysStrList = new ArrayList<String>();
        daysStrList.add(sdf.format(dBegin));
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(dBegin);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dEnd);
        while (dEnd.after(calBegin.getTime())) {
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
            String dayStr = sdf.format(calBegin.getTime());
            daysStrList.add(dayStr);
        }
        return daysStrList;
    }

    public static List<String> getMonthFullHour(String begintTime, String endTime) {
        List<String> date_list = DateUtils.findDaysStr(begintTime, endTime);
        List<String> date_hour_list = new ArrayList<>();
        for (int i = 0; i < date_list.size(); i++) {
            String everyDate = date_list.get(i);
            for (int j = 0; j < 24; j++) {
                String hour = "";
                if (j < 10) {
                    hour = "0" + j;
                } else {
                    hour = "" + j;
                }
                date_hour_list.add(everyDate + " " + hour);
            }
        }
        return date_hour_list;
    }

    public static String getLastDay(int year, int month) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        return sdf.format(cal.getTime());
    }

    /**
     * 在输入日期上增加（+）月份
     *
     * @param date   输入日期
     * @param imonth 要增加或减少的月分数
     */
    public static Date addMonth(Date date, int imonth) {
        Calendar cd = Calendar.getInstance();

        cd.setTime(date);

        cd.add(Calendar.MONTH, imonth);

        return cd.getTime();
    }

    /**
     * 获取两个时间相差秒
     */
    public static long getTimeDifference(String strTime1, String strTime2) {
        //格式日期格式，在此我用的是"2018-01-24 19:49:50"这种格式
        //可以更改为自己使用的格式，例如：yyyy/MM/dd HH:mm:ss 。。。
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date now = df.parse(strTime1);
            Date date = df.parse(strTime2);
            long l = now.getTime() - date.getTime();       //获取时间差
            long s = l / 1000;
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 在输入日期上减（-）月份
     *
     * @param date  输入日期
     * @param month 要增加或减少的月分数
     */
    public static Date minusMonth(Date date, int month) {
        return DateUtils.addMonth(date, -month);
    }

    public static void main(String[] args) {

        System.out.println(DateUtils.getWeek(DateUtils.beforeDay(new Date(), 2)));
    }

    public static int getWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public static int getCurrentDayOfWeek(Date date) {
        int day = getWeek(date);
        switch (day) {
            case Calendar.MONDAY:
                return 0;
            case Calendar.TUESDAY:
                return 1;
            case Calendar.WEDNESDAY:
                return 2;
            case Calendar.THURSDAY:
                return 3;
            case Calendar.FRIDAY:
                return 4;
            case Calendar.SATURDAY:
                return 5;
            case Calendar.SUNDAY:
                return 6;
            default:
                return day;
        }
    }

    public static long getDay(Date date1, Date date2) {
        return (DateUtils.getDayByTime(date2).getTime() - DateUtils.getDayByTime(date1).getTime()) / 1000 / 3600 / 24;
    }

    public static int getHour(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinute(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MINUTE);
    }

}
