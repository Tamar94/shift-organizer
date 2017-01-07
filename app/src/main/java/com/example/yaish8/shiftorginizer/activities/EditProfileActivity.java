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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.GoHomeClickListener;
import com.example.yaish8.shiftorginizer.Utils.user.User;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class EditProfileActivity extends AppCompatActivity {
    TextView name,userName,email;
    EditText address,phone;
    RelativeLayout loading;
    ImageView homeIcon;
    SharedPreferences sharedPreferences;
    String username, tableName, userPass;
    final String TAG = "EditProActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);
        //initializing
        loading = (RelativeLayout)findViewById(R.id.editprofile_loadingPanel);
        loading.setVisibility(View.VISIBLE); // visible until page finished loading
        homeIcon = (ImageView)findViewById(R.id.homeIcon_editProfile);
        homeIcon.setOnClickListener(new GoHomeClickListener(this));
        sharedPreferences = getSharedPreferences(getResources().getString(R.string.SHARED_PREFERENCES_NAME),MODE_PRIVATE);
        username = User.userName;
        if ("0".equals(User.permission)) tableName = "AdminUsers";
        else if ("1".equals(User.permission)) tableName = "EmployeeUsers";
        else tableName = "EmployeeUsers";
        name = (TextView)findViewById(R.id.editprofile_nameTextEdit);
        userName = (TextView)findViewById(R.id.editprofile_userNameTextEdit);
        email = (TextView)findViewById(R.id.editprofile_emailTextEdit);

        address = (EditText)findViewById(R.id.editprofile_addressEditText);
        phone = (EditText)findViewById(R.id.editprofile_phoneEditText);
        setEditTexts();
    }
    //setting text to all the TextViews and EditTexts in activity:
    private void setEditTexts(){
        name.setText(User.firstName + " " + User.lastName); //getting from User class instances..
        userName.setText(username);
        email.setText(User.email);

        address.setText(User.address);
        phone.setText(User.phoneNumber);
        userPass = User.password;
        loading.setVisibility(View.GONE);
    }
    //saving the changes the user made inside both backendless and sharedpreferences and loging out for reConnection..
    private void editUser(final Map editWhat){
        Backendless.Persistence.of(tableName).find(new BackendlessDataQuery("userName = '" + username + "'"), new AsyncCallback<BackendlessCollection<Map>>() {
            @Override
            public void handleResponse(BackendlessCollection<Map> mapBackendlessCollection) {
                Map user = mapBackendlessCollection.getData().get(0);
                Set keys = editWhat.entrySet(); //get all the keys to a Set..
                Iterator i = keys.iterator();
                while (i.hasNext()){
                    Map.Entry me =(Map.Entry) i.next(); // inserting the Iteretor to mapEntry and next()..
                    user.put(me.getKey(),me.getValue()); // inserting to user-Map the current value of mapentry
                }
                Backendless.Persistence.of(tableName).save(user, new AsyncCallback<Map>() { //saving the changes in backendless
                    @Override
                    public void handleResponse(Map map) {
                        Toast.makeText(EditProfileActivity.this,"Changes saved!!",Toast.LENGTH_SHORT).show();
                        loading.setVisibility(View.GONE);
                        Intent i = new Intent(EditProfileActivity.this,LoginActivity.class);
                        sharedPreferences.edit().clear().commit();
                        startActivity(i);
                        EditProfileActivity.this.finish();
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                       Log.e(TAG, backendlessFault.getMessage());
                        loading.setVisibility(View.GONE);
                    }
                });
            }
            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                loading.setVisibility(View.GONE);
            }
        });
    }
    //save button
    public void save(View v){
        loading.setVisibility(View.VISIBLE);
        if(this.phone.getText().toString().length()!=10){ //checks validation for number
            Toast.makeText(this,"Phone Number Not Valid",Toast.LENGTH_SHORT).show();
            return;
        }
        Map thingsToEdit = new HashMap();//making a map to pass through in method editUser()..
        thingsToEdit.put("address",this.address.getText().toString()); //putting to the map the changes
        thingsToEdit.put("phoneNumber",this.phone.getText().toString());
        editUser(thingsToEdit);
    }
    //change password button
    public void changePass(View v){
        //making EditTexts for the dialog box later on.. but needed to put all textViews inside a LinearLayout..
        final EditText oldPass = new EditText(this);
        oldPass.setHint("Old Password");
        oldPass.setTextSize(18);
        final EditText pass = new EditText(this);
        pass.setHint("Choose new password");
        pass.setTextSize(18);
        final EditText rePass = new EditText(this);
        rePass.setHint("Type again new password");
        rePass.setTextSize(18);
        LinearLayout layout = new LinearLayout(this);//making a layout with all editTexts because dialog box may have only one View..
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(oldPass);
        layout.addView(pass);
        layout.addView(rePass);
        //Dialog building:
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Change password").setView(layout).setNegativeButton("Cancel",null).
                setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (rePass.getText().toString().equals(pass.getText().toString())){
                            if (oldPass.getText().toString().equals(EditProfileActivity.this.userPass)){
                                //change password in backendless..
                                loading.setVisibility(View.VISIBLE);
                                Map thingsToEdit = new HashMap();
                                thingsToEdit.put("password",pass.getText().toString());
                                editUser(thingsToEdit);
                                sharedPreferences.edit().putString("password",pass.getText().toString()).commit();
                            }else{
                                Toast.makeText(EditProfileActivity.this,"Wrong Old Password Written",Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(EditProfileActivity.this,"Passwords don't match",Toast.LENGTH_SHORT).show();}
                    }
                }).show();
    }
}
