package com.doctor.daktrakzdoctor.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

public class SaveLog {
    static SharedPreferences mPref;

    public static void saveLogToFile(Context context) {
        BufferedOutputStream buff = null;
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
        ArrayList<String> a = new ArrayList<String>();

        String errorLog = mPref.getString(PreferenceKey.SAVE_LOG, "");
        if (!TextUtils.isEmpty(errorLog)) {
            a = new ArrayList<String>(Arrays.asList(errorLog.split(",")));
        }
        //
        File file = new File(getFullFileName(context, "log"));
        //		if (file.exists()) {
        //			file.delete();
        //		}
        //		mPref.edit().remove(PreferenceKeys.PREF_LOG).commit();

        try {
            //			buff = new BufferedOutputStream(new FileOutputStream(file.getAbsolutePath()));

            //			Process process = Runtime.getRuntime().exec("logcat -d");
            //			BufferedReader bufferedReader = new BufferedReader(
            //					new InputStreamReader(process.getInputStream()));
            //
            //			StringBuilder log=new StringBuilder();
            //			String line;
            //			while ((line = bufferedReader.readLine()) != null) {
            //				log.append(line);
            //			}
            //
            //			buff.write(a.toString().getBytes("UTF-8"));
            FileOutputStream fileOut =
                    new FileOutputStream(file.getAbsolutePath());
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(a);
            out.close();
            fileOut.close();

//			mPref.edit().remove(PreferenceKeys.PREF_LOG).commit();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (buff != null) {
                try {
                    buff.close();
                    buff.flush();
                } catch (Exception e) { /* Don't care */ }
            }
        }
    }

    public static void readLogFromFile(Context context) {
        File file = new File(getFullFileName(context, "log"));

        if (file.exists()) {
            Uri contentUri = FileProvider.getUriForFile(context, "com.humbhi.blackbox.fileprovider", file);
            //			Uri contentUri = Uri.fromFile(file);
            emailLogFile(context, contentUri);
        }
    }

    public static void emailLogFile(Context context, Uri uri) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"jasmine@hitecpoint.in,munish@hitecpoint.in"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "BlackBox Log.");
        intent.putExtra(Intent.EXTRA_TEXT, "Attached is the Blackbox log file.");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        //		context.startActivity(Intent.createChooser(intent, "Send email with..."));
        //		context.startActivityForResult(Intent.createChooser(intent, "Send email with..."),2);
        //		context.st
        //		mPref.edit().remove(PreferenceKeys.PREF_LOG).commit();


        ((Activity) context).startActivityForResult(intent, 6);
    }


    public static String getFullFileName(Context context, String locId) {
        File dir = new File(context.getFilesDir() + System.getProperty("file.separator") + "BlackBoxLog");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return context.getFilesDir() + System.getProperty("file.separator") + "BlackBoxLog" + System.getProperty("file.separator") + locId + ".txt";
    }

}
