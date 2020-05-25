package com.abhed.hkccappointment;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

public class UIBuilder {

    public static TextView addLabel(Activity activity, LinearLayout llParent, String s) {
        TextView tv = new TextView(activity);
        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tv.setTextColor(Color.parseColor("#121833"));
        tv.setText(s);
        tv.setTextSize(18.0f);

        Typeface typeface = ResourcesCompat.getFont(activity, R.font.sfnsdisplay);

        tv.setTypeface(typeface, Typeface.NORMAL);

        llParent.addView(tv);

        TextView pad = new TextView(activity);
        pad.setTextSize(2.0f);
        //pad.setPadding(10, 10, 10, 10);

        llParent.addView(pad);

        return tv;

    }

    public static EditText addField(Activity activity, LinearLayout llParent, String label) {
        EditText editText = new EditText(activity);
        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setHint(label);
        //editText.setBackground(Color.parseColor("#00000000"));
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setTextColor(Color.parseColor("#030303"));
        editText.setHintTextColor(Color.parseColor("#d3d3d3"));

        llParent.addView(editText);

        TextView tv = new TextView(activity);
        tv.setTextSize(2.0f);
        tv.setPadding(10, 10, 10, 10);

        llParent.addView(tv);

        return editText;
    }

    public static Button addButton(Activity activity, LinearLayout llParent, String s) {
        Button btn = new Button(activity);
        btn.setBackgroundResource(R.drawable.rect_blue);
        btn.setText(s);
        btn.setTextColor(Color.WHITE);
        llParent.addView(btn);

        TextView tv = new TextView(activity);
        tv.setTextSize(2.0f);
        tv.setPadding(10, 10, 10, 10);

        llParent.addView(tv);

        return btn;
    }

    public static TextView addLabelSmall(Activity activity, LinearLayout llParent, String s) {
        TextView tv = new TextView(activity);
        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tv.setTextColor(Color.parseColor("#121833"));
        tv.setText(s);
        tv.setTextSize(14.0f);

        Typeface typeface = ResourcesCompat.getFont(activity, R.font.sfnsdisplay);


        llParent.addView(tv);


        return tv;

    }
}
