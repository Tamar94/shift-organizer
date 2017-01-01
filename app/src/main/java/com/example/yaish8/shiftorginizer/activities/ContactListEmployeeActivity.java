package com.example.yaish8.shiftorginizer.activities;
/**
 * Created by Levona
 */

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.Adapters.UserContactAdapter;
import com.example.yaish8.shiftorginizer.Utils.user.EmployeeUsers;
import com.example.yaish8.shiftorginizer.Utils.GoHomeClickListener;
import com.example.yaish8.shiftorginizer.Utils.user.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ContactListEmployeeActivity extends AppCompatActivity {
    ListView listView;
    List<EmployeeUsers> employessList = new ArrayList<>();
    ImageView homeIcon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactlist_employee);
        listView=(ListView)findViewById(R.id.listView_user);
        homeIcon=(ImageView)findViewById(R.id.homeIconEmployee);
        homeIcon.setOnClickListener(new GoHomeClickListener(this));
        setNamesAndNumbers();
    }

    private void setNamesAndNumbers(){
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
                        }
                        listView.setAdapter(new UserContactAdapter(ContactListEmployeeActivity.this,employessList));
                    }
                    @Override
                    public void handleFault( BackendlessFault backendlessFault ) {
                    }

        });
    }
}
