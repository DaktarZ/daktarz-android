package com.doctor.daktrakzdoctor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.doctor.daktrakzdoctor.utils.AlertClass;
import com.doctor.daktrakzdoctor.utils.Constants;
import com.doctor.daktrakzdoctor.utils.Network;
import com.doctor.daktrakzdoctor.utils.PreferenceKey;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by amit ji on 7/29/2018.
 */

public class ForgetPwdActivity extends AppCompatActivity {

    private EditText MobileNumber;
    private Button ConfirmPwd;
    String UserNumber;
    AlertClass alert;
    private ProgressDialog pDialog;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        MobileNumber = (EditText)findViewById(R.id.edt_mobile);

        ConfirmPwd = (Button)findViewById(R.id.btnforgetpwd);
        ConfirmPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String MobNumb = MobileNumber.getText().toString();
                if (TextUtils.isEmpty(MobNumb)) {
                    MobileNumber.setError("Enter Mobile number first");
                } else {
                    if (Network.isNetworkAvailable(ForgetPwdActivity.this)) {
                        String Regex = "[^\\d]";
                        String PhoneDigits = MobNumb.replaceAll(Regex, "");
                        if (PhoneDigits.length()!=10)
                        {
                            MobileNumber.setError("Please enter 10 digit mobile number only");
                        }
                        else
                        {
                            UserNumber  = "91" + MobNumb;
                            new ForgetPwdUserLogin().execute();
                        }
                    } else {
                        alert = new AlertClass(ForgetPwdActivity.this, "No Network Connection");
                    }
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent bck = new Intent(this, AlreadyLoginUserActivity.class);
                startActivity(bck);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private class ForgetPwdUserLogin extends AsyncTask<Void, Void, String> {

        private static final String LOG_TAG = "ExampleApp";

        String ForgetAPI = Constants.MAIN_URL + "Register/ForgetPassword?MobileNumber="+ UserNumber;

        @Override
        protected String doInBackground(Void... args) {

            HttpURLConnection conn = null;
            final StringBuilder json = new StringBuilder();
            try {
                java.net.URL url = new URL(ForgetAPI);
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
            pDialog = new ProgressDialog(ForgetPwdActivity.this);
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
                if (Registerresult.equalsIgnoreCase("Password sent to mobile.")) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(ForgetPwdActivity.this);

                    TextView myMsg = new TextView(getApplicationContext());
                    myMsg.setText("You have received password by message on Verified Mobile Number.");
                    myMsg.setTextSize(16);
                    myMsg.setPadding(0, 20, 0, 0);
                    myMsg.setGravity(Gravity.CENTER);
                    builder1.setView(myMsg);

                    builder1.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent regiotp = new Intent(ForgetPwdActivity.this, AlreadyLoginUserActivity.class);
                                    startActivity(regiotp);
                                    finish();
                                }
                            });


                    AlertDialog alert11 = builder1.create();
                    alert11.setCancelable(false);
                    alert11.show();
                    pDialog.hide();
                }else if(Registerresult.equalsIgnoreCase("Password Not Found"))  {

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(ForgetPwdActivity.this);

                    TextView myMsg = new TextView(getApplicationContext());
                    myMsg.setText("This Number is not verified. Please first Verify number.");
                    myMsg.setTextSize(16);
                    myMsg.setPadding(0, 20, 0, 0);
                    myMsg.setGravity(Gravity.CENTER);
                    builder1.setView(myMsg);

                    builder1.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.remove(PreferenceKey.MOBILE_NUMBER);
                                    editor.putString(PreferenceKey.MOBILE_NUMBER, UserNumber);
                                    Intent getotp = new Intent(ForgetPwdActivity.this, OtpVerifyActivity.class);
                                    startActivity(getotp);
                                    finish();
                                }
                            });


                    AlertDialog alert11 = builder1.create();
                    alert11.setCancelable(false);
                    alert11.show();

                    pDialog.hide();
                }

                pDialog.hide();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
