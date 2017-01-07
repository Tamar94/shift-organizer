package com.example.yaish8.shiftorginizer.Utils.Adapters;
/**
 * Created by Tamar
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.Shift;
import com.example.yaish8.shiftorginizer.activities.ShiftPatternActivity;

import java.util.TreeSet;


public class ShiftPatternAdapter extends BaseAdapter {
    private final Context CONTEXT;
    private final TreeSet<Shift> DAY_SHIFTS;
    private final int DAY_INDEX;

    public ShiftPatternAdapter(Context context, TreeSet<Shift> dayShifts, int dayIndex) {
        CONTEXT = context;
        DAY_SHIFTS = dayShifts;
        DAY_INDEX = dayIndex;
    }

    @Override
    public int getCount() {
        return DAY_SHIFTS.size();
    }

    @Override
    public Object getItem(int i) {
        Shift[] temp = new Shift[DAY_SHIFTS.size()];
        temp = DAY_SHIFTS.toArray(temp);
        return temp[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View recycleView, final ViewGroup parent) {
        if(recycleView==null)
            recycleView = LayoutInflater.from(CONTEXT).inflate(R.layout.shift,null);
        TextView shift = (TextView)recycleView.findViewById(R.id.shift_text);
        //show relevant text in reused view
        Shift correctShift = ((Shift)getItem(i));
        shift.setText(correctShift.toTimeString()+"\n"+correctShift.getEmployNum()+" employees");

        //set edit button
        ImageView edit = (ImageView) recycleView.findViewById(R.id.shift_edit);
        edit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //get edit shift details
                final Shift correctShift = (Shift) getItem(i);
                String startTime = correctShift.getStartTime(),
                        endTime = correctShift.getEndTime();
                final int employNum = correctShift.getEmployNum();

                //find the number pickers
                final View activityLayout = (View) parent.getParent();
                final NumberPicker hoursStart = (NumberPicker) activityLayout.findViewById(R.id.hours_start),
                        hoursEnd = (NumberPicker) activityLayout.findViewById(R.id.hours_end),
                        minStart = (NumberPicker) activityLayout.findViewById(R.id.min_start),
                        minEnd = (NumberPicker) activityLayout.findViewById(R.id.min_end);

                //set start time picker to the edit shift start time
                int cutIndex = startTime.indexOf(":");
                hoursStart.setValue(Integer.parseInt(startTime.substring(0,cutIndex)));
                minStart.setValue(Integer.parseInt(startTime.substring(cutIndex+1)));

                //set end time picker to the edit shift end time
                cutIndex = endTime.indexOf(":");
                hoursEnd.setValue(Integer.parseInt(endTime.substring(0,cutIndex)));
                minEnd.setValue(Integer.parseInt(endTime.substring(cutIndex+1)));

                //set employ num to the edit shift employ num
                final EditText inputEmployNum = (EditText)activityLayout.findViewById(R.id.shiftPatt_employInput);
                inputEmployNum.setText(""+employNum);

                //add btn - gone
                final Button add = (Button)activityLayout.findViewById(R.id.shiftPatt_addBtn);
                add.setVisibility(View.GONE);

                //save & cancel btns - visible
                final Button save = (Button)activityLayout.findViewById(R.id.shiftPatt_saveEditBtn),
                        cancel  =(Button)activityLayout.findViewById(R.id.shiftPatt_cancelEditBtn);
                save.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);

                //save btn - on click
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //get user hours input_login
                        String format = "%02d",
                                startTime = ""+String.format(format, hoursStart.getValue())+":"
                                        +String.format(format, minStart.getValue()),
                                endTime = ""+String.format(format, hoursEnd.getValue())+":"
                                        +String.format(format, minEnd.getValue());

                        String numInput = inputEmployNum.getText().toString(),
                                msg = "Please enter employees number"; //default msg
                        //check valid employ num
                        if (!inputEmployNum.getText().toString().isEmpty())
                        {
                            int num = Integer.parseInt(numInput);
                            if(num>0)
                            {
                                correctShift.setEmployNum(num);
                                correctShift.setEndTime(endTime);
                                correctShift.setStartTime(startTime);
                                //update
                                DAY_SHIFTS.remove(getItem(i));
                                DAY_SHIFTS.add(correctShift);
                                //find and update list view
                                ListView shiftsList = (ListView)activityLayout.findViewById(R.id.shiftPatt_list);
                                shiftsList.setAdapter(new ShiftPatternAdapter(v.getContext(), DAY_SHIFTS, DAY_INDEX));

                                save.setVisibility(View.GONE);
                                cancel.setVisibility(View.GONE);
                                add.setVisibility(View.VISIBLE);
                            }
                            else {
                                msg = "Employees number isn't valid";
                                Toast.makeText(v.getContext(), msg, Toast.LENGTH_LONG).show();
                            }
                        }
                        else
                            Toast.makeText(v.getContext(),msg,Toast.LENGTH_LONG).show();
                    }
                });

                //cancel Btn on click
                cancel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        save.setVisibility(View.GONE);
                        cancel.setVisibility(View.GONE);
                        add.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        //set delete button
        ImageView delete = (ImageView) recycleView.findViewById(R.id.shift_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //remove from local data
                DAY_SHIFTS.remove(getItem(i));
                //find and update list view
                ListView shiftsList = (ListView)parent.findViewById(R.id.shiftPatt_list);
                shiftsList.setAdapter(new ShiftPatternAdapter(v.getContext(), DAY_SHIFTS, DAY_INDEX));
            }
        });

        return recycleView;
    }
}
