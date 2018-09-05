package com.doctor.daktrakzdoctor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.doctor.daktrakzdoctor.utils.AlertClass;
import com.doctor.daktrakzdoctor.utils.Constants;
import com.doctor.daktrakzdoctor.utils.Network;
import com.doctor.daktrakzdoctor.utils.PreferenceKey;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignUpActivity extends AppCompatActivity {

    private Button ConfirmUser;
    SharedPreferences prefs;
    AlertClass alert;
    private EditText CustUsername, CustAddress, CustPassword;
    String username, address, password, RegNumb, Custloc, Custlat, Custlog, custlocspace;
    private ImageView LocationUpdate;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

      //  getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      //  getSupportActionBar().setDisplayShowHomeEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        RegNumb = prefs.getString(PreferenceKey.MOBILE_NUMBER, "");
        Custloc = prefs.getString(PreferenceKey.USER_ADDRESS, "");
        Custlat = prefs.getString(PreferenceKey.USER_LATITUDE, "");
        Custlog = prefs.getString(PreferenceKey.USER_LONGNITUDE, "");

        CustUsername = (EditText) findViewById(R.id.edtusername);
        CustAddress = (EditText) findViewById(R.id.edtaddress);
        CustPassword = (EditText) findViewById(R.id.edtpassword);
        CustAddress.setText(Custloc);
        ConfirmUser = (Button) findViewById(R.id.btnConfirm);

        LocationUpdate = (ImageView)findViewById(R.id.location_icon);

        LocationUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(SignUpActivity.this);
                    startActivityForResult(intent, 1);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }

        });

        ConfirmUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Uname = CustUsername.getText().toString();
                String Uaddress = CustAddress.getText().toString();
                String Upwd = CustPassword.getText().toString();
                if (TextUtils.isEmpty(Uname)) {
                    CustUsername.setError("Enter username first");
                }else if (TextUtils.isEmpty(Uaddress)) {
                    CustAddress.setError("Enter your address");
                }else if (TextUtils.isEmpty(Upwd)) {
                    CustPassword.setError("Enter your Password");
                }else {
                    if (Network.isNetworkAvailable(SignUpActivity.this)) {
                        username = Uname;
                        address = Uaddress;
                        password = Upwd;
                        new SignUpUserLogin().execute();
                    }else {
                        alert = new AlertClass(SignUpActivity.this, "No Network Connection");
                    }
                }
            }
        });

    }

    // A place has been received; use requestCode to track the request.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.e("Tag", "Place: " + place.getAddress() + place.getPhoneNumber());


                CustAddress.setText(place.getName()+",\n"+
                        place.getAddress() +"\n" + place.getPhoneNumber());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.e("Tag", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private class SignUpUserLogin extends AsyncTask<Void, Void, String> {

        private static final String LOG_TAG = "ExampleApp";

        String RegiAPI = Constants.MAIN_URL + "Register/Signup?mobile="+ RegNumb +"&name="+ username +"&password="+ password +"&address="+ address +"&latitude="+ Custlat +"&longitude="+Custlog;
        String SignUpAPI = RegiAPI.replaceAll(" ", "%20");

        @Override
        protected String doInBackground(Void... args) {

            HttpURLConnection conn = null;
            final StringBuilder json = new StringBuilder();
            try {
                java.net.URL url = new URL(SignUpAPI);
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
            pDialog = new ProgressDialog(SignUpActivity.this);
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
                    String custid = jsonObject.getString("Id");
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove(PreferenceKey.CHECK_DETAILS);
                    editor.putString(PreferenceKey.CUST_ID, custid);
                    editor.putString(PreferenceKey.CHECK_DETAILS, "2");
                    editor.putString(PreferenceKey.USER_NAME, user);
                    editor.commit();
                    Intent regiotp = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(regiotp);
                    finish();
                }else {
                    alert = new AlertClass(SignUpActivity.this, "Please try again later.");
                }

                pDialog.hide();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


}
