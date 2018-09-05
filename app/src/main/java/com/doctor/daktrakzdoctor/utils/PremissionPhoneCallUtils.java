package com.doctor.daktrakzdoctor.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.SparseArray;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Administrator on 3/19/2018.
 */

public abstract class PremissionPhoneCallUtils {

    @Retention(RetentionPolicy.CLASS)
    public @interface PermissionTypeDef{}
    public static final int REQUEST_ACCOUNT = 1;
    public static final int REQUEST_PHONE_CALL = 2;

    public static final String[] PERMISSIONS_ACCOUNT= {
//            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    public static final String[] PERMISSIONS_CALL= {
            Manifest.permission.CALL_PHONE
    };

    public static final SparseArray<String[]> mTypeToArray;
    static {
        mTypeToArray = new SparseArray<>(4);
        mTypeToArray.put(REQUEST_ACCOUNT, PERMISSIONS_ACCOUNT);
        mTypeToArray.put(REQUEST_PHONE_CALL, PERMISSIONS_CALL);
    }

    public static String[] getPermissionArray(@PermissionTypeDef int type) {
        return mTypeToArray.get(type);
    }

    public static boolean checkPermissions(@NonNull final Context context,
                                           @NonNull String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean shouldShowRationale(@NonNull final Activity activity,
                                              @NonNull String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
     *
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     */
    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if(grantResults.length < 1) { return false; }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) { return false; }
        }
        return true;
    }
}