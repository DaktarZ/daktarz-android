package com.doctor.daktrakzdoctor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
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
 * Created by amit ji on 7/27/2018.
 */

public class FreeHealthCheckupFormActivity extends AppCompatActivity implements View.OnClickListener {

    final String[] blood_group = new String[]{"Blood Group", "O +", "O -", "A +", "A -", "B +", "B -", "AB +", "AB -"};
    Spinner navBloodGroupSpiner;
    private  EditText FirstName, LastName, MiddleName, Age, CustAddress, MedicalHistory, CustCity, CustState, CustPincode;
    String blodgrop, gender, Fstname, lstname, msname, custage, cstcity, cstaddress, cstpincode, cstprevioushistory;
    AlertClass alert;
    private Button NextConfirm;
    private RadioButton RadioMale, RadioFemale;
    private ProgressDialog pDialog;
    SharedPreferences prefs;
    String RegNumb, UserId, Custloc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reister_health_checkup);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        RegNumb = prefs.getString(PreferenceKey.MOBILE_NUMBER, "");
        UserId = prefs.getString(PreferenceKey.CUST_ID, "");
        Custloc = prefs.getString(PreferenceKey.USER_ADDRESS, "");

        FirstName = (EditText)findViewById(R.id.edtfirstname);
        MiddleName = (EditText)findViewById(R.id.edtmiddlename);
        LastName = (EditText)findViewById(R.id.edtlastname);
        Age = (EditText)findViewById(R.id.edtage);
        CustCity = (EditText)findViewById(R.id.edtcity);
        CustState = (EditText)findViewById(R.id.edtstate);
        CustPincode = (EditText)findViewById(R.id.edtpincode);
        CustAddress = (EditText)findViewById(R.id.edtaddress);
        CustAddress.setText(Custloc);
        MedicalHistory = (EditText)findViewById(R.id.edtprevious_history);

        RadioGroup rg = (RadioGroup) findViewById(R.id.radio);
        RadioMale = (RadioButton)findViewById(R.id.radioMale);
        RadioFemale = (RadioButton)findViewById(R.id.radioFemale);

        RadioMale.setOnClickListener(this);
        RadioFemale.setOnClickListener(this);

        NextConfirm = (Button)findViewById(R.id.btnConfirm);
        NextConfirm.setOnClickListener(this);
        blodgrop = "select";
        navBloodGroupSpiner = (Spinner) findViewById(R.id.edtblood_group);
        NavSelectionAdapter nvselt = new NavSelectionAdapter(this, R.layout.set_livespinnerview, blood_group);
        navBloodGroupSpiner.setAdapter(nvselt);
        navBloodGroupSpiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               switch (position) {
                   case 0:
                       blodgrop = "select";
                       break;
                   case 1:
                       blodgrop = "O+";
                       break;
                   case 2:
                       blodgrop = "O-";
                       break;
                   case 3:
                       blodgrop = "A+";
                       break;
                   case 4:
                       blodgrop = "A-";
                       break;
                   case 5:
                       blodgrop = "B+";
                       break;
                   case 6:
                       blodgrop = "B-";
                       break;
                   case 7:
                       blodgrop = "AB+";
                       break;
                   case 8:
                       blodgrop = "AB-";
                       break;
               }
           }

           @Override
           public void onNothingSelected(AdapterView<?> parent) {

           }
       });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent bck = new Intent(this, MainActivity.class);
                startActivity(bck);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    //setting Selection Spinner Adapter
    public class NavSelectionAdapter extends ArrayAdapter<String> {

        public NavSelectionAdapter(Context context, int textViewResourceId, String[] objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View rowst = inflater.inflate(R.layout.spinner_dropdown_item, parent, false);
            TextView mTxtSpinnerText = (TextView) rowst.findViewById(R.id.tvspinner1text);
            mTxtSpinnerText.setText(blood_group[position]);
            mTxtSpinnerText.setTextSize(18);
            return rowst;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View rowst1 = inflater.inflate(R.layout.set_livespinnerview, parent, false);
            TextView mTxtSpinnerText = (TextView) rowst1.findViewById(R.id.tvspinnertext);
            mTxtSpinnerText.setText(blood_group[position]);
            mTxtSpinnerText.setTextSize(18);
            return rowst1;

        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.radioMale:
                gender = "Male";
                break;
            case R.id.radioFemale:
                gender = "female";
                break;
            case R.id.btnConfirm:
                String fname = FirstName.getText().toString();
                String lname = LastName.getText().toString();
                String mname = MiddleName.getText().toString();
                String age = Age.getText().toString();
                String city = CustCity.getText().toString();
                String adds = CustAddress.getText().toString();
                String pincode = CustPincode.getText().toString();
                String prevoiusMed = MedicalHistory.getText().toString();

                if (TextUtils.isEmpty(fname)) {
                    FirstName.setError("Enter your first name");
                } else if (TextUtils.isEmpty(lname)) {
                    LastName.setError("Enter your last name");
                } else if (TextUtils.isEmpty(age)) {
                    Age.setError("Enter your Age");
                 } else if (blodgrop.equalsIgnoreCase("select")) {
                   Toast.makeText(this, "Please select Blood Group ", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(city)) {
                    CustCity.setError("Please enter city name ");
                } else if (TextUtils.isEmpty(adds)) {
                    CustAddress.setError("Please enter Address ");
                } else if (TextUtils.isEmpty(pincode)) {
                    CustPincode.setError("Please enter Pincode ");
                } else if (TextUtils.isEmpty(prevoiusMed)) {
                    MedicalHistory.setError("Please enter Previous history ");
                } else {
                    if (Network.isNetworkAvailable(FreeHealthCheckupFormActivity.this)) {

                        Fstname = fname; lstname = lname; msname = mname;
                        custage = age; cstcity = city; cstaddress = adds;
                        cstpincode = pincode; cstprevioushistory = prevoiusMed;

                        new FreeHealthFormCustRegiter().execute();

                    } else {
                        alert = new AlertClass(FreeHealthCheckupFormActivity.this, "No Network Connection");
                    }
                    break;
                }

                default:
                    break;
        }
    }

    private class FreeHealthFormCustRegiter extends AsyncTask<Void, Void, String> {

        private static final String LOG_TAG = "ExampleApp";

        String formRegiAPI = Constants.MAIN_URL + "Register/HealthCheckup?custid="+ UserId +"&FirstName="+ Fstname +"&MiddleName="+ msname +"&LastName="+ lstname +"&Age="+ custage +"&Gender="+ gender +"&BLoodGroup="+ blodgrop +"&Address="+ cstaddress+"&City="+ cstcity +"&Pincode="+ cstpincode +"&AnyPreviousMedicine="+cstprevioushistory;
        String checkUpAPI = formRegiAPI.replaceAll(" ", "%20");

        @Override
        protected String doInBackground(Void... args) {

            HttpURLConnection conn = null;
            final StringBuilder json = new StringBuilder();
            try {
                java.net.URL url = new URL(checkUpAPI);
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
            pDialog = new ProgressDialog(FreeHealthCheckupFormActivity.this);
            pDialog.setMessage("Loading ...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String json) {

            try {
                JSONObject jsonObject1 = new JSONObject(json);
                String FormRegisterresult = jsonObject1.getString("result");
                if (FormRegisterresult.equalsIgnoreCase("true")) {
                    JSONObject jsonObject = jsonObject1.getJSONObject("CustomerHealthCheckup");
                    String customer_id = jsonObject.getString("CustId");
                    String customer_form_id = jsonObject.getString("Id");
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove(PreferenceKey.CHECK_DETAILS);
                    editor.putString(PreferenceKey.CUSTOMER_ID, customer_id);
                    editor.putString(PreferenceKey.CUSTOMER_FORM_ID, customer_form_id);
                    editor.commit();
                    pDialog.hide();
                    Intent regiotp = new Intent(FreeHealthCheckupFormActivity.this, FinalCheckOptionActivity.class);
                    startActivity(regiotp);
                    finish();
                }else {
                    alert = new AlertClass(FreeHealthCheckupFormActivity.this, "Please try again later.");
                }

                pDialog.hide();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent bck = new Intent(this, MainActivity.class);
        startActivity(bck);
        finish();
    }
}
