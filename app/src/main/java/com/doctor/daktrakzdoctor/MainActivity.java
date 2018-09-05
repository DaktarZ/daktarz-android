package com.doctor.daktrakzdoctor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.doctor.daktrakzdoctor.Adapter.FragmentAdapter;
import com.doctor.daktrakzdoctor.ImageView.CircularImageView;
import com.doctor.daktrakzdoctor.fragment.BookAnAppontmentFragment;
import com.doctor.daktrakzdoctor.fragment.HomeMapFragment;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity  {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    SharedPreferences prefs;
    private TextView CityName;
    String RegNumb, UserId, Custloc, CustCity, Username, CheckConfirm;
    Dialog dialogCheckUp;
    private Button mTxtHealthCheckup;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    // location last updated time
    private String mLastUpdateTime;

    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 50000;

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
        setContentView(R.layout.activity_main);

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

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        RegNumb = prefs.getString(PreferenceKey.MOBILE_NUMBER, "");
        UserId = prefs.getString(PreferenceKey.CUST_ID, "");
        Custloc = prefs.getString(PreferenceKey.USER_ADDRESS, "");
        CustCity = prefs.getString(PreferenceKey.USER_CITY, "");
        Username = prefs.getString(PreferenceKey.USER_NAME, "");
        CheckConfirm = prefs.getString(PreferenceKey.USER_CHECKUP_CONFIRM, "");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initNavigationDrawer();
        initViewPager();

        if (CheckConfirm.equalsIgnoreCase("1")) {

        }else {
            Filteralert();
        }

        CityName = (TextView)findViewById(R.id.city_name);
        CityName.setText(CustCity);


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
                editor.putString(PreferenceKey.USER_CITY, city);
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
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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

    public void initNavigationDrawer() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int id = menuItem.getItemId();

                switch (id) {
                    case R.id.user_profile:
                        Intent profile = new Intent(MainActivity.this, UserProfileActivity.class);
                        startActivity(profile);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.health_checkup:
                        // Filteralert();
                        Intent checkup = new Intent(MainActivity.this, FreeHealthCheckupFormActivity.class);
                        startActivity(checkup);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.home:
                      //  Toast.makeText(getApplicationContext(), "Book an Appointment", Toast.LENGTH_SHORT).show();
                        Intent book = new Intent(MainActivity.this, BookingAppointmentActivity.class);
                        startActivity(book);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.settings:
                        Intent sett = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(sett);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.trash:
                        Toast.makeText(getApplicationContext(), "Diagonistic Test", Toast.LENGTH_SHORT).show();
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.logout:
                    //    finish();
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Confirm")
                                .setMessage("Do you want to log out from Daktarz ?")
                                .setNegativeButton("No", null)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        callToLogOutFunction();
                                    }
                                })
                                .create().show();

                }
                return true;
            }
        });

        View header = navigationView.getHeaderView(0);
       /* LinearLayout imgProfile = (LinearLayout)header.findViewById(R.id.nav_profile);
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profile = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(profile);
            }
        }); */
        TextView tv_name = (TextView) header.findViewById(R.id.user_name);
        tv_name.setText(Username);
        CircularImageView imgView = (CircularImageView)header.findViewById(R.id.circular_image_view);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View v) {
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void initViewPager() {
        mTabLayout = findViewById(R.id.tab_layout_main);
        mViewPager = findViewById(R.id.view_pager_main);

        List<String> titles = new ArrayList<>();
        titles.add(getString(R.string.tab_title_main_1));
        titles.add(getString(R.string.tab_title_main_2));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(1)));

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new HomeMapFragment());
        fragments.add(new BookAnAppontmentFragment());
        mViewPager.setOffscreenPageLimit(2);
        FragmentAdapter mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), fragments, titles);
        mViewPager.setAdapter(mFragmentAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(pageChangeListener);
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
           /* if (position == 2) {
                fab.show();
            } else {
                fab.hide();
            } */
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    //logout function
    private void callToLogOutFunction() {
        // TODO Auto-generated method stub
        SharedPreferences.Editor editor = prefs.edit();     //
        editor.remove(PreferenceKey.CUSTOMER_ID);
        editor.remove(PreferenceKey.CUSTOMER_FORM_ID);
        editor.remove(PreferenceKey.CUST_ID);
        editor.remove(PreferenceKey.USER_NAME);
        editor.remove(PreferenceKey.MOBILE_NUMBER);
        editor.remove(PreferenceKey.CHECK_DETAILS);

        editor.remove(PreferenceKey.USER_LONGNITUDE);
        editor.remove(PreferenceKey.USER_LATITUDE);
        editor.remove(PreferenceKey.USER_ADDRESS);

        editor.commit();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void Filteralert() {
        dialogCheckUp = new Dialog(this, R.style.dialog_style);
        Log.e("error", "error in method");
        dialogCheckUp.setContentView(R.layout.alert_window_btn_design);
        dialogCheckUp.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialogCheckUp.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mTxtHealthCheckup=(Button) dialogCheckUp.findViewById(R.id.btnConfirm);
        Button cancel = (Button) dialogCheckUp.findViewById(R.id.button_close);

        mTxtHealthCheckup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent checkup = new Intent(MainActivity.this, FreeHealthCheckupFormActivity.class);
                startActivity(checkup);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCheckUp.cancel();
            }
        });

        dialogCheckUp.setCancelable(false);
        dialogCheckUp.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
}