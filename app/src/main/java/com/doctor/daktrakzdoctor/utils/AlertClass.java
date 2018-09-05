package com.doctor.daktrakzdoctor.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.TextView;

public class AlertClass {

	Context context;
	String message;

	public AlertClass(Context context, String message) {
		this.context = context;
		this.message=message;
		dialog(context,message);
	}


	public void dialog(Context context, String message){

		AlertDialog.Builder builder1 = new AlertDialog.Builder(context);

		TextView myMsg = new TextView(context);
		myMsg.setText(message);
		myMsg.setTextSize(16); 
		myMsg.setPadding(0, 20, 0, 0);
		myMsg.setGravity(Gravity.CENTER);
		builder1.setView(myMsg);


		builder1.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});


		AlertDialog alert11 = builder1.create();
		alert11.setCancelable(false);
		alert11.show();
	}
}
