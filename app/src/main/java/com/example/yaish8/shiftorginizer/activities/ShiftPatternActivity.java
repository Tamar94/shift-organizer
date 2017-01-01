package com.example.yaish8.shiftorginizer.activities;
/**
 * Created by Tamar
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;

import android.widget.Spinner;
import android.widget.Toast;
import com.example.yaish8.shiftorginizer.Utils.Adapters.ShiftPatternAdapter;
import com.example.yaish8.shiftorginizer.Utils.GoHomeClickListener;

import com.example.yaish8.shiftorginizer.Utils.user.User;
import com.example.yaish8.shiftorginizer.Utils.Shift;
import com.example.yaish8.shiftorginizer.Utils.user.UserShiftsPattern;
import com.example.yaish8.shiftorginizer.R;


import java.util.TreeSet;

public class ShiftPatternActivity extends AppCompatActivity{
    private Spinner daysSpinner;
    private ListView shiftsList;
    private UserShiftsPattern shiftsPattern;
    private EditText employNum;
    private String format;
    private ImageView homeIcon;
    private NumberPicker hoursStart, hoursEnd, minStart, minEnd;
    private static TreeSet<Shift>[] daysShiftsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_pattern);

        //get UserShiftsPattern Instance
        shiftsPattern = UserShiftsPattern.getInstance(User.company,this);

        //initialize attributes
        homeIcon = (ImageView)findViewById(R.id.homeIcon_shiftPattern);
        homeIcon.setOnClickListener(new GoHomeClickListener(this));
        daysShiftsArray = new TreeSet[7];
        shiftsList = (ListView) findViewById(R.id.shiftPatt_list);
        employNum = (EditText)findViewById(R.id.shiftPatt_employInput);
        format = "%02d";

        daysSpinner = (Spinner) findViewById(R.id.shiftPatt_daysSpin);
        //Create an ArrayAdapter using the string days array
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.days_array,R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        daysSpinner.setAdapter(adapter);
        daysSpinner.setOnItemSelectedListener(new ShiftSpinnerListener());
        setNumberPicker();
    }


    @Override
    protected void onStart() {
        super.onStart();
        //update any local data
        for (int i = 0; i<daysShiftsArray.length;i++)
        {
            daysShiftsArray[i] = shiftsPattern.getDayShifts(i);
        }
    }


    public void addShift(View v)
    {
        //get user hours input
        String startTime = ""+String.format(format, hoursStart.getValue())+":"
                +String.format(format, minStart.getValue()),
                endTime = ""+String.format(format, hoursEnd.getValue())+":"
                        +String.format(format, minEnd.getValue());

        String numInput = employNum.getText().toString(),
                msg = "Please enter employees number"; //default msg
        //check valid employ num
        if (!employNum.getText().toString().isEmpty())
        {
            int num = Integer.parseInt(numInput);
            if(num>0)
            {
                //create new shift
                Shift newShift = new Shift(startTime,endTime,num);
                //find the right index for the selected day
                int dayIndex = findDayIndex(daysSpinner.getSelectedItem().toString());
                //add the new shift only to local data and reload list view
                daysShiftsArray[dayIndex].add(newShift);
                shiftsList.setAdapter(new ShiftPatternAdapter(v.getContext(), daysShiftsArray[dayIndex], dayIndex));
            }
            else {
                msg = "Employees number isn't valid";
                Toast.makeText(v.getContext(), msg, Toast.LENGTH_LONG).show();
            }
        }
        else
            Toast.makeText(v.getContext(),msg,Toast.LENGTH_LONG).show();
    }

    public void done (View v)
    {
        shiftsPattern.updateShiftDayPattern(daysShiftsArray);
    }

    private void setNumberPicker()
    {
        NumberPicker.Formatter formatter = new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format(format, i);
            }
        };

        //sat start hours
        hoursStart = (NumberPicker) findViewById(R.id.hours_start);
        hoursStart.setMinValue(1);
        hoursStart.setMaxValue(24);
        hoursStart.setFormatter(formatter);

        //set start minute
        minStart = (NumberPicker) findViewById(R.id.min_start);
        minStart.setMinValue(0);
        minStart.setMaxValue(60);
        minStart.setFormatter(formatter);

        //sat end hours
        hoursEnd = (NumberPicker) findViewById(R.id.hours_end);
        hoursEnd.setMinValue(1);
        hoursEnd.setMaxValue(24);
        hoursEnd.setFormatter(formatter);

        //set end minute
        minEnd = (NumberPicker) findViewById(R.id.min_end);
        minEnd.setMinValue(0);
        minEnd.setMaxValue(60);
        minEnd.setFormatter(formatter);
    }

    private class ShiftSpinnerListener implements AdapterView.OnItemSelectedListener
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, final View view, final int i, long id) {

            shiftsList.setAdapter(new ShiftPatternAdapter(view.getContext(),daysShiftsArray[i],i));

        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private int findDayIndex(String day)
    {
        String[] daysNameArray = getResources().getStringArray(R.array.days_array);
        for (int i = 0; i<daysNameArray.length; i++)
        {
            //find the right index for the selected day
            if (daysNameArray[i].equals(day)) return i;
        }
        return -1;
    }
    }


