package com.doctor.daktrakzdoctor.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network {

	public static boolean isNetworkAvailable(Context context){
		if(context!=null){
			// Using ConnectivityManager to check for Network Connection
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
			return activeNetworkInfo != null;
		}
		return false;
	}

}
