package com.test.slideshow.utilities.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import com.test.slideshow.R;
import com.test.slideshow.models.QueryViewModel;

import java.util.Date;

/**
 * Created by Nikita on 17.10.2014.
 */
public class Prefs {



    public static final String PREFS_ROOT = "ROOT";
    public static final String PREFS_EXT_DIR = "DIR_KEY_EXT";
    public static final String PREFS_INT_DIR = "DIR_KEY_INT";

    public static final String PREFS_IS_INITED = "IS_INITED";;
    public static String getRootDir(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean isExtUsed = prefs.getBoolean(context.getString(R.string.ext_dir_key), true);

        return getRootDir(context, isExtUsed);
    }

    public static boolean getIsInited(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean isInited = prefs.getBoolean(PREFS_IS_INITED, false);

        return isInited;
    }

    public static void setIsInited(Context context, boolean value, String dir){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(PREFS_IS_INITED, value);
        editor.putString(PREFS_INT_DIR, dir);
        editor.commit();
    }

    private static String getRootDir(Context context, Boolean isExtDir){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (isExtDir)
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        else {
            String dir = prefs.getString(PREFS_INT_DIR, null);
            if (dir == null)
                throw new NullPointerException("Initialization went wrong");

            return dir;

        }
    }

    public static void setDir(Context context, String newDir){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Boolean isExtUsed = prefs.getBoolean(context.getString(R.string.ext_dir_key), true);
        editor.putString(isExtUsed ? PREFS_EXT_DIR : PREFS_INT_DIR, newDir);

        // Commit the edits!
        editor.commit();
    }

    public static String getDir(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //using internal dir as default cause it is already populated with several images
        String dir = prefs.getString(isExternalDirNow(context) ? PREFS_EXT_DIR : PREFS_INT_DIR, null);
        if (dir == null)
            dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (dir == null)
            throw new NullPointerException("Initialization went wrong");

        return dir;
    }

    public static boolean isExternalDirNow(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.ext_dir_key), true);
    }



    public static int getInterval(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int interval = prefs.getInt(context.getString(R.string.interval_key), 5);

        return interval;
    }

    public static QueryViewModel getQueryViewModel(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Uri uri = isExternalDirNow(context) ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI : MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        String folder = getDir(context);
        return  new QueryViewModel(uri, folder);
    }



}
