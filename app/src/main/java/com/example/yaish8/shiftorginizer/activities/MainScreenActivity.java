package com.example.yaish8.shiftorginizer.activities;
/**
 * Created by Nimrod
 */
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.yaish8.shiftorginizer.Utils.Adapters.ShiftMainScreenAdapter;
import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.Dates;
import com.example.yaish8.shiftorginizer.Utils.user.User;
import com.example.yaish8.shiftorginizer.Utils.user.UserShiftsPattern;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MainScreenActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    TextView header;
    Spinner spinner;
    ExpandableListView listView;
    private String[] thisWeekDates, nextWeekDates, daysNameArray;
    String permission,lastSunday,nextSunday,nextSundayReadable,lastSundayReadable, thisSaturday, nextSaturday;
    RelativeLayout loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainscreen);
        header = (TextView)findViewById(R.id.mainscreen_header);
        spinner = (Spinner)findViewById(R.id.mainscreen_spinner);
        listView = (ExpandableListView)findViewById(R.id.mainscreen_listView);
        loading=(RelativeLayout)findViewById(R.id.mainScreen_loadingPanel);
        sharedPreferences = getSharedPreferences(getResources().getString(R.string.SHARED_PREFERENCES_NAME),MODE_PRIVATE);
        permission = User.permission;
        daysNameArray = getResources().getStringArray(R.array.days_array);

        thisWeekDates = Dates.getThisWeekDays();
        nextWeekDates = Dates.getNextWeekDays();
        //gets the lastSunday and nextSunday
        DateFormat df=new SimpleDateFormat("ddMMyyyy");
        lastSundayReadable = thisWeekDates[0];
        lastSunday = lastSundayReadable.replaceAll("/","");// This past Sunday [ May include today ]
        nextSundayReadable = nextWeekDates[0];
        nextSunday = nextSundayReadable.replaceAll("/","");

        //get this & next week Saturday
        thisSaturday = thisWeekDates[6];
        nextSaturday = nextWeekDates[6];
    }

    @Override
    protected void onStart() {
        super.onStart();
        //making the spinner and Adapter
        List spinnerList = new ArrayList();
        spinnerList.add("Week Of: "+lastSundayReadable+" - "+thisSaturday);
        spinnerList.add("Week Of: "+nextSundayReadable+" - "+nextSaturday);
        //android.R.layout.simple_spinner_dropdown_item
        ArrayAdapter spinnerAdapter = new ArrayAdapter(this,R.layout.spinner_item_mainscreen , spinnerList);
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(itemSelectedListener());
        header.setText("Welcome, "+ User.firstName);
    }

    //on item selected of spinnerView:
    private AdapterView.OnItemSelectedListener itemSelectedListener(){
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //checking which item selected:
                if (spinner.getSelectedItem().toString().equalsIgnoreCase("week of: "+lastSundayReadable+ " - "+thisSaturday)){
                    loading.setVisibility(View.VISIBLE);
                     //getting the shifts from backendless and showing them in listView
                    getWorkArrange(User.company+lastSunday, thisWeekDates);
                }else if (spinner.getSelectedItem().toString().equalsIgnoreCase("week of: "+nextSundayReadable+ " - "+nextSaturday)){
                    loading.setVisibility(View.VISIBLE);
                    getWorkArrange(User.company+nextSunday, nextWeekDates);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        };
    }

    private void getWorkArrange(String tableName , final String[] weekDates)
    {
        Backendless.Persistence.of(tableName).find(new AsyncCallback<BackendlessCollection<Map>>() {
            @Override
            public void handleResponse(BackendlessCollection<Map> data) {
                //Linked Hashmap of<String day-name , String[] hours+employees>
                //hours+employees should be: {"HH:mm-HH:mm#names,names,names","HH:mm-HH:mm#names,names,names"} and so on..
                LinkedHashMap<String, String[]> shifts = new LinkedHashMap<>();
                List<Map> allDays = data.getData();

                //create an array for each say
                String[] sunday=null, monday=null, tuesday=null, wednesday=null, thursday=null,
                        friday=null, saturday=null, shiftWithEmploy = null;
                if(!allDays.isEmpty())
                {
                    for (Map correctDay : allDays)
                    {
                        String dayData = ((String)correctDay.get("hoursEmployees"));
                        String day = (String) correctDay.get("day");
                        if (dayData!=null)if (!dayData.isEmpty()) {
                            //split to hours#employees
                            shiftWithEmploy = ((String) correctDay.get("hoursEmployees")).split("!");
                        }
                        else
                            shiftWithEmploy = new String[]{"No Shifts#"};

                        switch (day.toLowerCase()){ //checking if the day is: sunday or monday and so on..
                            case "sunday": sunday = shiftWithEmploy;break;
                            case "monday":monday=shiftWithEmploy; break;
                            case "tuesday":tuesday=shiftWithEmploy;break;
                            case "wednesday":wednesday=shiftWithEmploy; break;
                            case "thursday": thursday=shiftWithEmploy;break;
                            case "friday": friday=shiftWithEmploy;break;
                            case "saturday": saturday=shiftWithEmploy;break;
                        }
                    }

                    //put in order
                    shifts.put("Sunday",sortedArray(sunday));
                    shifts.put("Monday",sortedArray(monday));
                    shifts.put("Tuesday",sortedArray(tuesday));
                    shifts.put("Wednesday",sortedArray(wednesday));
                    shifts.put("Thursday",sortedArray(thursday));
                    shifts.put("Friday",sortedArray(friday));
                    shifts.put("Saturday",sortedArray(saturday));

                    listView.setAdapter(new ShiftMainScreenAdapter(MainScreenActivity.this,shifts, weekDates));
                    listView.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.GONE);
                }
            }

            @Override
            public void handleFault(BackendlessFault error) {
                //there isn't an arrange yet
                if (error.getCode().equals("1009"))
                {
                    listView.setVisibility(View.GONE);
                    loading.setVisibility(View.GONE);
                }

                else
                    Log.e("Main",error.getMessage());
            }
        });
    }

    //getting a String[] that need to be sorted and sorts it:
    private String[] sortedArray(String [] array){
        Arrays.sort(array);
        return array;
    }
    //creating option menu:
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (permission.equals("0")) { //checking what permission - admin or employee and than decide which menu to inflate.. 0 is admin
            inflater.inflate(R.menu.admin_menu, menu);
        }else if (permission.equals("1")){//1 is employee
            inflater.inflate(R.menu.employee_menu,menu);
        }
        return true;
    }
    //inside menu: when item has selected:
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (permission.equals("0")) { //again checks permission:
            switch (item.getItemId()){
                case R.id.menu_AdminAddEmployee:
                    goToDifferentActivity(AddEmployeeActivity.class);
                    break;
                case R.id.menu_AdminContactList:
                    goToDifferentActivity(ContactListAdminActivity.class);
                    break;
                case R.id.menu_AdminEditProfile:
                    goToDifferentActivity(EditProfileActivity.class);
                    break;
                case R.id.menu_AdminEditShifts:
                    goToDifferentActivity(ShiftPatternActivity.class);
                    break;
                case R.id.menu_AdminWorkArrange:
                    goToDifferentActivity(CreateWorkArrangeActivity.class);
                    break;
                case R.id.menu_AdminlogOut:
                    UserShiftsPattern.getInstance(User.company,this).preventShifts();
                    sharedPreferences.edit().clear().commit();
                    goToDifferentActivity(LoginActivity.class);
                    MainScreenActivity.this.finish();
                    break;
            }
        }else if (permission.equals("1")){
            switch (item.getItemId()){
                case R.id.menu_employeeContactList:
                    goToDifferentActivity(ContactListEmployeeActivity.class);
                    break;
                case R.id.menu_employeeEditProfile:
                    goToDifferentActivity(EditProfileActivity.class);
                    break;
                case R.id.menu_employeeLogOut:
                    UserShiftsPattern.getInstance(User.company,this).preventShifts();
                    sharedPreferences.edit().clear().commit();
                    goToDifferentActivity(LoginActivity.class);
                    MainScreenActivity.this.finish();
                    break;
                case R.id.menu_employeeShiftRequest:
                    goToDifferentActivity(ShiftRequestsActivity.class);
                    break;
            }

        }
        return super.onOptionsItemSelected(item);
    }
    //creating a method that would be easy to move to different activities without always typing it again..
    private void goToDifferentActivity(Class whichClass){ //Getting a Class to move to..
        Intent i = new Intent(this,whichClass);
        i.putExtra("intentFrom","MainScreenActivity");
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle("Are you sure you want to exit app?")
                .setMessage("Maybe that's not what you really wanted ✌")
                .setNegativeButton("No",null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainScreenActivity.this.finish();
                    }
                }).show();

    }
}
