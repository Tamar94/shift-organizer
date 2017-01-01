package com.example.yaish8.shiftorginizer.activities;
/**
 * Created by Tamar
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.Dates;
import com.example.yaish8.shiftorginizer.Utils.GoHomeClickListener;
import com.example.yaish8.shiftorginizer.Utils.Shift;
import com.example.yaish8.shiftorginizer.Utils.user.User;
import com.example.yaish8.shiftorginizer.Utils.user.UserShiftsPattern;
import com.example.yaish8.shiftorginizer.Utils.WorkArrange;
import java.util.Map;
import java.util.TreeSet;

public class CreateWorkArrangeActivity extends AppCompatActivity{
    private static ExpandableListView listView;
    private ImageView homeIcon;
    private RelativeLayout loading;
    private TreeSet<Shift>[] daysShiftArray;
    private UserShiftsPattern shiftsPattern;
    private WorkArrange workArrange;
    private static Map<String,String>[] workArrangeData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_work_arrange);
        shiftsPattern =UserShiftsPattern.getInstance(User.company,this);

        homeIcon = (ImageView)findViewById(R.id.homeIcon_createWorkArrange);
        homeIcon.setOnClickListener(new GoHomeClickListener(this));

        loading = (RelativeLayout)findViewById(R.id.createArr_loadingPanel);
        loading.setVisibility(View.VISIBLE);

        //set list view
        listView = (ExpandableListView)findViewById(R.id.createArr_list);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //get shift pattern for each day
        daysShiftArray = new TreeSet[7];
        for (int i = 0; i<daysShiftArray.length;i++)
        {
            daysShiftArray[i] = shiftsPattern.getDayShifts(i);
        }

        workArrange = new WorkArrange(this, daysShiftArray,Dates.getNextWeekDays()[0].replaceAll("/",""));
    }

    public void saveArrange(View v)
    {
       workArrange.sendNewArrange(workArrangeData);
    }

    public static void setWorkArrange(Map<String,String>[] newWorkArrange)
    {
        workArrangeData = newWorkArrange;
    }

    public static ExpandableListView getListView()
    {
        return listView;
    }
}
