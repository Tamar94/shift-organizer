package com.example.yaish8.shiftorginizer.Utils.emailing;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.user.User;
import com.example.yaish8.shiftorginizer.activities.MainScreenActivity;

import java.util.List;
import java.util.Map;

/**
 * Created by Nimrod
 */

public class EmailSendingToAll extends AppCompatActivity{

    public static void sendEmailToAll(final Context context){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Send Email?").setMessage("Would you want to send emails\n for your employees to let them know \nabout your new work arrangement?")
                .setNegativeButton("NO THANKS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(context, MainScreenActivity.class);
                        context.startActivity(i);
                        ((Activity)context).finish();
                    }
                })
                .setPositiveButton("SURE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME),MODE_PRIVATE);
                String tableName = sharedPreferences.getString("TABLE_NAME","");
                BackendlessDataQuery query = new BackendlessDataQuery("company = '"+User.company+"'");
                Backendless.Persistence.of(tableName).find(query, new AsyncCallback<BackendlessCollection<Map>>() {
                    @Override
                    public void handleResponse(BackendlessCollection<Map> mapBackendlessCollection) {
                        List<Map> data = mapBackendlessCollection.getData();
                        if (!data.isEmpty()) {
                            String[] emails = new String[data.size()];
                            for (int i = 0; i < data.size(); i++) {
                                Map user = data.get(i);
                                emails[i] = user.get("email").toString();
                            }
                            String subject = User.firstName + " " + User.lastName + " has published a new work arrangement";
                            String message = "You can now log in to ShiftOrginizer app to see your new work arrangement";
                            String email;
                            for (int i = 0; i < emails.length; i++) {
                                email = emails[i];
                                new SendMail(context, email, subject, message).execute();
                            }
                            Toast.makeText(context, "Email sent!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        Toast.makeText(context,"Problem sending Email..",Toast.LENGTH_SHORT).show();
                    }
                });
                Intent intent = new Intent(context, MainScreenActivity.class);
                context.startActivity(intent);
                ((Activity)context).finish();
            }

        })
                .show();
    }



}
