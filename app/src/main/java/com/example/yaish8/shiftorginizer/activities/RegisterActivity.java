package com.example.yaish8.shiftorginizer.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.emailing.SendMail;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Nimrod
 */

public class RegisterActivity extends AppCompatActivity {
    //
    EditText firstName,lastName,email,password,rePassword,id,company,phoneNumber,birthDay,address;
    String emailValidationCode;
    RelativeLayout loadingIcon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //initializing..
        firstName=(EditText)findViewById(R.id.register_firstNameEdit);
        lastName=(EditText)findViewById(R.id.register_lastNameEdit);
        email=(EditText)findViewById(R.id.register_emailEdit);
        password=(EditText)findViewById(R.id.register_passwordEdit);
        rePassword=(EditText)findViewById(R.id.register_rePasswordEdit);
        id=(EditText)findViewById(R.id.register_idEdit);
        company=(EditText)findViewById(R.id.register_companyEdit);
        phoneNumber=(EditText)findViewById(R.id.register_phoneEdit);
        birthDay=(EditText)findViewById(R.id.register_birthDay);
        address=(EditText)findViewById(R.id.register_address);
    }
    //"Back" Button
    public void goBackBtn(View v){
        //going back to LoginActivity screen..
        Intent i = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(i);
        RegisterActivity.this.finish();

    }
    //"Register" Button
    public void registerBtn(View v){
        //getting user's input_login text from edittext
        final String firstName = getStringFromEditText(this.firstName);
        final String lastName = getStringFromEditText(this.lastName);
        final String email = getStringFromEditText(this.email);
        final String password = getStringFromEditText(this.password);
        String rePassword = getStringFromEditText(this.rePassword);
        final String id = getStringFromEditText(this.id);
        final String company = getStringFromEditText(this.company);
        final String phoneNumber = getStringFromEditText(this.phoneNumber);
        final String address = getStringFromEditText(this.address);
        final String bDay = getStringFromEditText(birthDay);
        String [] fields = {firstName,lastName,email,password,rePassword,id,company,phoneNumber,address,bDay};
        if (!isEmptyFields(fields)){
            Toast.makeText(this,"Fields can't stay empty",Toast.LENGTH_LONG).show();
            return;
        }
        loadingIcon=(RelativeLayout)findViewById(R.id.register_loadingPanel);
        //validations.. Regular Expressions
        String namesRegex = "^[a-zA-z]{2,12}$"; //only a-z or\and A-Z between 2-12 characters
        String passwordRegex = "^[a-zA-Z0-9]{6,12}$"; ////only a-z or\and A-Z and\or 0-9 between 6-12 characters
        String idRegex = "^[0-9]{9}$"; //only numbers with length of 9
        String phoneRegex = "^[0-9]{10}$"; //only numbers with length of 10
        String emailRegex = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        if (!(firstName.matches(namesRegex) && lastName.matches(namesRegex))){ //checking validation for name and last name
            Toast.makeText(this,"names must contain between 2-12 letters",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(password.equals(rePassword))){ //checking if both of the passwords are identical
            Toast.makeText(this,"passwords are not the same",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(password.matches(passwordRegex))){ //checking validation for password
            Toast.makeText(this,"password must contain between 6-12 letters OR numbers",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(id.matches(idRegex))){ //checking validation for ID
            Toast.makeText(this,"id must contain 9 numbers",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(phoneNumber.matches(phoneRegex))){ //checking validation for Phone-Number
            Toast.makeText(this,"phone number must contain 10 numbers",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(email.matches(emailRegex))){
            Toast.makeText(this,"Email is not Valid",Toast.LENGTH_SHORT).show();
            return;
        }
        //making an email confirmation with dialog box and edit text..
        loadingIcon.setVisibility(View.VISIBLE);
        sendEmail();
        android.os.Handler handler = new android.os.Handler(); //making Runnable handler that would handle this besides the email sending..
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //making an EditText View for the DialogBox later..
                final EditText confirmEmailEdit = new EditText(RegisterActivity.this);
                confirmEmailEdit.setHint("Write Confirmation code");
                confirmEmailEdit.setTextSize(20);
                loadingIcon.setVisibility(View.GONE);
                final AlertDialog.Builder dialog = new AlertDialog.Builder(RegisterActivity.this);
                dialog.setTitle("Confirm your Email").setMessage("An email  has been sent to you with the confirm code").setView(confirmEmailEdit).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    //when clicking the Button inside the Dialog box
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String validation = confirmEmailEdit.getText().toString();
                        if (!(validation.equals(RegisterActivity.this.emailValidationCode))){
                            Toast.makeText(RegisterActivity.this,"Validation Wrong!!",Toast.LENGTH_SHORT).show();
                        }else if (validation.equals(RegisterActivity.this.emailValidationCode)){
                            register(firstName,lastName,email,company,password,id,phoneNumber);
                        }
                    }
                }).setNegativeButton("Cancel",null).show();
            }
        },500);
    }
    //Registration User to Backendless
    private void register(String firstname, String lastname, String email, final String company, String password, String id, String phone){
        //adding to Users table in backendless with permissions 0 - admin..
        Map user = new HashMap();
        user.put("company", company);
        user.put("email",email);
        user.put("firstName",firstname);
        user.put("lastName",lastname);
        user.put("password",password);
        user.put("permission",0);
        user.put("phoneNumber",phone);
        user.put("ID",id);
        user.put("userName",firstname+"_"+lastname);
        user.put("ownerId",id);
        user.put("birthDay",birthDay.getText().toString());
        user.put("address",address.getText().toString());
        //Built in Class of Backendless that register user
        Backendless.Persistence.of("AdminUsers").save(user, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map map) { //if the registration was completed:
                Toast.makeText(RegisterActivity.this,"Added",Toast.LENGTH_SHORT).show();
                //going back to LoginActivity so that the user will login
                Intent i = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(i);
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) { //if registration Failed:
                Toast.makeText(RegisterActivity.this,"Error Adding User",Toast.LENGTH_SHORT).show();
            }
        });


    }
    //sending Email From project owner's email to user's email to confirm..
    private void sendEmail() {
        this.emailValidationCode = ""+randomNum(); //saving a random number to emailValidationCode Variable
        String email = getStringFromEditText(this.email).trim();
        String subject = "Thanks for Registering, Hope you will enjoy our App!! "; //this will be the title of the email
        //this is the main message inside the sended email: username, password and confirmation code from emailValidationCode Variable
        String message = "Your username is: "+getStringFromEditText(this.firstName)
                +"_"+getStringFromEditText(this.lastName)+"\nYour password is: "+getStringFromEditText(this.password)
                +"\nYour Code to confirm is: " + this.emailValidationCode;

        //Creating SendMail object
        SendMail sm = new SendMail(this, email, subject, message);
        //Executing sendmail to send email
        sm.execute();
    }
    //method that returns a random number between 1111111 to 999999999
    public static int randomNum() {
        Random r = new Random();
        int l1 = r.nextInt(999999999 - 1111111) + 1111111;
        return l1;
    }
    //method that will return a String from Edittext to make things easier for me :)
    private String getStringFromEditText(EditText editText){
        return ""+editText.getText().toString();
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

