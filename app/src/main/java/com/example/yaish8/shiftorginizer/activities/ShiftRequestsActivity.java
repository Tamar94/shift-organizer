package com.example.yaish8.shiftorginizer.activities;

/**
 * Created by Tamar
 */
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.GoHomeClickListener;
import com.example.yaish8.shiftorginizer.Utils.user.User;
import com.example.yaish8.shiftorginizer.Utils.user.UserRequests;
import java.util.Map;
import java.util.TreeSet;

public class ShiftRequestsActivity extends AppCompatActivity {
    private static Map<String, TreeSet<String>>[] daysReqArray;
    ImageView homeIcon;
    private RelativeLayout loadingCircle;
    private UserRequests userRequests;
    private static ExpandableListView listView;
    private String[] daysNameArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_requests);
        homeIcon = (ImageView)findViewById(R.id.homeIcon_shiftRequest);
        homeIcon.setOnClickListener(new GoHomeClickListener(this));
        daysReqArray = new Map[7];
        daysNameArray = getResources().getStringArray(R.array.days_array);

        loadingCircle=(RelativeLayout)findViewById(R.id.shiftReq_loadingPanel);
        listView = (ExpandableListView)findViewById(R.id.shiftReq_list);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //delete any local data
        for (int i = 0; i<daysReqArray.length;i++)
        {
            daysReqArray[i] = null;
        }

        //get user requests
        userRequests = new UserRequests(User.userName,User.company,this);
    }

    public void saveRequests(final View v)
    {
        userRequests.sendRequests(daysReqArray);
    }

    public static Map<String, TreeSet<String>>[] getDaysReqArray()
    {
       return ShiftRequestsActivity.daysReqArray;
    }

    public static ExpandableListView getListView()
    {
        return listView;
    }


}
