package com.musicplayer.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
public class TimeUtils {
    // 将毫秒转时分秒
    public static String transformTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds)
                : String.format("%02d:%02d", minutes, seconds);
    }

    public static long getMSTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");
        Date date = new Date(System.currentTimeMillis());
        long time = Long.parseLong(simpleDateFormat.format(date));
        return time;
    }

}
