package com.hyunju.jin.movie.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/*
    주로 비슷한 작업을 하는 것 같아 보이는 메서드에 대해 구분하기 편하도록 주석을 달려고 노력했음
    그러나 메서드 목록만 봐도 알 수 있는 매개변수 타입, 리턴 타입을 메서드 설명에 넣은게 어쩌면 내용만 느리는게 아니었을까? 하는 생각이 든다.
*/

/**
    문자열 날짜/시간 <-> Calendar 객체로 변환 하는 메서드가 정의된 클래스
    DB에 저장된 시간을 원하는 형태로 화면에 나타낼때 사용한다.
 */
public class DateFormatUtils {

    /*
        날짜/시간을 표현하는 포맷 정의. 어떤 형태로 표현되는지는 주석을 참고하세요.
     */
    public static String FORMAT_YEAR = "yyyy";  // 2018
    public static String FORMAT_DATE = "yyyy-MM-dd";    // 2018-04-16
    public static String FORMAT_DATE_AND_TIME = "yyyy-MM-dd HH:mm"; // 2018-04-16 18:30
    public static String FORMAT_TIME_MILLISECONDS = "HH:mm:ss"; // 18:30:59
    public static String FORMAT_MSG_KEY = "yyyy-MM-dd HH:mm:ss.SSS";    // 2018-04-16 18:30:59.999

    /**
     * long 타입의 시간을 매개변수로 받아서 [18:30:59] 형식의 String 으로 리턴한다.
     */
    public static String getHHmmssByLong(long time){
        String convert = "";
        convert = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
        return convert;
    }

    /**
     * String 타입의 시간을 매개변수로 받아서 [2018-04-16] 형식의 String 으로 리턴한다.
     * 시분초까지 함께 있는 시간에서 날짜만 필요할 경우를 위해 만듬.
     */
    public static String getyyyyMMdd(String timeStr){
        String convert = "";
        Calendar time = parseDateDefault(timeStr);  // 받은 문자열로 Calendar 객체 생성
        convert = formatDateMonthDay(time); // Calendar 객체로 [2018-04-16] 형태의 날짜 추출
        return convert;
    }


    /**
     * String 타입의 시간을 매개변수로 받아서 Calendar 객체로 변환하여 리턴한다.
     * 시간 계산 작업이 필요한 경우를 위해 만듬. (예) 이 시간이 오늘인지 확인한다.
     */
    public static Calendar parseDateDefault(String selectDay){
        SimpleDateFormat format = new SimpleDateFormat(FORMAT_DATE);
        Calendar calendar = Calendar.getInstance();
        try{
            Date result = format.parse(selectDay);
            calendar.setTime(result);
        }catch(Exception e){
            // String to Calendar 가 실패할 경우 필요한 작업을 작성한다.

            /*
                여기서 아무런 작업을 하지 않으면 현재 시각에 대한 Calendar 객체가 리턴된다.
                우선은 null 이 리턴되는 것보단 낫다고 생각해서 아무런 작업을 하지 않았음.
             */
        }
        return calendar;
    }

    /**
     * Calendar 객체를 매개변수로 받아 [2018-04-16] 형식의 String 으로 리턴한다.
     */
    public static String formatDateMonthDay(Calendar calendar){
        SimpleDateFormat format = new SimpleDateFormat(FORMAT_DATE);
        String date = format.format(calendar.getTime());
        return date;
    }

    /**
     * 현재 시각을 [2018-04-16 18:30] 형식의 String 으로 리턴한다.
     */
    public static String getyyyyMMDDHHmm(){
        SimpleDateFormat format = new SimpleDateFormat(FORMAT_DATE_AND_TIME);
        String date = format.format(Calendar.getInstance().getTime());
        return date;
    }

    /**
     *  Calendar 객체를 매개변수로 받아 [2018] 형식의 String 으로 리턴한다.
     * @param calendar
     * @return
     */
    public static String getyyyy(Calendar calendar){
        SimpleDateFormat format = new SimpleDateFormat(FORMAT_YEAR);
        String date = format.format(calendar.getTime());
        return date;
    }

}
