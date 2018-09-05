package com.doctor.daktrakzdoctor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.doctor.daktrakzdoctor.Adapter.DoctorBookingAdapter;
import com.doctor.daktrakzdoctor.model.DoctorBookList;
import com.doctor.daktrakzdoctor.utils.AlertClass;
import com.doctor.daktrakzdoctor.utils.Network;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class BookingAppointmentActivity extends AppCompatActivity{

    private EditText mEdtSearch;
    String newURL="https://www.daktarz.com/api/doctor/GetAllDoctors";
    SharedPreferences prefs;
    AlertClass alert;
    private ProgressDialog pDialog;
    ListView BookanAppointList;
    DoctorBookingAdapter adapter;
    public ArrayList<DoctorBookList> booklist = new ArrayList<DoctorBookList>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mEdtSearch = (EditText) findViewById(R.id.edtlivesearch);
        mEdtSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, 0, 0);

        hideSoftKeyboard(this);

        BookanAppointList = (ListView) findViewById(R.id.lvbook_anappoint);
        adapter = new DoctorBookingAdapter(this, booklist);
        BookanAppointList.setAdapter(adapter);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (Network.isNetworkAvailable(this)) {
            new serviceForGetDoctorData().execute();
        }else {
            alert = new AlertClass(this, "No Network Connection");
        }

        mEdtSearch.addTextChangedListener(filterTextWatcher);

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

    private TextWatcher filterTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            adapter.getFilter().filter(s.toString());
        }

    };

    private class serviceForGetDoctorData extends AsyncTask<Void, Void, String> {

        private static final String LOG_TAG = "ExampleApp";

        private final String IgnitionURL = newURL;

        @Override
        protected String doInBackground(Void... args) {

            HttpURLConnection conn = null;
            final StringBuilder json = new StringBuilder();
            try {
                // Connect to the web service
                URL url = new URL(IgnitionURL);
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

            pDialog = new ProgressDialog(BookingAppointmentActivity.this);
            pDialog.setMessage("Loading data...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String json) {

            try {
                JSONObject obj = new JSONObject(json);
                String status = obj.getString("result");
                if (status.equalsIgnoreCase("true")) {
                    JSONArray jsonArray1 = obj.getJSONArray("data");
                    if (jsonArray1.length() > 0) {
                        for (int i = 0; i < jsonArray1.length(); i++) {
                            JSONObject jsonObject = jsonArray1.getJSONObject(i);
                            DoctorBookList dataset = new DoctorBookList();
                            dataset.setId(jsonObject.getString("Id"));
                            dataset.setDoctorname(jsonObject.getString("Name"));
                            dataset.setLat(jsonObject.getString("latitude"));
                            dataset.setLag(jsonObject.getString("longitude"));
                            dataset.setAddress(jsonObject.getString("Address"));
                            dataset.setQualification(jsonObject.getString("Education"));
                            dataset.setSpecification(jsonObject.getString("Services"));
                            dataset.setPhone(jsonObject.getString("Mobile"));
                            dataset.setEmailid(jsonObject.getString("Emailid"));
                            dataset.setDoctor_img(jsonObject.getString("ProfilePic"));

                            booklist.add(dataset);

                            pDialog.hide();
                        }
                    } else {
                        Toast.makeText(BookingAppointmentActivity.this, "No Record Found", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(BookingAppointmentActivity.this, "No Record Found", Toast.LENGTH_LONG).show();
                }
                adapter.notifyDataSetChanged();
                pDialog.hide();

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error processing JSON", e);
            }

        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

}
