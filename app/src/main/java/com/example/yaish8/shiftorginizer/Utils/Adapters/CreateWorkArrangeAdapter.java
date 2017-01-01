package com.example.yaish8.shiftorginizer.Utils.Adapters;
/**
 * Created by Tamar
 */
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.Dates;
import com.example.yaish8.shiftorginizer.Utils.Shift;
import com.example.yaish8.shiftorginizer.activities.CreateWorkArrangeActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;


public class CreateWorkArrangeAdapter extends BaseExpandableListAdapter {
    private final Context CONTEXT;
    private final ArrayList<String> DAYS_NAME;
    private Map<String,String>[] allShiftsWithChosenEmployees;
    private TextView emloyChoice;
    private String[] nextWeekDates;
    private final LinkedHashMap<String,Map<Shift,String>> ALL_DAYS_SHIFTS_REQUESTS;

    public CreateWorkArrangeAdapter(LinkedHashMap<String,Map<Shift,String>> allDayShiftRequests, Context context, Map<String,String>[] allShiftsWitheChosenEmployees) {
        ALL_DAYS_SHIFTS_REQUESTS = allDayShiftRequests;
        CONTEXT = context;
        DAYS_NAME = new ArrayList<>(allDayShiftRequests.keySet());
        nextWeekDates = Dates.getNextWeekDays();
        this.allShiftsWithChosenEmployees = allShiftsWitheChosenEmployees;
    }

    @Override
    public int getGroupCount() {
        return DAYS_NAME.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return 1;
    }

