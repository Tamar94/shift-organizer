package com.example.yaish8.shiftorginizer.Utils;
/**
 * Created by Nimrod
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.example.yaish8.shiftorginizer.activities.ContactListAdminActivity;
import com.example.yaish8.shiftorginizer.activities.MainScreenActivity;


public class GoHomeClickListener  implements View.OnClickListener{

    private Context context;
    public GoHomeClickListener(Context context)
    {
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(context,MainScreenActivity.class);
        context.startActivity(i);
        ((Activity)context).finish();
    }
}
