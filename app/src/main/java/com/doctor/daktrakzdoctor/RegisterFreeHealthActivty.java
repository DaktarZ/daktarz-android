package com.doctor.daktrakzdoctor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Created by amit ji on 7/24/2018.
 */

public class RegisterFreeHealthActivty extends Activity {

    private Button HealthCheckUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_free_health_check);

        HealthCheckUp = (Button)findViewById(R.id.btnConfirm);

        HealthCheckUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent healthcheckup = new Intent(RegisterFreeHealthActivty.this, FreeHealthCheckupFormActivity.class);
                startActivity(healthcheckup);
            }
        });


    }
}
