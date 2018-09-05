package com.doctor.daktrakzdoctor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.doctor.daktrakzdoctor.utils.AlertClass;
import com.doctor.daktrakzdoctor.utils.Network;
import com.doctor.daktrakzdoctor.utils.PreferenceKey;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by amit ji on 8/19/2018.
 */

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private AppCompatButton SaveDetails;
    private EditText Username, EmailId, UserAddress, DateofBirth, UserGender, Contactno, BloodGroup;
    AlertClass alert;
    String username, email, address;
    private ProgressDialog pDialog;
    String CreateUserURL = "https://www.daktarz.com/api/Register/UpdateUserProfile";
    SharedPreferences prefs;
    private FloatingActionButton ImageUpdate;
    private CircleImageView ImgChangePic;
    ImageView LocationView;
    String UserId, RegNumb, birthdate, gender, adaarno, contactno, bloodgroup;
    protected static final int CAMERA_REQUEST = 0;
    protected static final int GALLERY_PICTURE = 1;
    private Intent pictureActionIntent = null;
    Bitmap bitmap;
    String selectedImagePath;

    private static final int PLACE_PICKER_REQUEST = 1;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        RegNumb = prefs.getString(PreferenceKey.MOBILE_NUMBER, "");
        UserId = prefs.getString(PreferenceKey.CUST_ID, "");

        SaveDetails = (AppCompatButton)findViewById(R.id.btn_login);

        ImgChangePic = (CircleImageView)findViewById(R.id.imageview_account_profile);
        ImageUpdate = (FloatingActionButton)findViewById(R.id.floatingActionButton);
        ImageUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialog();
            }
        });

        Username = (EditText)findViewById(R.id.username);
        EmailId = (EditText)findViewById(R.id.email_id);
        UserAddress = (EditText)findViewById(R.id.user_address);
        DateofBirth = (EditText)findViewById(R.id.date_of_birth);
        UserGender = (EditText)findViewById(R.id.gender);
        Contactno = (EditText)findViewById(R.id.contact);
        BloodGroup = (EditText)findViewById(R.id.blood_groud);

        Contactno.setText(RegNumb);
        ImgChangePic.setImageResource(R.drawable.ic_launcher);

        LocationView = (ImageView)findViewById(R.id.location_icon);
        LocationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PlacePicker.IntentBuilder intentBuilder =
                            new PlacePicker.IntentBuilder();
                    intentBuilder.setLatLngBounds(BOUNDS_MOUNTAIN_VIEW);
                    Intent intent = intentBuilder.build(UserProfileActivity.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);

                } catch (GooglePlayServicesRepairableException
                        | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                /*try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(UserProfileActivity.this);
                    startActivityForResult(intent, 1);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }*/
            }
        });

        SaveDetails.setOnClickListener(this);

    }

    private void startDialog() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(UserProfileActivity.this);
        myAlertDialog.setTitle("Upload Pictures Option");
        myAlertDialog.setMessage("How do you want to set your picture?");

        myAlertDialog.setPositiveButton("Gallery",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent pictureActionIntent = null;

                        pictureActionIntent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(
                                pictureActionIntent,
                                GALLERY_PICTURE);

                    }
                });

        myAlertDialog.setNegativeButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {

                        Intent intent = new Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE);
                        File f = new File(android.os.Environment
                                .getExternalStorageDirectory(), "temp.jpg");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(f));

                        startActivityForResult(intent,
                                CAMERA_REQUEST);

                    }
                });
        myAlertDialog.show();
    }

    // A place has been received; use requestCode to track the request.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

       /* bitmap = null;
        selectedImagePath = null;

        if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST) {

            File f = new File(Environment.getExternalStorageDirectory()
                    .toString());
            for (File temp : f.listFiles()) {
                if (temp.getName().equals("temp.jpg")) {
                    f = temp;
                    break;
                }
            }

            if (!f.exists()) {

                Toast.makeText(getBaseContext(),

                        "Error while capturing image", Toast.LENGTH_LONG)

                        .show();

                return;

            }

            try {

                bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());

                bitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true);

                int rotate = 0;
                try {
                    ExifInterface exif = new ExifInterface(f.getAbsolutePath());
                    int orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);

                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotate = 270;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotate = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotate = 90;
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(rotate);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, true);


                ImgChangePic.setImageBitmap(bitmap);
                //storeImageTosdCard(bitmap);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (resultCode == RESULT_OK && requestCode == GALLERY_PICTURE) {
            if (data != null) {

                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath,
                        null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                selectedImagePath = c.getString(columnIndex);
                c.close();

                if (selectedImagePath != null) {
                  //  txt_image_path.setText(selectedImagePath);
                }

                bitmap = BitmapFactory.decodeFile(selectedImagePath); // load
                // preview image
                bitmap = Bitmap.createScaledBitmap(bitmap, 180, 180, true);

                ImgChangePic.setImageBitmap(bitmap);

            } else {
                Toast.makeText(getApplicationContext(), "Cancelled",
                        Toast.LENGTH_SHORT).show();
            }
        }*/

        if (requestCode == PLACE_PICKER_REQUEST
                && resultCode == Activity.RESULT_OK) {

            final Place place = PlacePicker.getPlace(this, data);
            final LatLng latt = place.getLatLng();
            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            String attributions = (String) place.getAttributions();
            if (attributions == null) {
                attributions = "";
            }

         /*   mName.setText(name + ",  " + latt);
            mAddress.setText(address);
            mAttributions.setText(Html.fromHtml(attributions));*/

            UserAddress.setText(address);
          //  mAttributions.setText(Html.fromHtml(attributions));

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        /*if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.e("Tag", "Place: " + place.getAddress() + place.getPhoneNumber());


                UserAddress.setText(place.getName()+",\n"+
                        place.getAddress() +"\n" + place.getPhoneNumber());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.e("Tag", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }*/
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                String Uname = Username.getText().toString();
                String UEmail = EmailId.getText().toString();
                String Uaddress = UserAddress.getText().toString();
                String UdateofBirth = DateofBirth.getText().toString();
                String Ugender = UserGender.getText().toString();
                String UContactno = Contactno.getText().toString();
                String Ubloodgroup = BloodGroup.getText().toString();

                Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(),  R.drawable.ic_launcher);
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 100, bao);
                byte [] ba = bao.toByteArray();
                final String ba1= Base64.encodeToString(ba,Base64.DEFAULT);

                if (TextUtils.isEmpty(Uname)) {
                    Username.setError("Enter username first");
                }else if (TextUtils.isEmpty(UEmail)) {
                    EmailId.setError("Enter your Email Id");
                }else if (TextUtils.isEmpty(Uaddress)) {
                    UserAddress.setError("Enter your address");
                }else if (TextUtils.isEmpty(UdateofBirth)) {
                    DateofBirth.setError(" Enter correct Date of Birth DD/MM/YYYY ");
                }else if (TextUtils.isEmpty(Ugender)) {
                    UserGender.setError(" Enter Your Gender ");
                } else if (TextUtils.isEmpty(UContactno)) {
                    Contactno.setError(" Enter Your Contact no ");
                } else if (TextUtils.isEmpty(Ubloodgroup)) {
                    BloodGroup.setError(" Enter Your Contact no ");
                } else {
                    if (Network.isNetworkAvailable(this)) {
                        username = Uname;
                        address = Uaddress;
                        email = UEmail;
                        birthdate = UdateofBirth;
                        gender = Ugender;
                        contactno = UContactno;
                        bloodgroup = Ubloodgroup;
                        RequestQueue queue = Volley.newRequestQueue(this);
                        String url = CreateUserURL;

                        StringRequest strRequest = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(response);
                                            String result = jsonObject.getString("result");
                                            if (result.equalsIgnoreCase("0")) {
                                                alert = new AlertClass(UserProfileActivity.this, "Username is already registered. Please change username");
                                            }else {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
                                                builder.setMessage("Account has been Created!")
                                                        .setCancelable(false)
                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                Intent bck = new Intent(UserProfileActivity.this, MainActivity.class);
                                                                startActivity(bck);
                                                                finish();
                                                            }
                                                        });
                                                AlertDialog alert = builder.create();
                                                alert.show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(UserProfileActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("Content-Type", "application/json");
                                params.put("id", UserId);
                                params.put("Name", username);
                                params.put("Address", address);
                                params.put("EmailId", email);
                                params.put("Latitude", "30.67899");
                                params.put("Longitude", "70.67899");
                                params.put("Gender", gender);
                                params.put("ProfilePic", ba1);
                                return params;
                            }
                        };
                        strRequest.setRetryPolicy(new DefaultRetryPolicy(
                                20000,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        queue.add(strRequest);

                    }else {
                        alert = new AlertClass(this, "No Network Connection");
                    }
                }
                break;
            default:
                break;
        }
    }
}