    @Override
    public Object getGroup(int i) {
        return DAYS_NAME.get(i);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return ALL_DAYS_SHIFTS_REQUESTS.get(DAYS_NAME.get(groupPosition));
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean isExpanded, View rView, ViewGroup parent) {

        if(rView == null)
            rView = LayoutInflater.from(CONTEXT).inflate(R.layout.day_item, null);
        LinearLayout ll = (LinearLayout)rView;
        //set the day name
        TextView dayName =(TextView) ll.findViewById(R.id.dayName);
        dayName.setText(DAYS_NAME.get(i)+" "+nextWeekDates[i]);
        return rView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View recycleView, ViewGroup parent) {
        if(recycleView==null)
            recycleView = new LinearLayout(CONTEXT);
        //set vertical & remove all views from the other days
        LinearLayout allShiftsLayout = (LinearLayout)recycleView;
        allShiftsLayout.setOrientation(LinearLayout.VERTICAL);
        allShiftsLayout.removeAllViews();

        //get this day shifts with req
        Map<Shift,String> dayShiftWithReq = ALL_DAYS_SHIFTS_REQUESTS.get(DAYS_NAME.get(groupPosition));

        //get this day shifts
        TreeSet<Shift> dayShifts = new TreeSet<>(ALL_DAYS_SHIFTS_REQUESTS.get(DAYS_NAME.get(groupPosition)).keySet());

        //if there is any shifts for this day  - get them
        if (!dayShifts.isEmpty()) {
            for (Shift correctShift : dayShifts) {
                //create view for each shift
                LinearLayout shiftLayout = (LinearLayout) LayoutInflater.from(CONTEXT).inflate(R.layout.shift_hours_inflate, null);
                //set the text shift
                String shiftHours = correctShift.toTimeString();
                TextView shift = (TextView) shiftLayout.findViewById(R.id.inflate_hours);
                shift.setText(shiftHours);

                //set employee choice
                emloyChoice = (TextView) shiftLayout.findViewById(R.id.inflate_names);
                emloyChoice.setOnClickListener(new ChooseEmployListener(groupPosition, correctShift, dayShiftWithReq.get(correctShift)));

                //check if there is already employees chosen for this shift
                if (!allShiftsWithChosenEmployees[groupPosition].isEmpty()) {
                    String thisShiftChosenEmployees = allShiftsWithChosenEmployees[groupPosition].get(shiftHours);
                    if (thisShiftChosenEmployees != null)
                        //set the chosen employees
                        setEmloyChoise(thisShiftChosenEmployees);
                    else
                        setEmloyChoise("Choose Employees");
                }
                else
                    setEmloyChoise("Choose Employees");
                allShiftsLayout.addView(shiftLayout);
            }
        }
        else
        {
            //create view
            LinearLayout shiftLayout = (LinearLayout) LayoutInflater.from(CONTEXT).inflate(R.layout.shift_hours_inflate, null);
            //set the text shift
            TextView noShift = (TextView) shiftLayout.findViewById(R.id.inflate_hours);
            noShift.setText("No Shifts");
            allShiftsLayout.addView(shiftLayout);
        }

        return allShiftsLayout;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private class ChooseEmployListener implements View.OnClickListener
    {
        ArrayList<Integer> selectedItems = new ArrayList<>();
        boolean[] isSelectedArray;
        Shift shift;
        String shiftTime;
        int howManySelected, maxEmployees, day;
        CharSequence[] items;

        private ChooseEmployListener(int day, Shift shift, String ableUsers)
        {
            this.shift = shift;
            this.day = day;
            this.shiftTime = shift.toTimeString();
            this.maxEmployees = shift.getEmployNum();
            howManySelected = 0;
            items = ableUsers.split(",");
            isSelectedArray = new boolean[items.length];
            //check if there some employees chosen - and set them as checked
            for (int i=0;i<isSelectedArray.length; i++) {
                if (!allShiftsWithChosenEmployees[day].isEmpty()) {
                    String correctShift = allShiftsWithChosenEmployees[day].get(shiftTime);
                    if (correctShift!=null)if (correctShift.contains(items[i])) {
                        isSelectedArray[i] = true;
                        howManySelected += 1;
                        selectedItems.add(i);
                    } else
                        isSelectedArray[i] = false;
                }
            }
        }

        @Override
        public void onClick(View v) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(CONTEXT)
                    .setTitle(shift.toString())
                    .setMultiChoiceItems(items, isSelectedArray, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if (isChecked) {
                                    selectedItems.add(which);
                                    howManySelected+=1;

                                if (howManySelected==maxEmployees){
                                    Toast.makeText(CONTEXT, "Shift is full!", Toast.LENGTH_LONG).show();
                                }
                            }
                            else if(selectedItems.contains(which))
                            {
                                selectedItems.remove(Integer.valueOf(which));
                                howManySelected-=1;
                            }
                        }
                    })
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            StringBuilder sb = new StringBuilder();
                            //get all the selected employees
                            if(selectedItems.size()!=0)
                            {
                                for (int i = 0; i<selectedItems.size();i++)
                                {sb.append(items[selectedItems.get(i)]+", ");}
                                //delete the last ", "
                                sb.delete(sb.length()-2, sb.length());
                            }
                            else
                                sb.append("Choose Employees");

                            allShiftsWithChosenEmployees[day].put(shiftTime,sb.toString());
                            //sat local data
                            CreateWorkArrangeActivity.setWorkArrange(allShiftsWithChosenEmployees);
                            setEmloyChoise(sb.toString());

                            ExpandableListView newListView = CreateWorkArrangeActivity.getListView();
                            //save expanded days
                            ArrayList<Integer> expandedDays = new ArrayList<>();
                            for (int i = 0; i < DAYS_NAME.size(); i++) {
                                if (newListView.isGroupExpanded(i))
                                    expandedDays.add(i);
                            }
                            //set adapter
                            newListView.setAdapter(new CreateWorkArrangeAdapter(ALL_DAYS_SHIFTS_REQUESTS,CONTEXT, allShiftsWithChosenEmployees));
                            //set expanded days
                            for (Integer day : expandedDays) {
                                if (!newListView.isGroupExpanded(day))
                                    newListView.expandGroup(day);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null);
            builder.show();

        }
    }

    private void setEmloyChoise(String string)
    {
        emloyChoice.setText(string);
    }
}

