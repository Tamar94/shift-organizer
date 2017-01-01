package com.example.yaish8.shiftorginizer.Utils;
/**
 * Created by Tamar
 */
public class Shift implements Comparable{
    private String startTime, endTime;
    private int employNum;

    public Shift(String start, String end, int employ)
    {
        startTime = start;
        endTime = end;
        employNum = employ;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public int getEmployNum() {
        return employNum;
    }

    public void setEmployNum(int employNum) {
        this.employNum = employNum;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String toString()
    {
        return startTime+" - "+endTime+", "+employNum+" employees";
    }

    public String toTimeString()
    {
        return startTime+" - "+endTime;
    }

    public String toDataString() {return startTime+"-"+endTime+"["+employNum+"]";}

    @Override
    public int compareTo(Object other)
    {
        return this.toString().compareTo(((Shift)other).toString());
    }
}
