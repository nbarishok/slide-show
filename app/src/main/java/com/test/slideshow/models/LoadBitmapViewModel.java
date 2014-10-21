package com.test.slideshow.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Nikita on 18.10.2014.
 */
public class LoadBitmapViewModel implements Parcelable {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mUriString);
        parcel.writeInt(mRequiredWidth);
        parcel.writeInt(mRequiredHeight);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public LoadBitmapViewModel createFromParcel(Parcel in) {
            return new LoadBitmapViewModel(in);
        }

        public LoadBitmapViewModel[] newArray(int size) {
            return new LoadBitmapViewModel[size];
        }
    };

    public LoadBitmapViewModel(Parcel in){
        mUriString = in.readString();
        mRequiredWidth = in.readInt();
        mRequiredHeight = in.readInt();

    }
}
