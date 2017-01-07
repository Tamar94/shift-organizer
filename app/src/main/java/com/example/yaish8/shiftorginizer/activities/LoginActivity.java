package com.example.yaish8.shiftorginizer.activities;
/**
 * Created by Nimrod
 */
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.user.User;
import com.example.yaish8.shiftorginizer.Utils.user.UserShiftsPattern;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    EditText username,password;
    RelativeLayout loadingCircle;
    SharedPreferences sharedPreferences;
    String firstConnection, userName;
    String TABLE_NAME;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //initializing..
        username=(EditText)findViewById(R.id.login_userEdit);
        password=(EditText)findViewById(R.id.login_passEdit);
        loadingCircle=(RelativeLayout)findViewById(R.id.login_loadingPanel);
        //connecting to Backendless
        Backendless.initApp(this, getResources().getString(R.string.APP_ID),
                getResources().getString(R.string.SECURITY_KEY), getResources().getString(R.string.VERSION_NAME));
    }
    @Override
    protected void onStart() {
        super.onStart();
        //getting the username and password from shared preferences
        sharedPreferences = getSharedPreferences(getResources().getString(R.string.SHARED_PREFERENCES_NAME),MODE_PRIVATE);
        userName = sharedPreferences.getString("userName","");
        String password = sharedPreferences.getString("password","");
        boolean isConnected = sharedPreferences.getBoolean("isConnected",false);
        //if the user didn't LogOut he will jump right to the next Activity
        if (isConnected){
            TABLE_NAME=sharedPreferences.getString("tableName","");
            if ("EmployeeUsers".equals(TABLE_NAME)) {
                loginEmployee(userName, password);
            }else if ("AdminUsers".equals(TABLE_NAME)){
                AdminLogin(userName,password);
            }
        }else{loadingCircle.setVisibility(View.GONE);} //else - stop the loading icon and make it's visibility GONE..
    }
    //"Login" Button
    public void login(View v){
        loadingCircle.setVisibility(View.VISIBLE); //starts the loading Icon rotate
        //getting the text from Edittext that the user input_login
        userName = this.username.getText().toString();
        String password=this.password.getText().toString();
        if (userName.length()<2 || password.length()<4){ //checking the length
            Toast.makeText(this,"username or password not long enough",Toast.LENGTH_LONG).show();
            return;
        }
        loginEmployee(userName,password);
    }
    //"Back" Button
    public void goToRegister(View v){
        Intent i = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(i);
        LoginActivity.this.finish();
    }
    //try logging as employee
    private void loginEmployee(final String userName, final String password){
        //search in backendless by the table name: EmployeeUsers and username = typed username
        final BackendlessDataQuery query = new BackendlessDataQuery("userName = '"+userName+"'");
        Backendless.Persistence.of("EmployeeUsers").find(query, new AsyncCallback<BackendlessCollection<Map>>() {
            @Override
            //if found!
            public void handleResponse(BackendlessCollection<Map> mapBackendlessCollection) {
                if (mapBackendlessCollection.getData().isEmpty()){ //if the is NO data inside
                    AdminLogin(userName,password);
                    loadingCircle.setVisibility(View.GONE);
                    return;
                }
                Map user = mapBackendlessCollection.getData().get(0); // get the first MAP DATA of the user that was found
                String userPass = user.get("password").toString();
                if (userPass.equals(password)){ //checking if the password matches inside backendless and typed one
                    sharedPreferences.edit().putString("userName",userName).putString("password",password).putBoolean("isConnected",true).putString("tableName","EmployeeUsers").commit();
                    LoginActivity.this.TABLE_NAME = "EmployeeUsers"; //initialize table name
                    SetCurrentUser();//setting all instances of USER object inside USER class
                }
                else{ AdminLogin(userName,password); }//if the username and password doesnt match - check inside the admin table
            }
            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                AdminLogin(userName,password); }//if the User is not found in table backendless
        });
    }
    //try logging as Admin
    private void AdminLogin(final String userName, final String password){//search in AdminUsers table
        BackendlessDataQuery query = new BackendlessDataQuery("userName = '"+userName+"'"); //making a query
        Backendless.Persistence.of("AdminUsers").find(query, new AsyncCallback<BackendlessCollection<Map>>() {
            @Override
            public void handleResponse(BackendlessCollection<Map> mapBackendlessCollection) {//if the userName found
                if (mapBackendlessCollection.getData().isEmpty()){ //checking for empty data..
                    Toast.makeText(LoginActivity.this,"User Don't exists OR Password is Wrong",Toast.LENGTH_SHORT).show();
                    loadingCircle.setVisibility(View.GONE);
                    return;
                }
                Map user = mapBackendlessCollection.getData().get(0); //user is now a map of ALL USER data from backendless
                String userPass = user.get("password").toString(); //getting the password from column "password" in backendless
                firstConnection = user.get("firstConnection").toString(); //getting the firstConnection from column "password" in backendless
                if (userPass.equals(password)){ //checks for password to match
                    //inserting to sharedpreferences to remember the user detail and login without writing again next time
                    sharedPreferences.edit().putString("userName",userName).putString("password",password).putBoolean("isConnected",true).putString("tableName","AdminUsers").commit();
                    LoginActivity.this.TABLE_NAME = "AdminUsers";
                    SetCurrentUser(); //setting all instances of USER object inside USER class

                }else{
                    Toast.makeText(LoginActivity.this,"Username or password is wrong!!",Toast.LENGTH_SHORT).show();
                    loadingCircle.setVisibility(View.GONE); //Stopping the loading Icon..
                }
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {//if the user doesnt exists
                Toast.makeText(LoginActivity.this,"Username does not exists!!",Toast.LENGTH_LONG).show();
                loadingCircle.setVisibility(View.GONE); //Stopping the loading Icon..
            }
        });
    }
    //sets the current user's information inside USER class instances(static instance)
    private void SetCurrentUser(){
        BackendlessDataQuery query = new BackendlessDataQuery("userName = '"+userName+"'");
        Backendless.Persistence.of(TABLE_NAME).find(query, new AsyncCallback<BackendlessCollection<Map>>() {
            @Override
            public void handleResponse(BackendlessCollection<Map> res) {//if userName is found
                if (res.getData().isEmpty())return;
                Map userMap = res.getData().get(0);
                //initializing USER OBJECT's static instances:
                LoginActivity.this.user = new User(userMap.get("firstName").toString(),userMap.get("lastName").toString(),
                        userMap.get("userName").toString(),userMap.get("password").toString(),
                        userMap.get("ID").toString(),userMap.get("firstConnection").toString(),
                        userMap.get("email").toString(),userMap.get("company").toString(),
                        userMap.get("permission").toString(),userMap.get("ownerId").toString(),userMap.get("address").toString(),
                        userMap.get("birthDay").toString(),userMap.get("phoneNumber").toString());

                Intent i = new Intent(LoginActivity.this, MainScreenActivity.class);
                startActivity(i);
                loadingCircle.setVisibility(View.GONE); //Stopping the loading Icon..
                UserShiftsPattern.getInstance(User.company, LoginActivity.this);
                LoginActivity.this.finish(); //destroy this activity starting a different activity
            }
            @Override
            public void handleFault(BackendlessFault backendlessFault) {} //it will never be here!
        });
    }


}