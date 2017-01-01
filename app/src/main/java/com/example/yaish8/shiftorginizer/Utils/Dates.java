package com.example.yaish8.shiftorginizer.Utils;
/**
 * Created by Tamar
 */
import java.util.Calendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Dates {
    private static DateFormat df=new SimpleDateFormat("dd/MM/yyyy");


    public static String[] getThisWeekDays()
    {
        String [] thisWeek = new String[7];
        Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
        thisWeek[0] = df.format(calendar.getTime());
        for (int i = 1; i<7; i++)
        {
            calendar.add(Calendar.DATE,1);
            thisWeek[i] = df.format(calendar.getTime());
        }
         return thisWeek;
    }

    public static String[] getNextWeekDays()
    {
        String [] thisWeek = new String[7];
        Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
        calendar.add(Calendar.DATE,7);
        thisWeek[0] = df.format(calendar.getTime());
        for (int i = 1; i<7; i++)
        {
            calendar.add(Calendar.DATE,1);
            thisWeek[i] = df.format(calendar.getTime());
        }
        return thisWeek;
    }


}
