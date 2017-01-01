package com.example.yaish8.shiftorginizer.Utils.Adapters;
/**
 * Created by Nimrod
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.Dates;

import java.util.ArrayList;
import java.util.LinkedHashMap;


public class ShiftMainScreenAdapter extends BaseExpandableListAdapter {
    private final Context context;
    private final LinkedHashMap<String, String[]> shifts;
    private String[] weekDates;
    private ArrayList<String> days;

    public ShiftMainScreenAdapter(Context context, LinkedHashMap<String,String[]> shifts, String[] weekDates) {
        this.context = context;
        this.shifts=shifts;
        this.weekDates = weekDates;
        days = new ArrayList<>(this.shifts.keySet());
    }

    @Override
    public int getGroupCount() {
        return days.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return 1;
    }

    @Override
    public String getGroup(int i) {
       return days.get(i);
    }

    @Override
    public String[] getChild(int i, int i1) {
        return shifts.get(days.get(i));
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View rView, ViewGroup viewGroup) {
        if(rView == null)rView = LayoutInflater.from(context).inflate(R.layout.day_item, null);
        LinearLayout ll = (LinearLayout)rView;
        TextView dayName =(TextView) ll.findViewById(R.id.dayName);
        dayName.setText(days.get(i)+" "+weekDates[i]);
        return rView;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View rView, ViewGroup parent) {

        if(rView == null)rView = LayoutInflater.from(context).inflate(R.layout.shift_hours_inflate, null);
        LinearLayout ll = (LinearLayout)rView;
        TextView inflate_hours =(TextView) ll.findViewById(R.id.inflate_hours);
        TextView inflate_names =(TextView) ll.findViewById(R.id.inflate_names);
        String day = days.get(groupPosition);
        String[] hoursAndEmploye = shifts.get(day);
        String[] arrHours = new String[hoursAndEmploye.length];
        String[] arrEmployees = new String[hoursAndEmploye.length];
        for(int j = 0; j < hoursAndEmploye.length; j++){
            String s = hoursAndEmploye[j];
            if(!s.isEmpty())
            {
                String hours = s.substring(0, s.indexOf("#"));
                String employees = s.substring(s.indexOf("#")+1);
                arrHours[j] = hours;
                arrEmployees[j] = employees;
            }

        }
        if (arrHours[0]!=null || arrEmployees[0]!=null)
        {
            inflate_hours.setText(getFixedString(arrHours));
            inflate_names.setText(getFixedString(arrEmployees));
        }
        return rView;
    }

    private String getFixedString(String[] hours){
        String str = "";
        for(int i = 0; i < hours.length; i++){
            str += hours[i];
            if(hours.length - i != 1)str += "\n\n";
        }

        return str;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

}
