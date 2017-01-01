package com.example.yaish8.shiftorginizer.Utils;
/**
 * Created by Tamar
 */

import java.util.TreeSet;

public class DataStringConvert {
    public static String[] getStartsTime(Object data)
    {
        //replace the end time&employees number (-00:00[2]) with ":"
        String str = ((String)data).replaceAll("-[0-9]{2}:[0-9]{2}\\[[0-9]+\\]", "#");
        //get an array only with the hours
        return str.split("#");
    }

    public static String[] getEndsTime(Object data)
    {
        //cut the firs start time
        String str = ((String)data).substring(((String)data).indexOf('-')+1);
        //replace the start time&employees number ([2]00:00-) with ""
        str = str.replaceAll("[0-9]{2}:[0-9]{2}-|\\[[0-9]+\\][0-9]{2}:[0-9]{2}-", "#");
        //cut the last employees number
        str = str.substring(0,str.lastIndexOf('['));
        //get an array only with the hours
        return str.split("#");
    }

    public static int[] getEmployeesNumber(Object data)
    {
        //replace the hours (00:00-00:00) with ":"
        String str = ((String)data).replaceAll("[0-9]{2}:[0-9]{2}-[0-9]{2}:[0-9]{2}", "#");
        //replace []
        str = str.replaceAll("\\[|\\]", "");
        //get an array only with the employees number
        String[] temp = (str.split("#"));
        int[] employNum = new int[temp.length-1];
        //parse to int
        for (int i = 1; i<temp.length;i++)
        {
            employNum[i-1] = Integer.parseInt(temp[i]);
        }
        return employNum;
    }

    public static String getDayPatternString(TreeSet<Shift> dayPattern)
    {
        String dataString="";
        for (Shift temp : dayPattern)
        {
            dataString += temp.toDataString();
        }
        return dataString;
    }

    public static String getRequestsString(TreeSet<String> dayReq)
    {
        String dataString = "";
        for (String temp : dayReq)
        {
            dataString += temp+"#";
        }
        return  dataString;
    }
}
