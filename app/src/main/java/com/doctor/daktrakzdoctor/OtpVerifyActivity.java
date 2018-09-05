package com.doctor.daktrakzdoctor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.doctor.daktrakzdoctor.utils.AlertClass;
import com.doctor.daktrakzdoctor.utils.Constants;
import com.doctor.daktrakzdoctor.utils.Network;
import com.doctor.daktrakzdoctor.utils.PreferenceKey;
import com.goodiebag.pinview.Pinview;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OtpVerifyActivity extends AppCompatActivity {

    private Pinview OtpVerify;
    TextView resend_timer;
    String RegNumb, GetEnterPinValue;
    AlertClass alert;
    private ProgressDialog pDialog;
    SharedPreferences prefs;
    private TextView RegisterNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verify);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        RegNumb = prefs.getString(PreferenceKey.MOBILE_NUMBER, "");

        RegisterNumber = (TextView)findViewById(R.id.numberText);
        RegisterNumber.setText(RegNumb);

        resend_timer = (TextView) findViewById(R.id.resend_timer);
        resend_timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResendCode();
            }
        });
        startTimer();

        TextView resend_timer = (TextView) findViewById(R.id.resend_timer);
        resend_timer.setClickable(false);

        OtpVerify= (Pinview) findViewById(R.id.pinview1);

        OtpVerify.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean fromUser) {
                GetEnterPinValue = pinview.getValue();
                Toast.makeText(OtpVerifyActivity.this, pinview.getValue(), Toast.LENGTH_SHORT).show();

                if (Network.isNetworkAvailable(OtpVerifyActivity.this)) {
                    new OTPVerifyUserLogin().execute();
                }else {
                    alert = new AlertClass(OtpVerifyActivity.this, "No Network Connection");
                }

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent bck = new Intent(this, LoginActivity.class);
                startActivity(bck);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void ResendCode() {
        Toast.makeText(this, "test", Toast.LENGTH_LONG).show();
        new VerifyByCall().execute();
      //  mVerification.resend("voice");
    }

    private void startTimer() {
        resend_timer.setClickable(false);
        resend_timer.setTextColor(ContextCompat.getColor(this, R.color.sendotp_grey));
        new CountDownTimer(30000, 1000) {
            int secondsLeft = 0;

            public void onTick(long ms) {
                if (Math.round((float) ms / 1000.0f) != secondsLeft) {
                    secondsLeft = Math.round((float) ms / 1000.0f);
                    resend_timer.setText("Resend via call ( " + secondsLeft + " )");
                }
            }

            public void onFinish() {
                resend_timer.setClickable(true);
                resend_timer.setText("Resend via call");
                resend_timer.setTextColor(ContextCompat.getColor(OtpVerifyActivity.this, R.color.colorPrimary));
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    private class VerifyByCall extends AsyncTask<Void, Void, String> {

        private static final String LOG_TAG = "ExampleApp";

        String CallURL = "https://www.daktarz.com/api/Register/Resend?MobileNumber="+ RegNumb;

        @Override
        protected String doInBackground(Void... args) {

            HttpURLConnection conn = null;
            final StringBuilder json = new StringBuilder();
            try {
                java.net.URL url = new URL(CallURL);
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                // Read the JSON data into the StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    json.append(buff, 0, read);
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error connecting to service", e);
                //throw new IOException("Error connecting to service", e); //uncaught
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return json.toString();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(OtpVerifyActivity.this);
            pDialog.setMessage("Loading ...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String json) {

            try {
                JSONObject jsonObject1 = new JSONObject(json);
                String OtpCallresult = jsonObject1.getString("result");
                if (OtpCallresult.equalsIgnoreCase("Successs")) {
                    alert = new AlertClass(OtpVerifyActivity.this, "Please Wait you have to receive OTP by Voice Call");
                }

                pDialog.hide();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class OTPVerifyUserLogin extends AsyncTask<Void, Void, String> {

        private static final String LOG_TAG = "ExampleApp";

        String OTPURL =  Constants.MAIN_URL + "Register/VerifyUser?mobileNumber="+ RegNumb +"&otp="+ GetEnterPinValue;

        @Override
        protected String doInBackground(Void... args) {

            HttpURLConnection conn = null;
            final StringBuilder json = new StringBuilder();
            try {
                java.net.URL url = new URL(OTPURL);
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                // Read the JSON data into the StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    json.append(buff, 0, read);
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error connecting to service", e);
                //throw new IOException("Error connecting to service", e); //uncaught
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return json.toString();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(OtpVerifyActivity.this);
            pDialog.setMessage("Loading ...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String json) {

            try {
                JSONObject jsonObject1 = new JSONObject(json);
                String Registerresult = jsonObject1.getString("result");
                if (Registerresult.equalsIgnoreCase("Verified Successfully")) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove(PreferenceKey.CHECK_DETAILS);
                    editor.putString(PreferenceKey.CHECK_DETAILS, "1");
                    editor.commit();
                    Intent regiotp = new Intent(OtpVerifyActivity.this, SignUpActivity.class);
                    startActivity(regiotp);
                    finish();
                }else if (Registerresult.equalsIgnoreCase("Match Not Found")){
                    alert = new AlertClass(OtpVerifyActivity.this, "Please Enter Correct OTP Code");
                }

                pDialog.hide();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
