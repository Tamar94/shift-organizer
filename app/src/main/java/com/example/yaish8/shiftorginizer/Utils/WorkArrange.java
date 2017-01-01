package com.example.yaish8.shiftorginizer.Utils;
/**
 * Created by Tamar
 */
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
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
import com.example.yaish8.shiftorginizer.Utils.Adapters.CreateWorkArrangeAdapter;
import com.example.yaish8.shiftorginizer.Utils.emailing.EmailSendingToAll;
import com.example.yaish8.shiftorginizer.Utils.user.User;
import com.example.yaish8.shiftorginizer.Utils.user.UserRequests;
import com.example.yaish8.shiftorginizer.activities.CreateWorkArrangeActivity;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class WorkArrange {
    private TreeSet<Shift>[] daysShiftArray;
    //array of map - value: shift, key - employees
    private Map<String, String>[] allShiftsWithChosenEmployees;
    private final String[] DAYS_NAME_ARRAY;
    private final Context CONTEXT;
    private boolean noWorkArrange;
    private  final String TABLE_NAME, TAG = "WorkArrange";

    public WorkArrange(Context context,TreeSet<Shift>[] daysShiftArray,String week)
    {
        this.daysShiftArray = daysShiftArray;
        allShiftsWithChosenEmployees = new Map[7];
        for (int i = 0; i<allShiftsWithChosenEmployees.length; i++)
        {
            allShiftsWithChosenEmployees[i] = new TreeMap<>();
        }
        CONTEXT = context;
        DAYS_NAME_ARRAY = context.getResources().getStringArray(R.array.days_array);
        TABLE_NAME = User.company+week;
        noWorkArrange = false;

        setAllUsersRequests();

    }

    public void sendNewArrange(Map<String, String>[] newWorkArrange)
    {
       Map<String, String> dayArrange;
        for (int i=0; i<newWorkArrange.length; i++)
       {
           //get this day arrange
           dayArrange = newWorkArrange[i];
           StringBuilder sb = new StringBuilder();
           if (dayArrange!=null)if (!dayArrange.isEmpty())
           {
               Set<Map.Entry<String,String>> entrySet = dayArrange.entrySet();
               Iterator iterator = entrySet.iterator();
               Map.Entry<String, String> temp;
               //put all the shift & chosen employees for this day in String
               while (iterator.hasNext())
               {
                   temp = (Map.Entry<String, String>)iterator.next();
                   sb.append(temp.getKey()+"#"+temp.getValue()+"!");
               }
               sb.deleteCharAt(sb.length()-1);
               //save this day
               saveDayArrange(sb.toString(), i);
               sb = new StringBuilder();
           }
       }

        EmailSendingToAll.sendEmailToAll(CONTEXT);
    }

    private void saveDayArrange(final String dayArrange, int dayIndex)
    {
        BackendlessDataQuery query = new BackendlessDataQuery("day='"+DAYS_NAME_ARRAY[dayIndex]+"'");
        //find the day to update
        Backendless.Persistence.of(TABLE_NAME).find(query, new AsyncCallback<BackendlessCollection<Map>>() {
            @Override
            public void handleResponse(BackendlessCollection<Map> data) {
                if (!data.getData().isEmpty())
                {
                    Map newDayArrange = data.getData().get(0);
                    //update the new day arrange
                    newDayArrange.put("hoursEmployees", dayArrange);
                    Backendless.Persistence.of(TABLE_NAME).save(newDayArrange, new AsyncCallback<Map>() {
                        public void handleResponse(Map map) {Log.i(TAG, map.get("day")+" update");}
                        public void handleFault(BackendlessFault backendlessFault) {Log.e(TAG, backendlessFault.getMessage());}
                    });
                }
            }
            @Override
            public void handleFault(BackendlessFault backendlessFault) {}
        });
    }

    private void setAllUsersRequests()
    {
        //go to all request table and find all the users the didn't sent request yet
        String url = " https://api.backendless.com/" + CONTEXT.getString(R.string.VERSION_NAME)
                + "/data/bulk/" + User.company+"EmployeesRequests?where=creator%3D'0f'";
        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String response = null, day;
                UserRequests userRequests = new UserRequests(null, User.company, CONTEXT);
                Map<String,TreeSet<String>>[] allShiftsAble = userRequests.getAllShiftAble();
                JSONObject jsonDataToSend = new JSONObject();
                try {
                    jsonDataToSend.put("creator","0");
                    //put al shifts able
                    for (int i =0; i<allShiftsAble.length; i++)
                    {
                        day = DAYS_NAME_ARRAY[i].toLowerCase();
                        jsonDataToSend.put(day+"Able", DataStringConvert.getRequestsString(allShiftsAble[i].get("able")));
                        jsonDataToSend.put(day+"Block","");
                    }

                    HttpRequest httpRequest = new HttpRequest(params[0]);
                    //set method
                    response = httpRequest.
                            //set headers
                                    withHeaders("application-id:" + CONTEXT.getString(R.string.APP_ID),
                                    "secret-key:" + CONTEXT.getString(R.string.SECURITY_KEY),
                                    "Content-Type:application/json",
                                    "application-type: REST").
                                    prepare(HttpRequest.Method.PUT).
                                    withData(jsonDataToSend.toString()).
                                    sendAndReadString();
                }catch (Exception e)
                    {Log.e(TAG, e.getMessage());}

                return response;
            }

            @Override
            protected void onPostExecute(String response) {
                //if succeeded - load work arrange
                if (response!=null)
                    loadWorkArrange();
                else
                    Log.e(TAG, "no");
            }
        };

        task.execute(url);
    }

    private void loadWorkArrange()
    {
        Backendless.Persistence.of(TABLE_NAME).find(new AsyncCallback<BackendlessCollection<Map>>() {
            @Override
            public void handleResponse(BackendlessCollection<Map> data) {
                List<Map> allDays = data.getData();
                TreeSet<Shift> dayPattern;
                //if there is any work arrange for next week
                if (!allDays.isEmpty())
                {
                    int dayIndex;
                    String[] keyAndValue, shiftWithEmploy;
                    for (Map day : allDays)
                    {
                        //check in each day this shift is
                        dayIndex = findDayIndex((String)day.get("day"));
                        //get day pattern
                        dayPattern = daysShiftArray[dayIndex];
                        String dayData = ((String)day.get("hoursEmployees"));
                        if (dayData!=null)if (!dayData.isEmpty()){
                            shiftWithEmploy = ((String)day.get("hoursEmployees")).split("!");
                            for (int i=0;i<shiftWithEmploy.length;i++)
                            {
                                //split to key - shift hours, and value - employees name
                                keyAndValue = shiftWithEmploy[i].split("#");
                                //check that this shift didn't change
                                if (dayPattern.toString().contains(keyAndValue[0].trim()))
                                    allShiftsWithChosenEmployees[dayIndex].put(keyAndValue[0], keyAndValue[1]);
                            }
                        }
                    }
                }
                //get users requests
               loadRequestsForEachShift();
            }

            @Override
            public void handleFault(BackendlessFault error) {
                //there isn't an arrange yet - and this is create work arrange activity
                if (error.getCode().equals("1009"))
                {
                    //create new table, row for each day
                    Map<String, String> newDay = new HashMap<>();
                    newDay.put("day", DAYS_NAME_ARRAY[0]);
                    newDay.put("hoursEmployees", "");
                    createDay(newDay, 0);
                 }

                else
                    Log.e(TAG,error.getMessage());
            }
        });
    }

    private void createDay(final Map newDay, final int index)
    {
        Backendless.Persistence.of(TABLE_NAME).save(newDay, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map map) {
                //if saturday - load requests
               if (map.get("day").equals(DAYS_NAME_ARRAY[6]))
               {
                       loadRequestsForEachShift();
               }
               //create the next day
               else
               {
                   Map<String,String> nextDay = new HashMap<>();
                   nextDay.put("day",DAYS_NAME_ARRAY[index+1]);
                   nextDay.put("hoursEmployees", "");
                   createDay(nextDay, index+1);
               }
            }
            @Override
            public void handleFault(BackendlessFault backendlessFault) {Log.e(TAG, backendlessFault.getMessage());}
        }) ;
    }

    private void loadRequestsForEachShift()
    {
        //get all user requests
        Backendless.Persistence.of(User.company+"EmployeesRequests").find(new AsyncCallback<BackendlessCollection<Map>>() {
            @Override
            public void handleResponse(BackendlessCollection<Map> data) {
                List<Map> users = data.getData(); //all users requests
                String userName, day;
                StringBuilder ableUsers = new StringBuilder();
                //key - day, value - map: key-shift, value-users that can work
                LinkedHashMap<String,Map<Shift, String>> daysShiftsWithReq = new LinkedHashMap<>();
                Map<Shift, String> shiftsWithReq;
                //if there is any data
                if (!users.isEmpty())
                {
                    //go for each day
                    for (int i=0;i<DAYS_NAME_ARRAY.length; i++)
                    {
                        day = DAYS_NAME_ARRAY[i];
                        shiftsWithReq = new HashMap<>();
                        //go for each shift
                        for (Shift temp : daysShiftArray[i])
                        {
                            //go for each user
                            for (Map user : users)
                            {
                                //transfer user name to first name + last name
                                userName = (String)user.get("userName");
                                int index = userName.indexOf("_");
                                userName = userName.substring(0,1).toUpperCase() + userName.substring(1,index) + " "
                                        +userName.substring(index+1,index+2).toUpperCase()+userName.substring(index+2);
                                //if user have any able shifts for this day
                                if (user.get(day.toLowerCase()+"Able")!=null)
                                    //if the correct shift is one of them
                                    if(((String)user.get(day.toLowerCase()+"Able")).contains(temp.toTimeString()))
                                        //put user in able users
                                        ableUsers.append(userName+",");
                            }
                            //put in map
                            shiftsWithReq.put(temp, ableUsers.toString());
                            //clean ableUsers
                            ableUsers = new StringBuilder();
                        }
                        //put in LinkedHashMap
                        daysShiftsWithReq.put(day, shiftsWithReq);
                    }
                }
                //set List view
                ExpandableListView listView = CreateWorkArrangeActivity.getListView();
                listView.setAdapter(new CreateWorkArrangeAdapter(daysShiftsWithReq,CONTEXT,allShiftsWithChosenEmployees));
                RelativeLayout loading = (RelativeLayout)((Activity)CONTEXT).findViewById(R.id.createArr_loadingPanel);
                loading.setVisibility(View.GONE);
            }

            @Override
            public void handleFault(BackendlessFault error) {
                Log.e(TAG,error.getMessage());
            }
        });
    }

    private int findDayIndex(String day)
    {
        for (int i = 0; i<DAYS_NAME_ARRAY.length; i++)
        {
            //find the right index for the selected day
            if (DAYS_NAME_ARRAY[i].equals(day)) return i;
        }
        return -1;
    }
}
