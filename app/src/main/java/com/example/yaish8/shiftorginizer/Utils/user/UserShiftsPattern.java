package com.example.yaish8.shiftorginizer.Utils.user;
/**
 * Created by Tamar
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.DataStringConvert;
import com.example.yaish8.shiftorginizer.Utils.HttpRequest;
import com.example.yaish8.shiftorginizer.Utils.Shift;
import com.example.yaish8.shiftorginizer.activities.MainScreenActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class UserShiftsPattern {
    private String company;
    private TreeSet<Shift>[] daysShiftPattern = new TreeSet[7];
    private boolean isLoaded = false;
    private String[] daysNameArray;
    private Context context;
    private BackendlessDataQuery query;
    final String TABLE_NAME = "ShiftsPattern", TAG = "UserShiftPattern";

    private static UserShiftsPattern instance = new UserShiftsPattern();

    public static UserShiftsPattern getInstance(String company, Context context) {
        instance.company = company;
        instance.context = context;
        instance.daysNameArray = context.getResources().getStringArray(R.array.days_array);
        //check if shift already loaded
        instance.query = new BackendlessDataQuery("company = '" + company + "'");
        if (!instance.isLoaded) instance.loadAllShifts();
        return instance;
    }

    private UserShiftsPattern() {
        //initialize days array
        for (int i = 0; i < daysShiftPattern.length; i++) {
            daysShiftPattern[i] = new TreeSet<>();
        }
    }

    public TreeSet<Shift> getDayShifts(int i) {
        return new TreeSet<>(daysShiftPattern[i]);
    }

    public void preventShifts() {
        instance = new UserShiftsPattern();
        instance.isLoaded = false;
    }

    public void updateShiftDayPattern(final TreeSet<Shift>[] daysNewPattern) {
        //find the row to update
        Backendless.Persistence.of(TABLE_NAME).find(query, new AsyncCallback<BackendlessCollection<Map>>() {
            Map dayPattern;

            public void handleResponse(BackendlessCollection<Map> data) {
                //check if there is any data
                if (!data.getData().isEmpty()) {
                    dayPattern = data.getData().get(0);
                    for (int i = 0; i < daysNewPattern.length; i++) {
                        //only update if there is a chance that this day have changed
                        if (daysNewPattern[i] != null) {
                            //put new data
                            dayPattern.put(daysNameArray[i].toLowerCase(), DataStringConvert.getDayPatternString(daysNewPattern[i]));
                        }
                    }
                }
                //this is the first time pattern is created
                else {
                    System.out.println("no Data");
                    dayPattern = new HashMap();
                    dayPattern.put("company", company);
                    for (int i = 0; i < daysNewPattern.length; i++) {
                        //if there is any data for this day
                        if (daysNewPattern[i] != null)
                            //put new data
                            dayPattern.put(daysNameArray[i].toLowerCase(), DataStringConvert.getDayPatternString(daysNewPattern[i]));
                        else
                            //if there is no data - put an empty String
                            dayPattern.put(daysNameArray[i].toLowerCase(), "");
                    }
                }
                //save the new data
                Backendless.Persistence.of(TABLE_NAME).save(dayPattern, new AsyncCallback<Map>() {
                    public void handleResponse(Map map) {
                        //update local
                        daysShiftPattern = daysNewPattern;
                        updateUsersRequests();
                    }

                    public void handleFault(BackendlessFault error) {
                        Log.e(TAG, error.getMessage());
                    }
                });
            }

            public void handleFault(BackendlessFault error) {
                Log.e(TAG, error.getMessage());
            }
        });
    }

    private void loadAllShifts() {
        Backendless.Persistence.of(TABLE_NAME).find(query, new AsyncCallback<BackendlessCollection<Map>>() {
            @Override
            public void handleResponse(BackendlessCollection<Map> data) {
                //check if there is any data
                if (!data.getData().isEmpty()) {
                    //the pattern is unique - should return only one map
                    Map dayPattern = data.getData().get(0);
                    if (!dayPattern.isEmpty()) {
                        //load shifts for each day
                        for (int i = 0; i < daysShiftPattern.length; i++) {
                            daysShiftPattern[i] = loadShiftsByDay(dayPattern.get(daysNameArray[i].toLowerCase()));
                        }
                    }

                }
                if (!isLoaded)
                    isLoaded = true;
                else {
                    //go back to home page
                    Intent i = new Intent(context, MainScreenActivity.class);
                    context.startActivity(i);
                    ((Activity) context).finish();
                }
            }

            @Override
            public void handleFault(BackendlessFault error) {
                Log.e(TAG, error.getMessage());
            }
        });
    }

    private TreeSet<Shift> loadShiftsByDay(Object dayPattern) {
        TreeSet<Shift> tempSet = new TreeSet<>();
        Shift tempShift;

        //check if there is any shifts for the selected day
        if (dayPattern != null) if (!((String) dayPattern).isEmpty()) {
            //convert data to Strings Array
            String[] startsTime = DataStringConvert.getStartsTime(dayPattern);
            String[] endsTime = DataStringConvert.getEndsTime(dayPattern);
            int[] employeesNum = DataStringConvert.getEmployeesNumber(dayPattern);

            int length = startsTime.length;
            //create new shift TreeSet from the data
            for (int i = 0; i < length; i++) {
                tempShift = new Shift(startsTime[i], endsTime[i], employeesNum[i]);
                tempSet.add(tempShift);
            }
        }
        return tempSet;
    }

    private void updateUsersRequests() {
        //go to all request table and find all the users the didn't sent request yet
        String url = " https://api.backendless.com/" + context.getString(R.string.VERSION_NAME)
                + "/data/bulk/" + User.company + "EmployeesRequests?where=creator%3D'0'";
        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String response = null, day;
                JSONObject jsonDataToSend = new JSONObject();
                try {
                    jsonDataToSend.put("creator", "0f");
                    HttpRequest httpRequest = new HttpRequest(params[0]);
                    //set method
                    response = httpRequest.
                            //set headers
                                    withHeaders("application-id:" + context.getString(R.string.APP_ID),
                                    "secret-key:" + context.getString(R.string.SECURITY_KEY),
                                    "Content-Type:application/json",
                                    "application-type: REST").
                                    prepare(HttpRequest.Method.PUT).
                                    withData(jsonDataToSend.toString()).
                                    sendAndReadString();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }

                return response;
            }
            @Override
            protected void onPostExecute(String response) {
                //if succeeded - save work arrange
                if (response!=null) {
                    //go to home page
                    Intent i = new Intent(context, MainScreenActivity.class);
                    context.startActivity(i);
                    ((Activity) context).finish();
                }
                else
                    Log.e(TAG, "no");
            }
        };
        task.execute(url);
    }
}
