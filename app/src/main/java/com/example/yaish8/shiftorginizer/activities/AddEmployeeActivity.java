package com.example.yaish8.shiftorginizer.activities;
/**
 * Created by Levona
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.GoHomeClickListener;
import com.example.yaish8.shiftorginizer.Utils.user.User;
import com.example.yaish8.shiftorginizer.Utils.emailing.SendMail;

import java.util.HashMap;
import java.util.Map;


public class AddEmployeeActivity extends AppCompatActivity {
    EditText firstName,lastName,id,email,address,bDay,phone;
    TextView helloHeader;
    RelativeLayout loading;
    String nameOfOwner, userName ,userPermission, firstConnection, ownerId, company, tableName;
    ImageView homeIcon;
    final String TAG = "AddEmplActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addemployee);
        //initializing
        userPermission = User.permission;
        // User user =(User) getIntent().getSerializableExtra("user");
        userName = User.userName;
        if ("MainScreenActivity".equals(getIntent().getStringExtra("intentFrom"))){
            System.out.println("Yeahhhhh!!!!");
            firstConnection = "yes";
        }
        else if ("0".equals(User.permission)) {
            firstConnection = User.firstConnection;
        }
        else if ("1".equals(User.permission)) {
            Intent i = new Intent(this,MainScreenActivity.class);
            startActivity(i);
        }
        System.out.println("userName = "+User.userName);
        System.out.println(firstConnection+" First connection");
        if ("no".equals(firstConnection)){
            Intent i = new Intent(this,MainScreenActivity.class);
            i.putExtra("userPermission",userPermission);
            startActivity(i);
            this.finish();
        } else if ("yes".equals(firstConnection)){
            changeThisUserFirstConnection();
        }
        tableName="AdminUsers";
        firstName = (EditText)findViewById(R.id.addEmp_firstNameEdit);
        lastName  = (EditText)findViewById(R.id.addEmp_lastNameEdit);
        id        = (EditText)findViewById(R.id.addEmp_idEdit);
        email     = (EditText)findViewById(R.id.addEmp_emailEdit);
        address   = (EditText)findViewById(R.id.addEmp_addressEdit);
        bDay      = (EditText)findViewById(R.id.addEmp_birthdayEdit);
        phone     = (EditText)findViewById(R.id.addEmp_phoneEdit);
        helloHeader = (TextView)findViewById(R.id.addEmp_helloHeader);
        loading = (RelativeLayout)findViewById(R.id.addEmp_loadingPanel);
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.SHARED_PREFERENCES_NAME),MODE_PRIVATE);

        homeIcon = (ImageView) findViewById(R.id.homeIcon_addemp);
        homeIcon.setOnClickListener(new GoHomeClickListener(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        helloHeader.setText("Hello, "+User.firstName);
        nameOfOwner = User.firstName+" "+User.lastName;
        ownerId = User.ownerId;
        company = User.company;
        loading.setVisibility(View.GONE);
    }

    public void addEmp(View v){
        String firstLast_NameRegex = "^[a-zA-Z]{2,12}$"; //regex for making first and last name only letters and between 2-12 chars
        String emailRegex = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        String firstName = this.firstName.getText().toString();
        String id = this.id.getText().toString(); //getting the text from the editText
        String phone = this.phone.getText().toString();
        String lastName = this.lastName.getText().toString();
        String email = this.email.getText().toString();
        String address = this.address.getText().toString();
        String bDay = this.bDay.getText().toString();
        String[] fields = {firstName,lastName,phone,email,id,address,bDay,phone};

        //validations
        if (!isEmptyFields(fields)){
            Toast.makeText(this,"Fields can't stay empty",Toast.LENGTH_LONG).show();
            return;
        }
        if (!(email.matches(emailRegex))){
            Toast.makeText(this,"Email is not Valid!",Toast.LENGTH_SHORT).show();
            return;
        }
        if (id.length()!=9){ //check that the id is 9 numbers - not more and not less..
            Toast.makeText(this,"ID must be 9 digits only!",Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.length()!=10){ //check that the phone is 10 numbers - not more and not less..
            Toast.makeText(this,"Phone Number must be 10 digits only!",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(firstName.matches(firstLast_NameRegex))){ //adding the regex
            Toast.makeText(this,"First name must contain between 2-12 letters!",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(lastName.matches(firstLast_NameRegex))){
            Toast.makeText(this,"Last name must contain between 2-12 letters!",Toast.LENGTH_SHORT).show();
            return;
        }
        //adding Employee to Backendless.. with a method wrote downstairs (addUser())...
        addUser(firstName,lastName,id, this.email.getText().toString());
    }

    private void addUser(final String firstname, final String lastname, final String id, final String email) {
        loading.setVisibility(View.VISIBLE);
        Map user = new HashMap();
        //Backendless have a builtin method for creating users:
        //BackendlessUser user = new BackendlessUser();
        user.put("userName",firstname + "_" + lastname);
        user.put("ID", id);
        user.put("phoneNumber", phone.getText().toString());
        user.put("password", id);
        user.put("firstName", firstname);
        user.put("lastName", lastname);
        user.put("email", email);
        user.put("permission", 1);
        user.put("address", address.getText().toString());
        user.put("birthDay", bDay.getText().toString());
        user.put("company", this.company);
        user.put("ownerId", this.ownerId);
        //
        Backendless.Persistence.of("EmployeeUsers").save(user, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map map) {
                Toast.makeText(AddEmployeeActivity.this,"Added!!",Toast.LENGTH_LONG).show();
                clearTexts();
                loading.setVisibility(View.GONE);
                //Creating SendMail object
                String subject = AddEmployeeActivity.this.nameOfOwner+" Added you to his company!!";
                //add App link to download for the employee... later...
                String message = "Welcome to ShiftOrginizer Application!! \n Your Username is: "+firstname + "_" + lastname + "\nYour password is your ID: "+id;
                new SendMail(AddEmployeeActivity.this, email, subject, message).execute();
                //create row for the employ in the request table
                Map userReq = new HashMap();
                userReq.put("userName", firstname + "_" + lastname);
                //mark as create by manager for first time
                userReq.put("creator", User.permission+"f");
                Backendless.Persistence.of(User.company+"EmployeesRequests").save(userReq, new AsyncCallback<Map>() {
                    public void handleResponse(Map map) {Log.i(TAG, "added to requests table");}
                    public void handleFault(BackendlessFault backendlessFault) {Log.e(TAG, backendlessFault.getMessage());}
                });
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                loading.setVisibility(View.GONE);
                Toast.makeText(AddEmployeeActivity.this,backendlessFault.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    public void doneEmp (View v){

        // when the user finished adding Emp, this button take to the next screen.
        Intent i = new Intent(this, MainScreenActivity.class); //Class should be the next screen Activity
        i.putExtra("ownerId",ownerId);
        startActivity(i);
        this.finish();
    }
    public void clearEmp (View v){
        clearTexts();
    }

    private void clearTexts(){ // resets all the editext.
        firstName.setText("");
        lastName.setText("");
        id.setText("");
        email.setText("");
        address.setText("");
        bDay.setText("");
        phone.setText("");
    }

    private void changeThisUserFirstConnection(){
        BackendlessDataQuery query = new BackendlessDataQuery("userName = '"+userName+"'");
        Backendless.Persistence.of("AdminUsers").find(query, new BackendlessCallback<BackendlessCollection<Map>>() {
            @Override
            public void handleResponse(BackendlessCollection<Map> mapBackendlessCollection) {
                Map user = mapBackendlessCollection.getData().get(0);
                user.put("firstConnection","no");
                Backendless.Persistence.of("AdminUsers").save(user, new BackendlessCallback<Map>() {
                    @Override
                    public void handleResponse(Map map) {
                        System.out.println("saved!");
                    }
                });
            }
        });
    }
    private boolean isEmptyFields(String[] fields){
        boolean empty = true;
        for (int i = 0; i<fields.length;i++){
            if (fields[i].isEmpty()){
                empty =  false;
            }
        } return empty;
    }

}
