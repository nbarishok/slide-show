package com.test.slideshow.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.test.slideshow.MyApplication;
import com.test.slideshow.R;
import com.test.slideshow.utilities.preferences.Prefs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Nikita on 18.10.2014.
 */
public class Auxiliary {

    public static String initInternalDir(){
        ContextWrapper cw = new ContextWrapper(MyApplication.getContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE); //create imageDir in internal storage for DirChooser to examine it
        return directory.getParent();
    }

    public static Cursor getImageCursorForDir(Context context, Uri uri, String[] projection, String[] selection ){
        return context.getContentResolver().query(uri,
                projection,
                MediaStore.Images.Media.DATA + " like ? ",
                selection,
                null);
    }

    private static boolean isExternalStorageAvailable(){
        boolean mExternalStorageAvailable;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable  = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = false;
        }

        return mExternalStorageAvailable;
    }

    public static boolean checkStorageAvailable(Context context){
        return !(Prefs.isExternalDirNow(context) && !Auxiliary.isExternalStorageAvailable());
    }
}
