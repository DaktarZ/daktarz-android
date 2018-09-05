package com.doctor.daktrakzdoctor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.doctor.daktrakzdoctor.list.CheckupListSpinner;
import com.doctor.daktrakzdoctor.list.CheckupTimeListSpinner;
import com.doctor.daktrakzdoctor.utils.AlertClass;
import com.doctor.daktrakzdoctor.utils.Constants;
import com.doctor.daktrakzdoctor.utils.Network;
import com.doctor.daktrakzdoctor.utils.PreferenceKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by amit ji on 7/30/2018.
 */

public class FinalCheckOptionActivity extends AppCompatActivity {

    private Spinner mTxtCheckupTypeSpinner, mTxtCheckupTimeSpinner;
    private ProgressDialog pDialog;
    SharedPreferences prefs;
    String CheckupType, CheckupPrice, checktype,checkupPrice, checktime, checkupid, CheckupTime, CheckupTimeId, ChecktypeId, checktimetypeid;
    ArrayList<CheckupListSpinner> mIgnitionSpinner;
    ArrayList<CheckupTimeListSpinner> mTimeSpinner;
    TimeCheckPlayBack mTimeReports;
    IgnitionPlayBack mIgnitionReports;
    ArrayList<String> spinnerList;
    ArrayList<String> spinnerTimeList;
    AlertClass alert;
    private Button HealthCheckUpConfirm;
    String RegNumb, Customer_form_Id, Cutomer_Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkup_selection);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        RegNumb = prefs.getString(PreferenceKey.MOBILE_NUMBER, "");
        Cutomer_Id = prefs.getString(PreferenceKey.CUSTOMER_ID, "");
        Customer_form_Id = prefs.getString(PreferenceKey.CUSTOMER_FORM_ID, "");

        mTxtCheckupTypeSpinner = (Spinner) findViewById(R.id.edtcheckup_type);
        mTxtCheckupTimeSpinner = (Spinner) findViewById(R.id.edttiming);

        HealthCheckUpConfirm = (Button)findViewById(R.id.btnConfirm);
        HealthCheckUpConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checktype.equalsIgnoreCase("Select Checkup Type")) {
                    Toast.makeText(FinalCheckOptionActivity.this, "Please select first checkup type option.", Toast.LENGTH_LONG).show();
                }else if (checktime.equalsIgnoreCase("Select Checkup Time")) {
                    Toast.makeText(FinalCheckOptionActivity.this, "Please select first checkup time option.", Toast.LENGTH_LONG).show();
                }else {
                     ChecktypeId = checkupPrice;
                     checktimetypeid = checkupid;
                 //    Toast.makeText(FinalCheckOptionActivity.this, ChecktypeId + "," + checktimetypeid, Toast.LENGTH_LONG).show();
                    new ConfirmCheckUpDetails().execute();
                }
            }
        });

        if (Network.isNetworkAvailable(FinalCheckOptionActivity.this)) {
            new servicetoGetCheckUpTypeList().execute();
        }else {
            alert = new AlertClass(FinalCheckOptionActivity.this, "No Network Connection");

        }

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

    private class servicetoGetCheckUpTimeList extends AsyncTask<Void, Void, String> {

        private static final String LOG_TAG = "ExampleApp";

        String CheckupListURL =  Constants.MAIN_URL + "Register/HealthCheckupTimes";

        @Override
        protected String doInBackground(Void... args) {

            HttpURLConnection conn = null;
            final StringBuilder json = new StringBuilder();
            try {
                java.net.URL url = new URL(CheckupListURL);
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
            pDialog = new ProgressDialog(FinalCheckOptionActivity.this);
            pDialog.setMessage("Loading ...");
            pDialog.hide();

        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String json) {

            try {
                JSONObject jsonObject1 = new JSONObject(json);
                String objresult = jsonObject1.getString("result");
                JSONArray arrlist = jsonObject1.getJSONArray("HealthCheckupTime");

                spinnerTimeList=new ArrayList<String>();

                mTimeSpinner=new ArrayList<CheckupTimeListSpinner>();
                mTimeSpinner.add(new CheckupTimeListSpinner("","Select Checkup Time"));

                for(int j = 0; j < arrlist.length(); j++) {
                    JSONObject response;

                    response = arrlist.getJSONObject(j);

                    if(!TextUtils.isEmpty(response.getString("Time")) && !response.getString("Time").equalsIgnoreCase("null"))
                    {
                        CheckupTime=response.get("Time").toString();
                    }
                    if(!TextUtils.isEmpty(response.getString("Id")) && !response.getString("Id").equalsIgnoreCase("null"))
                    {
                        CheckupTimeId=response.get("Id").toString();
                    }
                    mTimeSpinner.add(new CheckupTimeListSpinner(CheckupTimeId, CheckupTime));

                    mTimeReports = new TimeCheckPlayBack(FinalCheckOptionActivity.this, android.R.layout.simple_spinner_item);
                    mTimeReports.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mTimeReports.addAll(mTimeSpinner);
                    mTimeReports.notifyDataSetChanged();
                    mTxtCheckupTimeSpinner.setAdapter(mTimeReports);

                    mTxtCheckupTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapter, View arg1, int position, long id) {

                            // TODO Auto-generated method stub
                            CheckupTimeListSpinner mSpinner= (CheckupTimeListSpinner) adapter.getItemAtPosition(position);
                            checkupid=mSpinner.getTimeid();
                            checktime=mSpinner.getTimetype();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                            // TODO Auto-generated method stub
                        }
                    }) ;

                }

                pDialog.hide();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class ConfirmCheckUpDetails extends AsyncTask<Void, Void, String> {

        private static final String LOG_TAG = "ExampleApp";

        String CheckupListURL =  Constants.MAIN_URL + "Register/SaveHealthCheckup?checkupid="+ Customer_form_Id +"&custid="+ Cutomer_Id +"&time="+ checktimetypeid +"&type="+ChecktypeId;

        @Override
        protected String doInBackground(Void... args) {

            HttpURLConnection conn = null;
            final StringBuilder json = new StringBuilder();
            try {
                java.net.URL url = new URL(CheckupListURL);
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
            pDialog = new ProgressDialog(FinalCheckOptionActivity.this);
            pDialog.setMessage("Loading ...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String json) {

            try {
                JSONObject jsonObject1 = new JSONObject(json);
                String status = jsonObject1.getString("result");
                if (status.equalsIgnoreCase("true")) {
                    JSONObject checkdetails = jsonObject1.getJSONObject("data");
                    String username = checkdetails.getString("FirstName");
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove(PreferenceKey.CHECK_DETAILS);
                    editor.commit();

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(FinalCheckOptionActivity.this);

                    TextView myMsg = new TextView(getApplicationContext());
                    myMsg.setText("Your Checkup Regitration Confirm. Please check message for registration number.");
                    myMsg.setTextSize(16);
                    myMsg.setPadding(0, 20, 0, 0);
                    myMsg.setGravity(Gravity.CENTER);
                    builder1.setView(myMsg);

                    builder1.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.remove(PreferenceKey.USER_CHECKUP_CONFIRM);
                                    editor.putString(PreferenceKey.USER_CHECKUP_CONFIRM, "1");
                                    editor.commit();
                                    Intent thanks = new Intent(FinalCheckOptionActivity.this, MainActivity.class);
                                    startActivity(thanks);
                                    finish();
                                }
                            });


                    AlertDialog alert11 = builder1.create();
                    alert11.setCancelable(false);
                    alert11.show();

                }else {

                }

                pDialog.hide();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class servicetoGetCheckUpTypeList extends AsyncTask<Void, Void, String> {

        private static final String LOG_TAG = "ExampleApp";

        String CheckupListURL =  Constants.MAIN_URL + "Register/GetPackages";

        @Override
        protected String doInBackground(Void... args) {

            HttpURLConnection conn = null;
            final StringBuilder json = new StringBuilder();
            try {
                java.net.URL url = new URL(CheckupListURL);
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
            pDialog = new ProgressDialog(FinalCheckOptionActivity.this);
            pDialog.setMessage("Loading ...");
            pDialog.hide();

        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String json) {

            try {
                JSONObject jsonObject1 = new JSONObject(json);
                JSONArray arrlist = jsonObject1.getJSONArray("packages");

                spinnerList=new ArrayList<String>();

                mIgnitionSpinner=new ArrayList<CheckupListSpinner>();
                mIgnitionSpinner.add(new CheckupListSpinner("Select Checkup Type",""));

                for(int m = 0; m < arrlist.length(); m++) {
                    JSONObject response;

                    response = arrlist.getJSONObject(m);

                    if(!TextUtils.isEmpty(response.getString("Type")) && !response.getString("Type").equalsIgnoreCase("null"))
                    {
                        CheckupType=response.get("Type").toString();
                    }
                    if(!TextUtils.isEmpty(response.getString("Id")) && !response.getString("Id").equalsIgnoreCase("null"))
                    {
                        CheckupPrice=response.get("Id").toString();
                    }
                    mIgnitionSpinner.add(new CheckupListSpinner(CheckupType, CheckupPrice));

                    mIgnitionReports = new IgnitionPlayBack(FinalCheckOptionActivity.this, android.R.layout.simple_spinner_item);
                    mIgnitionReports.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mIgnitionReports.addAll(mIgnitionSpinner);
                    mIgnitionReports.notifyDataSetChanged();
                    mTxtCheckupTypeSpinner.setAdapter(mIgnitionReports);

                    mTxtCheckupTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapter, View arg1, int position, long id) {

                            // TODO Auto-generated method stub
                            CheckupListSpinner mSpinner= (CheckupListSpinner) adapter.getItemAtPosition(position);
                            checktype=mSpinner.getType();
                            checkupPrice=mSpinner.getPayment();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                            // TODO Auto-generated method stub
                        }
                    }) ;

                }
                new servicetoGetCheckUpTimeList().execute();
                pDialog.hide();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class TimeCheckPlayBack extends ArrayAdapter<CheckupTimeListSpinner> {

        public TimeCheckPlayBack(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            TimeCheckPlayBack.ViewHolder holder = null;

            if (view==null) {
                holder = new TimeCheckPlayBack.ViewHolder();
                view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_item, parent,false);
                holder.mSpinnerText=(TextView)view.findViewById(android.R.id.text1);
                view.setTag(holder);
            } else
                holder = (TimeCheckPlayBack.ViewHolder) view.getTag();
            final CheckupTimeListSpinner item=getItem(position);
            String txt=item.getTimetype();
            holder.mSpinnerText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            holder.mSpinnerText.setText(txt);

            return view;
        }
        @Override
        public View getDropDownView(int position, View view, ViewGroup parent) {
            TimeCheckPlayBack.ViewHolder holder=null;

            if (view==null) {
                holder = new TimeCheckPlayBack.ViewHolder();
                view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
                holder.mSpinnerText = (TextView) view.findViewById(android.R.id.text1);
                view.setTag(holder);
            } else
                holder = (TimeCheckPlayBack.ViewHolder) view.getTag();
            final CheckupTimeListSpinner item=getItem(position);
            String txt=item.getTimetype();
            holder.mSpinnerText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            holder.mSpinnerText.setText(txt);
            return view;
        }
        private class ViewHolder {
            TextView mSpinnerText;
        }
    }

    class IgnitionPlayBack extends ArrayAdapter<CheckupListSpinner> {

        public IgnitionPlayBack(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            IgnitionPlayBack.ViewHolder holder = null;

            if (view==null) {
                holder = new IgnitionPlayBack.ViewHolder();
                view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_item, parent,false);
                holder.mSpinnerText=(TextView)view.findViewById(android.R.id.text1);
                view.setTag(holder);
            } else
                holder = (IgnitionPlayBack.ViewHolder) view.getTag();
            final CheckupListSpinner item=getItem(position);
            String txt=item.getType();
            holder.mSpinnerText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            holder.mSpinnerText.setText(txt);

            return view;
        }
        @Override
        public View getDropDownView(int position, View view, ViewGroup parent) {
            IgnitionPlayBack.ViewHolder holder=null;

            if (view==null) {
                holder = new IgnitionPlayBack.ViewHolder();
                view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
                holder.mSpinnerText = (TextView) view.findViewById(android.R.id.text1);
                view.setTag(holder);
            } else
                holder = (IgnitionPlayBack.ViewHolder) view.getTag();
            final CheckupListSpinner item=getItem(position);
            String txt=item.getType();
            holder.mSpinnerText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            holder.mSpinnerText.setText(txt);
            return view;
        }
        private class ViewHolder {
            TextView mSpinnerText;
        }
    }
}
