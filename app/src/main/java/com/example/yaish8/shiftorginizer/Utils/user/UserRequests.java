package com.example.yaish8.shiftorginizer.Utils.user;
/**
 * Created by Tamar
 */
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.Adapters.ShiftRequestsAdapter;
import com.example.yaish8.shiftorginizer.Utils.DataStringConvert;
import com.example.yaish8.shiftorginizer.Utils.Shift;
import com.example.yaish8.shiftorginizer.activities.MainScreenActivity;
import com.example.yaish8.shiftorginizer.activities.ShiftRequestsActivity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

public class UserRequests {
    private final String USER_NAME,COMPANY,TABLE_NAME, TAG = "UserReq";
    private String[] daysNameArray;
    private final Context CONTEXT;
    private final BackendlessDataQuery QUERY;
    //save the requests for each day with map: key-able/block, value-shifts
    private Map<String, TreeSet<String>>[] daysRequestsArray;

    public UserRequests(String userName, String company, Context context)
    {
        USER_NAME = userName;
        daysRequestsArray = new Map[7];
        TABLE_NAME = company+"EmployeesRequests";
        CONTEXT = context;
        COMPANY = company;
        daysNameArray = context.getResources().getStringArray(R.array.days_array);
        QUERY = new BackendlessDataQuery("userName='"+USER_NAME+"'");
        if (userName!=null)
            loadRequests();
    }

    public void sendRequests(final Map<String, TreeSet<String>>[] userReqArray)
    {
        //find the row to update
        Backendless.Persistence.of(TABLE_NAME).find(QUERY, new AsyncCallback<BackendlessCollection<Map>>() {
            Map userReqData;
            public void handleResponse(BackendlessCollection<Map> data) {
                //check if there is any data
                if (!data.getData().isEmpty())
                {
                    userReqData = data.getData().get(0);
                    userReqData.put("creator", User.permission);
                    String day;
                    for (int i = 0; i < userReqArray.length; i++)
                    {
                        day = daysNameArray[i].toLowerCase();
                        //only update if there is a chance that this day have changed
                        if (userReqArray[i] != null)
                        {
                            if (userReqArray[i].get("able")!=null)
                                userReqData.put(day+"Able", DataStringConvert.getRequestsString(userReqArray[i].get("able")));
                            else
                                userReqData.put(day+"Able", "");

                            if (userReqArray[i].get("block")!=null)
                                 userReqData.put(day+"Block", DataStringConvert.getRequestsString(userReqArray[i].get("block")));
                            else
                                userReqData.put(day+"Block","");
                        }

                        else
                        {
                            //if there is no data - put an empty String
                            userReqData.put(day+"Able", "");
                            userReqData.put(day+"Block", "");
                        }

                    }
                }
                //save the new date
                Backendless.Persistence.of(TABLE_NAME).save(userReqData, new AsyncCallback<Map>() {
                    public void handleResponse(Map map) {
                        Log.i(TAG, "Request Update");
                        //go to home page
                        Intent i = new Intent(CONTEXT, MainScreenActivity.class);
                        CONTEXT.startActivity(i);
                        ((Activity) CONTEXT).finish();
                    }
                    public void handleFault(BackendlessFault error) {Log.e(TAG, error.getMessage());}
                });
            }
            public void handleFault(BackendlessFault error) {}
        });


    }

    public Map<String,TreeSet<String>>[] getAllShiftAble()
    {
        //put all the shifts at the able set
        UserShiftsPattern shiftsPattern = UserShiftsPattern.getInstance(COMPANY, CONTEXT);
        Map<String,TreeSet<String>>[] allShiftAble = new Map[7];
        for (int i = 0; i<daysRequestsArray.length; i++)
        {
            //get the shift pattern for this day - and make String set
            TreeSet<Shift> dayPattern = shiftsPattern.getDayShifts(i);
            TreeSet<String> able = new TreeSet<>();
            for (Shift temp : dayPattern)
            {
                able.add(temp.toTimeString());
            }
            allShiftAble[i] = new HashMap<>();
            allShiftAble[i].put("able",able);
        }
        return allShiftAble;
    }

    private void loadRequests()
    {
        Backendless.Persistence.of(TABLE_NAME).find(QUERY, new AsyncCallback<BackendlessCollection<Map>>() {
            @Override
            public void handleResponse(BackendlessCollection<Map> data) {
                //check if there is any data
                if (!data.getData().isEmpty()) {
                    //the user is unique - should return only one
                    Map dayRequest = data.getData().get(0);
                    String day;
                    if (!dayRequest.isEmpty()) {
                        //user already sent requests
                        if (dayRequest.get("creator").equals("1"))
                        {
                            RelativeLayout loadingCircle = (RelativeLayout)((Activity)CONTEXT).findViewById(R.id.shiftReq_loadingPanel);
                            loadingCircle.setVisibility(View.GONE);
                            new AlertDialog.Builder(CONTEXT)
                                    .setMessage("You already sent requests for next week... Come back at Sunday!")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent i = new Intent(CONTEXT,MainScreenActivity.class);
                                            CONTEXT.startActivity(i);
                                            ((Activity)CONTEXT).finish();
                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        }
                        //this is the first time this user send req OR manager loading requests
                        else {
                            daysRequestsArray = getAllShiftAble();
                            if (User.permission.equals("1")) afterLoading();
                        }
                    }
                }
            }
            @Override
            public void handleFault(BackendlessFault backendlessFault) {Log.e(TAG, backendlessFault.getMessage());}
        });

    }


    private void afterLoading()
    {
        LinkedHashMap<String, Map<String, TreeSet<String>>> allDaysShift = new LinkedHashMap<>();
        //update ShiftRequestsActivity daysReqArray
        Map<String, TreeSet<String>>[] daysReqArray = ShiftRequestsActivity.getDaysReqArray();
        for (int i = 0; i<daysReqArray.length; i++)
        {
            //put data by day
            daysReqArray[i]= getDayRequests(i);
            allDaysShift.put(daysNameArray[i], daysReqArray[i]);
        }
        //update list view
        ExpandableListView newListView = ShiftRequestsActivity.getListView();
        newListView.setAdapter(new ShiftRequestsAdapter(allDaysShift, CONTEXT));

        RelativeLayout loadingCircle = (RelativeLayout)((Activity)CONTEXT).findViewById(R.id.shiftReq_loadingPanel);
        loadingCircle.setVisibility(View.GONE);
    }

    private Map<String, TreeSet<String>> getDayRequests(int day)
    {
        return new HashMap<>(daysRequestsArray[day]);
    }


}
