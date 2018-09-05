package com.doctor.daktrakzdoctor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.doctor.daktrakzdoctor.utils.AlertClass;
import com.doctor.daktrakzdoctor.utils.Constants;
import com.doctor.daktrakzdoctor.utils.HexCoder;
import com.doctor.daktrakzdoctor.utils.Network;
import com.doctor.daktrakzdoctor.utils.PreferenceKey;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

/**
 * Created by amit ji on 7/26/2018.
 */

public class AlreadyLoginUserActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText CustMobileNumber, CustPassword;
    private TextView ForgetPwd;
    private Button LoginCust;
    String UserNumber, UserPassword;
    AlertClass alert;
    private ProgressDialog pDialog;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_already_user_login);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        CustMobileNumber = (EditText)findViewById(R.id.edt_mobile);
        CustPassword = (EditText)findViewById(R.id.edt_pwd);

        ForgetPwd = (TextView)findViewById(R.id.tv_forget_user);

        LoginCust = (Button)findViewById(R.id.btnlogin);

        LoginCust.setOnClickListener(this);
        ForgetPwd.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnlogin:
                String MobNumb = CustMobileNumber.getText().toString();
                String Password = CustPassword.getText().toString();
                if (TextUtils.isEmpty(MobNumb)) {
                    CustMobileNumber.setError("Enter Mobile number first");
                } if (TextUtils.isEmpty(Password)) {
                    CustPassword.setError("Enter your Password");
                } else {
                if (Network.isNetworkAvailable(AlreadyLoginUserActivity.this)) {
                    String Regex = "[^\\d]";
                    String PhoneDigits = MobNumb.replaceAll(Regex, "");
                    if (PhoneDigits.length() != 10) {
                        CustMobileNumber.setError("Please enter 10 digit mobile number only");
                    } else {
                        UserNumber = "91" + MobNumb;
                        UserPassword = Password;
                        new RegisterUserLogin().execute();
                    }
                } else {
                    alert = new AlertClass(AlreadyLoginUserActivity.this, "No Network Connection");
                }
             }
                break;
            case R.id.tv_forget_user:
                Intent forget = new Intent(this, ForgetPwdActivity.class);
                startActivity(forget);
                break;
             default:
                 break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent login = new Intent(this, LoginActivity.class);
        startActivity(login);
        finish();
    }

    private class RegisterUserLogin extends AsyncTask<Void, Void, String> {

        private static final String LOG_TAG = "ExampleApp";

        String RegiAPI = Constants.MAIN_URL + "register/Login?MobileNumber="+ UserNumber + "&Password=" + UserPassword +"&devicetype="+ getDeviceID(AlreadyLoginUserActivity.this) +"&TokenId=3&UserType=1&DeviceName=android";

        @Override
        protected String doInBackground(Void... args) {

            HttpURLConnection conn = null;
            final StringBuilder json = new StringBuilder();
            try {
                java.net.URL url = new URL(RegiAPI);
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
            pDialog = new ProgressDialog(AlreadyLoginUserActivity.this);
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
                if (Registerresult.equalsIgnoreCase("true")) {
                    JSONObject jsonObject = jsonObject1.getJSONObject("Customer");
                    String user = jsonObject.getString("Name");
                    String phonenumber = jsonObject.getString("Phone");
                    String custid = jsonObject.getString("Id");
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(PreferenceKey.MOBILE_NUMBER, phonenumber);
                    editor.putString(PreferenceKey.USER_NAME, user);
                    editor.putString(PreferenceKey.CUST_ID, custid);
                    editor.putString(PreferenceKey.CHECK_DETAILS, "2");
                    editor.commit();
                    Intent regiotp = new Intent(AlreadyLoginUserActivity.this, MainActivity.class);
                    startActivity(regiotp);
                    finish();
                }else if (Registerresult.equalsIgnoreCase("Already Register")){
                    alert = new AlertClass(AlreadyLoginUserActivity.this, "This number is already register.");
                }

                pDialog.hide();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public static String getDeviceID(Context context) {
        String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String device = Build.DEVICE;
        id = device + id;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
            byte[] bytes = id.getBytes("UTF-8");
            md.update(bytes, 0, bytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return HexCoder.toHex(md.digest());
    }

}
