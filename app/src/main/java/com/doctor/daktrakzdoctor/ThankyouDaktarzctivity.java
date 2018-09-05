package com.doctor.daktrakzdoctor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import com.doctor.daktrakzdoctor.utils.PreferenceKey;

/**
 * Created by amit ji on 8/2/2018.
 */

public class ThankyouDaktarzctivity extends Activity {

    private ImageView Logout;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_thanksto_you);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Logout = (ImageView)findViewById(R.id.btnLogout);

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ThankyouDaktarzctivity.this)
                        .setTitle("Confirm")
                        .setMessage("Do you want to log out from Daktarz ?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                callToLogOutFunction();
                            }
                        })
                        .create().show();
            }
        });

    }

    //logout function
    private void callToLogOutFunction() {
        // TODO Auto-generated method stub
        SharedPreferences.Editor editor = prefs.edit();     //
        editor.remove(PreferenceKey.CUSTOMER_ID);
        editor.remove(PreferenceKey.CUSTOMER_FORM_ID);
        editor.remove(PreferenceKey.CUST_ID);
        editor.remove(PreferenceKey.USER_NAME);
        editor.remove(PreferenceKey.MOBILE_NUMBER);
        editor.remove(PreferenceKey.CHECK_DETAILS);
        editor.commit();
        Intent intent = new Intent(ThankyouDaktarzctivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
}
