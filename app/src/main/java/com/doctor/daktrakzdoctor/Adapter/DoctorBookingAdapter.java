package com.doctor.daktrakzdoctor.Adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.TextView;
import com.doctor.daktrakzdoctor.R;
import com.doctor.daktrakzdoctor.model.DoctorBookList;
import com.doctor.daktrakzdoctor.utils.Constants;
import com.doctor.daktrakzdoctor.utils.PremissionPhoneCallUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorBookingAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    public ArrayList<DoctorBookList> DataList;
    public ArrayList<DoctorBookList> arryList;
    private Context context;
    private Button CallDoctor;
    public boolean isFirstTime = true, isCompactView;
    TextView mTxtDoctorName, mTxtDoctorAddress, mTxtDoctorQualification, mTxtDocMobile;
    private CircleImageView mTxtProfilePics;

    public DoctorBookingAdapter(Context context, ArrayList<DoctorBookList> billionairesItems) {
        this.context = context;
        this.DataList = billionairesItems;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

   public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<DoctorBookList> results = new ArrayList<DoctorBookList>();
                if (arryList == null)
                    arryList = DataList;
                if (constraint != null) {
                    if (arryList != null && arryList.size() > 0) {
                        for (final DoctorBookList g : arryList) {
                            if (g.getDoctorname().toLowerCase()
                                    .contains(constraint.toString()))
                                results.add(g);
                        }
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                DataList = (ArrayList<DoctorBookList>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return DataList.size();
    }

    @Override
    public Object getItem(int location) {
        return DataList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.set_booking_listview, null);

        mTxtDoctorName = (TextView) convertView.findViewById(R.id.tv_doc_name);
        mTxtDoctorAddress = (TextView) convertView.findViewById(R.id.tv_adress);
        mTxtDoctorQualification = (TextView) convertView.findViewById(R.id.tv_qualification);
        mTxtDocMobile = (TextView) convertView.findViewById(R.id.tv_mobileno);
        mTxtProfilePics = (CircleImageView)convertView.findViewById(R.id.profile_image);

        CallDoctor = (Button) convertView.findViewById(R.id.btncall);

        final DoctorBookList m = DataList.get(position);

        mTxtDoctorName.setText(m.getDoctorname());
        mTxtDocMobile.setText(m.getPhone());
        mTxtDoctorAddress.setText(m.getAddress());
        String qualfy = m.getQualification();
        String qualify_final = qualfy.replaceAll("\n", ", ");
        mTxtDoctorQualification.setText(qualify_final);

        Picasso.with(context).load("https://hcp.daktarz.com" + m.getDoctor_img()).into(mTxtProfilePics);

        CallDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PremissionPhoneCallUtils.checkPermissions(context, PremissionPhoneCallUtils.PERMISSIONS_CALL)) {
                    Uri callUri = Uri.parse(Constants.PHONE_CALL + m.getPhone());
                    Intent callIntent = new Intent(Intent.ACTION_CALL, callUri);
                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    context.startActivity(callIntent);
                } else {
                    requestCallPermissions();
                }
            }
        });

        return convertView;
    }

    private void requestCallPermissions() {
        ActivityCompat.requestPermissions((Activity) context, PremissionPhoneCallUtils.PERMISSIONS_CALL,
                PremissionPhoneCallUtils.REQUEST_PHONE_CALL);
    }

}