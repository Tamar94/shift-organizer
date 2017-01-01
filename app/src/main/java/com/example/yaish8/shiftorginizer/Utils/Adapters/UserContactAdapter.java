package com.example.yaish8.shiftorginizer.Utils.Adapters;
/**
 * Created by Levona
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.yaish8.shiftorginizer.R;
import com.example.yaish8.shiftorginizer.Utils.user.EmployeeUsers;

import java.util.List;

public class UserContactAdapter extends BaseAdapter {
    private final Context context;
    List<EmployeeUsers> employess;

    public UserContactAdapter(Context context, List<EmployeeUsers> employees) {
        this.context = context;
        this.employess = employees;
    }

    @Override
    public int getCount() {
        return this.employess.size();
    }

    @Override
    public Object getItem(int position) {
        return this.employess.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null)convertView = LayoutInflater.from(context).inflate(R.layout.user_name_phone_foradapter,null);
        LinearLayout linearLayout = (LinearLayout)convertView;
        TextView name   = (TextView)linearLayout.findViewById(R.id.inflate_name);
        TextView phone  = (TextView)linearLayout.findViewById(R.id.inflate_number);
        EmployeeUsers currentEmployee = this.employess.get(position);
        String txtName  = currentEmployee.getFirstName() + " " + currentEmployee.getLastName();
        String txtPhone = currentEmployee.getPhoneNumber();
        name.setText(txtName);
        phone.setText(txtPhone);
        return convertView;
    }
}
