package com.example.yaish8.shiftorginizer.activities;
/**
 * Created by Levona
 */

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.user.EmployeeUsers;
import com.example.yaish8.shiftorginizer.Utils.GoHomeClickListener;
import com.example.yaish8.shiftorginizer.Utils.user.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ContactListAdminActivity extends AppCompatActivity {
    ImageView homeIcon;
    Spinner spinner;
    RelativeLayout contact_all;
    List<String> spinnerList = new ArrayList();
    List<EmployeeUsers> employessList = new ArrayList<>();
    private Object Iterator;
    TextView firstNameTv,lastNameTv,phoneNumberTv,IdTv,EmailTv,AddressTv,BirthDayTv;
    EmployeeUsers chosenEmployee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactlist_admin);
        spinner=(Spinner)findViewById(R.id.contact_spinner);
        contact_all=(RelativeLayout)findViewById(R.id.contact_all);
        firstNameTv   = (TextView) findViewById(R.id.contactList_firstName);
        lastNameTv    = (TextView) findViewById(R.id.contactList_lastName);
        phoneNumberTv = (TextView) findViewById(R.id.contactList_phoneNumber);
        IdTv          = (TextView) findViewById(R.id.contactList_id);
        EmailTv       = (TextView) findViewById(R.id.contactList_email);
        AddressTv     = (TextView) findViewById(R.id.contactList_address);
        BirthDayTv    = (TextView) findViewById(R.id.contactList_birthDay);
        homeIcon = (ImageView)findViewById(R.id.homeIcon);
        homeIcon.setOnClickListener(new GoHomeClickListener(this));
        contact_all.setVisibility(View.GONE);
        getAllUsers();
    }

    private void getAllUsers(){
        String whereClause = "company = '"+ User.company+"'";
        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        dataQuery.setWhereClause( whereClause );


        Backendless.Data.of( EmployeeUsers.class ).find(dataQuery,
        new AsyncCallback<BackendlessCollection<EmployeeUsers>>() {
            @Override
            public void handleResponse( BackendlessCollection<EmployeeUsers> employess ){
                Iterator<EmployeeUsers> employeeIterator = employess.getCurrentPage().iterator();
                while( employeeIterator.hasNext() ) {
                    EmployeeUsers employee = employeeIterator.next();
                    employessList.add(employee);
                    spinnerList.add(employee.getFirstName()+" "+employee.getLastName());
                }
                createSpinner(spinnerList);
            }
            @Override
            public void handleFault( BackendlessFault backendlessFault ) {
            }
        });


    }

    private void createSpinner(List<String> spinnerList){
        ArrayList<String> mySpinnerItems = new ArrayList<>();
        mySpinnerItems.add("Choose Contact");
        for (String name : spinnerList) mySpinnerItems.add(name);
        ArrayAdapter arrayAdapter = new ArrayAdapter(ContactListAdminActivity.this, R.layout.spinner_item_mainscreen,mySpinnerItems);
        arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(itemSelectedListener());
    }

    private AdapterView.OnItemSelectedListener itemSelectedListener(){
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinner.getSelectedItem().toString().equals("Choose Contact")){
                    contact_all.setVisibility(View.GONE);
                    return;
                }

                chosenEmployee = employessList.get(position-1);
                firstNameTv.setText(chosenEmployee.getFirstName());
                lastNameTv.setText(chosenEmployee.getLastName());
                phoneNumberTv.setText(chosenEmployee.getPhoneNumber());
                IdTv.setText(chosenEmployee.getID());
                EmailTv.setText(chosenEmployee.getEmail());
                AddressTv.setText(chosenEmployee.getAddress());
                BirthDayTv.setText(chosenEmployee.getBirthDay());
                contact_all.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

    }

    public void delete(View v){

        new AlertDialog.Builder(this)
                .setTitle("Remove employee")
                .setMessage("Do you really want to remove " + chosenEmployee.getFirstName())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //when user pressed yes!
                            Backendless.Persistence.of( EmployeeUsers.class ).remove(chosenEmployee,
                            new AsyncCallback<Long>()
                            {
                                public void handleResponse( Long response )
                                {
                                    employessList.remove(chosenEmployee);
                                    spinnerList.remove(chosenEmployee.getFirstName());
                                    chosenEmployee = null;
                                    createSpinner(spinnerList);
                                    contact_all.setVisibility(View.GONE);
                                    Toast.makeText(ContactListAdminActivity.this,"Deleted!!",Toast.LENGTH_SHORT).show();
                                }
                                public void handleFault( BackendlessFault fault )
                                {
                                    // an error has occurred, the error code can be
                                    // retrieved with fault.getCode()
                                    Toast.makeText(ContactListAdminActivity.this,"Couldn't delete employee",Toast.LENGTH_LONG).show();
                                }
                            });
                    }})
                .setNegativeButton("No", null).show();

    }

}
