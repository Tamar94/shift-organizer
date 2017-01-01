package com.example.yaish8.shiftorginizer.Utils.Adapters;
/**
 * Created by Tamar
 */
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
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
                LinearLayout shiftLayout = (LinearLayout) LayoutInflater.from(CONTEXT).inflate(R.layout.shift_req, null);
                //set on click
                shiftLayout.setOnClickListener(new ChangeShiftRequest(blockShifts, ableShifts, groupPosition));
                //set the text shift
                TextView shift = (TextView) shiftLayout.findViewById(R.id.time_text);
                shift.setText(correctShift);

                //check if this shift is block
                for (String temp : blockShifts) {
                    //if found correct shift - change color
                    if (correctShift.compareTo(temp) == 0)
                        shift.setBackgroundColor(Color.parseColor("#FA5C5C"));
                }

                allShiftsLayout.addView(shiftLayout);
            }
        }
        else
        {
            //create view
            LinearLayout shiftLayout = (LinearLayout) LayoutInflater.from(CONTEXT).inflate(R.layout.shift_req, null);
            //set the text shift
            TextView noShift = (TextView) shiftLayout.findViewById(R.id.time_text);
            noShift.setText("No Shifts");
            noShift.setBackgroundColor(Color.WHITE);
            allShiftsLayout.addView(shiftLayout);
        }

        return allShiftsLayout;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private class ChangeShiftRequest implements View.OnClickListener
    {
        private final TreeSet<String> BLOCK,ABLE;
        private boolean isBlock;
        private int dayIndex;
        ChangeShiftRequest(TreeSet<String> block,TreeSet<String> able, int day)
        {
            if (block==null)
                BLOCK = new TreeSet<>();
            else
                BLOCK = block;
            if (able==null)
                ABLE = new TreeSet<>();
            else
                ABLE = able;
            isBlock = false;
            dayIndex = day;
        }

        @Override
        public void onClick(View v) {
            TextView textShift = (TextView) ((LinearLayout) v).getChildAt(0);
            String correctShift = textShift.getText().toString();

            String temp;
            //search in block set
            Iterator<String> iterator = BLOCK.iterator();
            while (iterator.hasNext()) {
                temp = iterator.next();
                //find if the selected shift is in the block set(compareTo return 0 = equal)
                if (correctShift.compareTo(temp) == 0) {
                    isBlock = true;
                    v.setBackgroundColor(Color.parseColor("#94F084"));
                    //transfer the shift to the able set
                    iterator.remove();
                    ABLE.add(correctShift);
                }
            }

            //only if the shift isn't in the block set - search it in able set
            if (!isBlock) {
                iterator = ABLE.iterator();
                while (iterator.hasNext()) {
                    temp = iterator.next();
                    //find if the selected shift is in the able set(compareTo return 0 = equal)
                    if (correctShift.compareTo(temp) == 0) {
                        v.setBackgroundColor(Color.parseColor("#FA5C5C"));
                        //transfer the shift to the block set
                        iterator.remove();
                        BLOCK.add(correctShift);
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
