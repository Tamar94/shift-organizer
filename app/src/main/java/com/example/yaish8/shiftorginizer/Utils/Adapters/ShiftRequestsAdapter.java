package com.example.yaish8.shiftorginizer.Utils.Adapters;
/**
 * Created by Tamar
 */
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.Dates;
import com.example.yaish8.shiftorginizer.activities.ShiftRequestsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

public class ShiftRequestsAdapter extends BaseExpandableListAdapter {
    private final Context CONTEXT;
    private final ArrayList<String> DAYS_NAME;
    private final String[] nextWeekDates;
    static boolean isTouched = false;
    private final LinkedHashMap<String, Map<String, TreeSet<String>>> ALL_DAYS_SHIFTS_REQUESTS;

    public ShiftRequestsAdapter(LinkedHashMap<String, Map<String, TreeSet<String>>> allDayShiftRequests, Context context) {
        ALL_DAYS_SHIFTS_REQUESTS = allDayShiftRequests;
        CONTEXT = context;
        DAYS_NAME = new ArrayList<>(allDayShiftRequests.keySet());
        nextWeekDates = Dates.getNextWeekDays();
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

        //get this day shifts
        Map<String, TreeSet<String>> dayPattern = ALL_DAYS_SHIFTS_REQUESTS.get(DAYS_NAME.get(groupPosition));
        TreeSet<String> ableShifts  = dayPattern.get("able") ,
                        blockShifts  =  dayPattern.get("block") ,
                        allShifts = new TreeSet<>();
        if (blockShifts==null)blockShifts = new TreeSet<>();
        if (ableShifts == null)ableShifts = new TreeSet<>();

        //if there is any shifts for this day  - get them
        if (!ableShifts.isEmpty() || !blockShifts.isEmpty()) {

            //create set with all shifts
            allShifts.addAll(blockShifts);
            allShifts.addAll(ableShifts);

            for (String correctShift : allShifts) {
                //create view for each shift
                RelativeLayout shiftLayout = (RelativeLayout) LayoutInflater.from(CONTEXT).inflate(R.layout.shift_req, null);
                //set switch
                SwitchCompat mySwitch = (SwitchCompat) shiftLayout.findViewById(R.id.switch_req);
                mySwitch.setChecked(true);
                mySwitch.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        isTouched = true;
                        return false;
                    }
                });
                mySwitch.setOnCheckedChangeListener(new SwitchShiftRequest(blockShifts, ableShifts, groupPosition,correctShift));
                //set the text shift
                TextView shift = (TextView) shiftLayout.findViewById(R.id.time_text);
                shift.setText(correctShift);

                //check if this shift is block
                for (String temp : blockShifts) {
                    //if found correct shift - change color
                    if (correctShift.compareTo(temp) == 0) {
                        mySwitch.setChecked(false);
                    }
                }

                allShiftsLayout.addView(shiftLayout);
            }
        }
        else
        {
            //create view
            RelativeLayout shiftLayout = (RelativeLayout) LayoutInflater.from(CONTEXT).inflate(R.layout.shift_req, null);
            //set the text shift
            TextView noShift = (TextView) shiftLayout.findViewById(R.id.time_text);
            noShift.setText("No Shifts");
            allShiftsLayout.addView(shiftLayout);
        }

        return allShiftsLayout;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private class SwitchShiftRequest implements CompoundButton.OnCheckedChangeListener{
        private final TreeSet<String> BLOCK,ABLE;
        private int dayIndex;
        private String shift;

        SwitchShiftRequest(TreeSet<String> block,TreeSet<String> able, int day, String shift)
        {
            if (block==null)
                BLOCK = new TreeSet<>();
            else
                BLOCK = block;
            if (able==null)
                ABLE = new TreeSet<>();
            else
                ABLE = able;
            this.shift = shift;
            dayIndex = day;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Iterator<String> iterator;
            String temp;
            if (isTouched) {
                isTouched = false;
                if (isChecked) {
                    iterator = BLOCK.iterator();
                    while (iterator.hasNext()) {
                        temp = iterator.next();
                        //find the selected shift in the block set(compareTo return 0 = equal)
                        if (shift.compareTo(temp) == 0) {
                            //transfer the shift to the block set
                            iterator.remove();
                            ABLE.add(shift);
                        }
                    }
                } else {
                    iterator = ABLE.iterator();
                    while (iterator.hasNext()) {
                        temp = iterator.next();
                        //find the selected shift in the able set(compareTo return 0 = equal)
                        if (shift.compareTo(temp) == 0) {
                            //transfer the shift to the block set
                            iterator.remove();
                            BLOCK.add(shift);
                        }
                    }
                }
                Map<String, TreeSet<String>> allShifts = new HashMap<>();
                allShifts.put("able", ABLE);
                allShifts.put("block", BLOCK);
                //get the activity data
                Map<String, TreeSet<String>>[] daysReqArray = ShiftRequestsActivity.getDaysReqArray();
                //update the Activity data
                daysReqArray[dayIndex] = allShifts;

                //set the list view with the new data
                ExpandableListView newListView = ShiftRequestsActivity.getListView();
                LinkedHashMap<String, Map<String, TreeSet<String>>> allDaysShift = new LinkedHashMap<>();
                for (int i = 0; i < daysReqArray.length; i++) {
                    allDaysShift.put(DAYS_NAME.get(i), daysReqArray[i]);
                }

                //save expanded days
                ArrayList<Integer> expandedDays = new ArrayList<>();
                for (int i = 0; i < DAYS_NAME.size(); i++) {
                    if (newListView.isGroupExpanded(i))
                        expandedDays.add(i);
                }
                //set adapter
                newListView.setAdapter(new ShiftRequestsAdapter(allDaysShift, CONTEXT));
                //set expanded days
                for (Integer day : expandedDays) {
                    if (!newListView.isGroupExpanded(day))
                        newListView.expandGroup(day);
                }
            }
        }
    }
}
