package com.doctor.daktrakzdoctor;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.doctor.daktrakzdoctor.utils.AlertClass;
import com.doctor.daktrakzdoctor.utils.Constants;
import com.doctor.daktrakzdoctor.utils.HexCoder;
import com.doctor.daktrakzdoctor.utils.Network;
import com.doctor.daktrakzdoctor.utils.PreferenceKey;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import butterknife.ButterKnife;
import static android.content.ContentValues.TAG;

public class LoginActivity extends Activity implements View.OnClickListener {

    private Button btnLogin;
    AlertClass alert;
    private ProgressDialog pDialog;
    private EditText mobileNumber;
    String UserNumber, CheckDetails;
    static SharedPreferences prefs;
    private TextView AlreadyRegisterUser;

    // location last updated time
    private String mLastUpdateTime;

    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 500000;

    private static final int REQUEST_CHECK_SETTINGS = 100;

    // bunch of location related apis
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    // boolean flag to toggle the ui
    private Boolean mRequestingLocationUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        ButterKnife.bind(this);

        init();

        // restore the values from saved instance state
        restoreValuesFromBundle(savedInstanceState);

        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(this)
                .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

        CheckDetails = prefs.getString(PreferenceKey.CHECK_DETAILS, "");

        if (CheckDetails.equalsIgnoreCase("1")) {
            Intent otp = new Intent(this, SignUpActivity.class);
            startActivity(otp);
            this.finish();
        }else if (CheckDetails.equalsIgnoreCase("2")) {
            Intent otp = new Intent(this, MainActivity.class);
            startActivity(otp);
            this.finish();
        }
        setContentView(R.layout.activity_login);

       /* ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false); // disable the button
            actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(false); // remove the icon
        } */

        AlreadyRegisterUser = (TextView)findViewById(R.id.tv_register_user);
        AlreadyRegisterUser.setOnClickListener(this);

        mobileNumber = (EditText)findViewById(R.id.edt_mobile);

        btnLogin = (Button)findViewById(R.id.btnlogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String MobNumb = mobileNumber.getText().toString();
                if (TextUtils.isEmpty(MobNumb)) {
                    mobileNumber.setError("Enter Mobile number first");
                } else {
                    if (Network.isNetworkAvailable(LoginActivity.this)) {
                        String Regex = "[^\\d]";
                        String PhoneDigits = MobNumb.replaceAll(Regex, "");
                        if (PhoneDigits.length()!=10)
                        {
                            mobileNumber.setError("Please enter 10 digit mobile number only");
                        }
                        else
                        {
                            UserNumber  = "91" + MobNumb;
                            new RegisterUserLogin().execute();
                        }
                    } else {
                        alert = new AlertClass(LoginActivity.this, "No Network Connection");
                    }
                }
            }
        });

    }

    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                updateLocationUI();
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
    //    mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
    //    mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_register_user:
                Intent login = new Intent(this, AlreadyLoginUserActivity.class);
                startActivity(login);
            default:
                break;

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private class RegisterUserLogin extends AsyncTask<Void, Void, String> {

        private static final String LOG_TAG = "ExampleApp";

        String RegiAPI = Constants.MAIN_URL + "Register/LoginUser?MobileNumber="+ UserNumber +"&devicetype="+ getDeviceID(LoginActivity.this) +"&TokenId=3&UserType=1&DeviceName=android";

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
            pDialog = new ProgressDialog(LoginActivity.this);
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
                if (Registerresult.equalsIgnoreCase("Success")) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(PreferenceKey.MOBILE_NUMBER, UserNumber);
                    editor.putString(PreferenceKey.CHECK_DETAILS, "1");
                    editor.commit();
                    Intent regiotp = new Intent(LoginActivity.this, OtpVerifyActivity.class);
                    startActivity(regiotp);
                    finish();
                }else if (Registerresult.equalsIgnoreCase("Already Register")){
                    alert = new AlertClass(LoginActivity.this, "This number is already register.");
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

    /**
     * Update the UI displaying the location data
     * and toggling the buttons
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null) {
         //   Toast.makeText(this, "Lat: " + mCurrentLocation.getLatitude() + ", " + "Lng: " + mCurrentLocation.getLongitude(), Toast.LENGTH_LONG).show();

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();

                SharedPreferences.Editor editor = prefs.edit();
                editor.remove(PreferenceKey.USER_LATITUDE);
                editor.remove(PreferenceKey.USER_LONGNITUDE);
                editor.remove(PreferenceKey.USER_ADDRESS);
                editor.putString(PreferenceKey.USER_LATITUDE, String.valueOf(mCurrentLocation.getLatitude()));
                editor.putString(PreferenceKey.USER_LONGNITUDE, String.valueOf(mCurrentLocation.getLongitude()));
                editor.putString(PreferenceKey.USER_ADDRESS, address);
                editor.commit();

            //    Toast.makeText(this, address, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Restoring values from saved instance state
     */
    private void restoreValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
            }

            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
            }

            if (savedInstanceState.containsKey("last_updated_on")) {
                mLastUpdateTime = savedInstanceState.getString("last_updated_on");
            }
        }

        updateLocationUI();
    }

    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //    Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocationUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(LoginActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        updateLocationUI();
                    }
                });
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Resuming location updates depending on button state and
        // allowed permissions
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        }

        updateLocationUI();
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();
                        //   toggleButtons();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mRequestingLocationUpdates) {
            // pausing location updates
            stopLocationUpdates();
        }
    }

}
