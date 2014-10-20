package com.test.slideshow.models;

import android.net.Uri;

/**
 * Created by Nikita on 18.10.2014.
 */
public class LoadBitmapViewModel {
    String mUriString;
    int mRequiredWidth;
    int mRequiredHeight;

    public LoadBitmapViewModel(String uri, int reqH, int reqW){
        if (uri == null ) throw new NullPointerException("provided args are not initialized");
        mUriString = uri;
        mRequiredHeight = reqH;
        mRequiredWidth = reqW;
    }

    public String getUriString() { return mUriString; }

    public int getHeight() {return mRequiredHeight;}
    public int getWidth() { return mRequiredWidth; }
}
