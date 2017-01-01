package com.example.yaish8.shiftorginizer.Utils;
/**
 * Created by Tamar
 */
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;


public class NumberPickerView extends NumberPicker{

        public NumberPickerView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public void addView(View child) {
            super.addView(child);
            updateView(child);
        }

        @Override
        public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
            super.addView(child, index, params);
            updateView(child);
        }

        @Override
        public void addView(View child, android.view.ViewGroup.LayoutParams params) {
            super.addView(child, params);
            updateView(child);
        }

        private void updateView(View view) {
            if(view instanceof EditText){
                ((EditText) view).setTextSize(25);
            }
        }
    }

