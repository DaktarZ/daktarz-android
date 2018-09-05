package com.doctor.daktrakzdoctor.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.doctor.daktrakzdoctor.BookingAppointmentActivity;
import com.doctor.daktrakzdoctor.R;
import com.doctor.daktrakzdoctor.utils.AlertClass;
import com.doctor.daktrakzdoctor.utils.Constants;
import com.doctor.daktrakzdoctor.utils.Network;
import com.doctor.daktrakzdoctor.utils.PreferenceKey;
import com.doctor.daktrakzdoctor.utils.PremissionPhoneCallUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class HomeMapFragment extends Fragment implements View.OnClickListener {

    View view;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    MapView mMapView;
    private ProgressDialog pDialog;
    ArrayList<Double> arrLatitude;
    ArrayList<Double> arrLongitude;
    JSONArray mArrResponse = new JSONArray();
    AlertClass alert;
    HashMap<Marker, Object> hm1;
    boolean mapLocationChk;
    private GoogleMap mMap;
    private LinearLayout DoctorProfile;
    private TextView docname, docQualification, docmobile, docAddress;
    private Button CallDoctor;
    String userid, DoctorName, latitude, longitude, qualification, specification, mobileno,
            usercreatedid, CustLocation, CurrentLat, CurrentLag;

    static SharedPreferences prefs;
    private Button BookAnAppoint;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        CustLocation = prefs.getString(PreferenceKey.USER_ADDRESS, "");
        CurrentLat = prefs.getString(PreferenceKey.USER_LATITUDE, "");
        CurrentLag = prefs.getString(PreferenceKey.USER_LONGNITUDE, "");

        BookAnAppoint = (Button)view.findViewById(R.id.btn_appointmeint);
        BookAnAppoint.setOnClickListener(this);

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        //   setUpMapIfNeeded(rootViewMap);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                boolean success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                getActivity(), R.raw.retro_style));

                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
                mMap.setOnMyLocationClickListener(onMyLocationClickListener);
                enableMyLocationIfPermitted();

                hm1 = new HashMap<>();
                mapLocationChk = false;

                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.setMinZoomPreference(8);

                serviceForLocationOnMap();   //service call
            }

        });

        DoctorProfile = (LinearLayout)view.findViewById(R.id.view_doctor_profile);
        docname = (TextView)view.findViewById(R.id.tv_doc_name);
        docQualification = (TextView)view.findViewById(R.id.tv_qualification);
        docmobile = (TextView)view.findViewById(R.id.tv_mobileno);
        docAddress = (TextView) view.findViewById(R.id.tv_adress);

        CallDoctor = (Button) view.findViewById(R.id.btncall);

        return view;

    }

    //call to service
    private void serviceForLocationOnMap() {
        // TODO Auto-generated method stub

        if (Network.isNetworkAvailable(getActivity())) {
            new ServiceMarkerMapView().execute();
        } else {
            alert = new AlertClass(getActivity(), "No Network Connection");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_appointmeint:
                Intent profile = new Intent(getActivity(), BookingAppointmentActivity.class);
                startActivity(profile);
                break;
            default:
                break;
        }
    }

    private class ServiceMarkerMapView extends AsyncTask<Void, Void, String> {

        private static final String LOG_TAG = "ExampleApp";

        private final String AlertStatus = "https://hcp.daktarz.com/api/doctor/GetDoctors";

        @Override
        protected String doInBackground(Void... args) {

            HttpURLConnection conn = null;
            final StringBuilder json = new StringBuilder();
            try {
                // Connect to the web service
                URL url = new URL(AlertStatus);
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
            pDialog.hide();

        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String json) {

            try {
                JSONObject Obj = new JSONObject(json);
                String rst = Obj.getString("result");
                if (rst.equalsIgnoreCase("true")) {
                    JSONArray jsonArray = Obj.getJSONArray("data");
                    arrLatitude = new ArrayList<Double>();
                    arrLongitude = new ArrayList<Double>();
                    if (jsonArray.length() > 0) {
                        mArrResponse = jsonArray;
                        showMap(jsonArray);
                    } else {
                        //   mTxtNoRecord.setVisibility(View.VISIBLE);
                    }

                }else  {
                    alert = new AlertClass(getActivity(), "no record found.");
                }
                pDialog.hide();

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error processing JSON", e);
            }

        }
    }

    public void showMap(JSONArray arr){
        try {

            if(arr.length()>0)
            {
                mMap.clear();

                for(int i = 0; i < arr.length(); i++)
                {
                    JSONObject response;

                    response = arr.getJSONObject(i);

                        {
                        if(!TextUtils.isEmpty(response.getString("Id")) && !response.getString("Id").equalsIgnoreCase("null"))
                        {
                            userid=response.get("Id").toString();
                        }
                        if(!TextUtils.isEmpty(response.getString("DoctorName")) && !response.getString("DoctorName").equalsIgnoreCase("null"))
                         {
                        DoctorName=response.get("DoctorName").toString();
                         }
                        if(!TextUtils.isEmpty(response.getString("Latitude")) && !response.getString("Latitude").equalsIgnoreCase("null"))
                        {
                            latitude=response.get("Latitude").toString();
                        }
                        if(!TextUtils.isEmpty(response.getString("Langitude")) && !response.getString("Langitude").equalsIgnoreCase("null"))
                        {
                            longitude=response.get("Langitude").toString();
                        }
                        if(!TextUtils.isEmpty(response.getString("Qualification")) && !response.getString("Qualification").equalsIgnoreCase("null"))
                        {
                            qualification=response.get("Qualification").toString();
                        }
                        if(!TextUtils.isEmpty(response.getString("Specification")) && !response.getString("Specification").equalsIgnoreCase("null"))
                        {
                            specification=response.get("Specification").toString();
                        }
                        if(!TextUtils.isEmpty(response.getString("Phone")) && !response.getString("Phone").equalsIgnoreCase("null"))
                        {
                            mobileno=response.get("Phone").toString();
                        }
                        if(!TextUtils.isEmpty(response.getString("CreatedDate")) && !response.getString("CreatedDate").equalsIgnoreCase("null"))
                        {
                            usercreatedid=response.get("CreatedDate").toString();
                        }

                        double lat=Double.parseDouble(latitude);
                        double log=Double.parseDouble(longitude);
                        if(lat!=0)
                            arrLatitude.add(lat);
                        if(log!=0)
                            arrLongitude.add(log);
                        if(mMap!=null){
                            Marker m=mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng( lat,log)));
                            m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.doctor_marker));
                            hm1.put(m, response);

                            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter()
                            {
                                HashMap<Marker, Object> hm=hm1;

                                @RequiresApi(api = Build.VERSION_CODES.O)
                                public View getInfoWindow(final Marker marker)
                                {
                                    // TODO Auto-generated method stub
                                    JSONObject d=(JSONObject) hm.get(marker);
                                  /*  LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                                    View v=layoutInflater.inflate(R.layout.set_custom_marker, null, true);
                                    TextView vehicleName=(TextView) v.findViewById(R.id.tvmVehicleName);
                                    ImageView currentStatus=(ImageView) v.findViewById(R.id.ivlocstatus);
                                    TextView vehicleStatus=(TextView) v.findViewById(R.id.tvmCurrentLocation);
                                    TextView vehicleSpeed=(TextView) v.findViewById(R.id.tvmCurrentSpeed);
                                    TextView vehicleDistance=(TextView) v.findViewById(R.id.tvmDistance);
                                    TextView vehicleTime=(TextView) v.findViewById(R.id.tvmCurrenttime); */

                                    DoctorProfile.setVisibility(View.VISIBLE);

                                    if(d!=null)	{
                                        if(d.length()>0)
                                        {
                                            if(marker!=null)
                                            {
                                                if(d!=null)
                                                {
                                                    try {
                                                        docname.setText(d.getString("DoctorName"));
                                                        docAddress.setText(d.getString("Address"));
                                                        docQualification.setText(d.getString("Qualification"));
                                                        docmobile.setText(d.getString("Phone"));
                                                        final String datatime = d.getString("Specification");

                                                        final String doccall =  d.getString("Phone");


                                                    //    vehicleTime.setText(datatime);

                                                       /* if(!prefs.getBoolean(PreferenceKey.IS_SUB_USER, false)){
                                                            vehicleSpeed.setVisibility(View.VISIBLE);
                                                            v.findViewById(R.id.tv3).setVisibility(View.VISIBLE);
                                                        }else{
                                                            vehicleSpeed.setVisibility(View.GONE);
                                                            v.findViewById(R.id.tv3).setVisibility(View.GONE);
                                                        } */
                                                        CallDoctor.setOnClickListener(new View.OnClickListener() {

                                                            @Override
                                                            public void onClick(View v) {
                                                                if (PremissionPhoneCallUtils.checkPermissions(getActivity(), PremissionPhoneCallUtils.PERMISSIONS_CALL)) {
                                                                    Uri callUri = Uri.parse(Constants.PHONE_CALL + doccall);
                                                                    Intent callIntent = new Intent(Intent.ACTION_CALL, callUri);
                                                                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                                                                    startActivity(callIntent);
                                                                } else {
                                                                    requestCallPermissions();
                                                                }
                                                         //       Toast.makeText(getActivity(), "work in progress", Toast.LENGTH_LONG).show();
                                                            }
                                                        });

                                                   //     return v;

                                                    } catch (JSONException e) {
                                                        // TODO Auto-generated catch block
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }else{
                                            return null;
                                        }
                                    }
                                    return null;
                                }
                                public View getInfoContents(Marker marker)
                                {
                                    return null;
                                }

                            });

                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    DoctorProfile.setVisibility(View.GONE);
                                }
                            });

                        }
                    }
                }
            }
            double minLat = Double.MAX_VALUE;
            double maxLat = Double.MIN_VALUE;
            double minLon = Double.MAX_VALUE;
            double maxLon = Double.MIN_VALUE;
            if(arrLatitude.size()>0){
                for (int i = 0; i < arrLatitude.size(); i++) {
                    double lat=arrLatitude.get(i);
                    double lon=arrLongitude.get(i);

                    maxLat = Math.max(lat, maxLat);
                    minLat = Math.min(lat, minLat);
                    maxLon = Math.max(lon, maxLon);
                    minLon = Math.min(lon, minLon);
                }
                LatLng coordinate = new LatLng((maxLat + minLat)/2,
                        (maxLon + minLon)/2);
                CameraUpdate Location = CameraUpdateFactory.newLatLngZoom(coordinate,15);
                if(mMap!=null)
                    mMap.animateCamera(Location);
            }

            else
            {
                //   mTxtNoRecord.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public void onDestroy()
    {
        if(mMapView!=null)
            mMapView.removeAllViews();
        super.onDestroy();
    }
    @Override
    public void onResume()
    {
        super.onResume();
        if(mMapView!=null)
            mMapView.onResume();
    }
    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        if(mMapView!=null)
            mMapView.onLowMemory();
    }
    @Override
    public void onDestroyView()
    {
        mMap=null;
        if(mMapView!=null)
            mMapView.removeAllViews();
        super.onDestroyView();
    }

    private void requestCallPermissions() {
        ActivityCompat.requestPermissions(getActivity(), PremissionPhoneCallUtils.PERMISSIONS_CALL,
                PremissionPhoneCallUtils.REQUEST_PHONE_CALL);
    }

    private void enableMyLocationIfPermitted() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void showDefaultLocation() {
        Toast.makeText(getActivity(), "Location permission not granted, " +
                        "showing default location",
                Toast.LENGTH_SHORT).show();
        LatLng redmond = new LatLng(47.6739881, -122.121512);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(redmond));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocationIfPermitted();
                } else {
                    showDefaultLocation();
                }
                return;
            }

        }
    }

    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener =
            new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    mMap.setMinZoomPreference(8);
                    return false;
                }
            };

    private GoogleMap.OnMyLocationClickListener onMyLocationClickListener =
            new GoogleMap.OnMyLocationClickListener() {
                @Override
                public void onMyLocationClick(@NonNull Location location) {

                    mMap.setMinZoomPreference(8);

                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(new LatLng(location.getLatitude(),
                            location.getLongitude()));

                    circleOptions.radius(200);
                    circleOptions.fillColor(Color.RED);
                    circleOptions.strokeWidth(6);

                    mMap.addCircle(circleOptions);
                }
            };

}