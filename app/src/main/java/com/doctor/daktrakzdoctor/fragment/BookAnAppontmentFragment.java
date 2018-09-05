package com.doctor.daktrakzdoctor.fragment;

import android.app.ProgressDialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.doctor.daktrakzdoctor.Adapter.BookListStatusAdapter;
import com.doctor.daktrakzdoctor.R;
import com.doctor.daktrakzdoctor.model.BookalertList;
import com.doctor.daktrakzdoctor.utils.AlertClass;
import com.doctor.daktrakzdoctor.utils.Network;
import com.doctor.daktrakzdoctor.utils.PremissionPhoneCallUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class BookAnAppontmentFragment extends Fragment {

    View view;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    double lattiCut, loggiCut;
    ListView BookanAppointList;
    AlertClass alert;
    private ProgressDialog pDialog;
    public BookListStatusAdapter adapter;
    public ArrayList<BookalertList> list = new ArrayList<BookalertList>();
    String newURL="https://hcp.daktarz.com/api/doctor/GetDoctors";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_book_appointment, container, false);

        BookanAppointList = (ListView) view.findViewById(R.id.lvbook_anappoint);
        adapter = new BookListStatusAdapter(getActivity(), list);
        BookanAppointList.setAdapter(adapter);

        if (Network.isNetworkAvailable(getActivity())) {
            new serviceForGetDoctorData().execute();
        }else {
            alert = new AlertClass(getActivity(), "No Network Connection");
        }

        return view;

    }

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

            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading data...");
         //   pDialog.setCancelable(false);
            pDialog.hide();

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
                            BookalertList dataset = new BookalertList();
                            dataset.setId(jsonObject.getString("Id"));
                            dataset.setDoctorname(jsonObject.getString("DoctorName"));
                            dataset.setLat(jsonObject.getString("Latitude"));
                            dataset.setLag(jsonObject.getString("Langitude"));
                            dataset.setAddress(jsonObject.getString("Address"));
                            dataset.setQualification(jsonObject.getString("Qualification"));
                            dataset.setSpecification(jsonObject.getString("Specification"));
                            dataset.setPhone(jsonObject.getString("Phone"));
                            dataset.setCreatedate(jsonObject.getString("CreatedDate"));

                            list.add(dataset);

                            pDialog.hide();
                        }
                    } else {
                        Toast.makeText(getActivity(), "No Record Found", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(getActivity(), "No Record Found", Toast.LENGTH_LONG).show();
                }
                adapter.notifyDataSetChanged();
                pDialog.hide();

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error processing JSON", e);
            }

        }
    }

}
