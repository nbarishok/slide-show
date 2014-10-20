package com.test.slideshow.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Nikita on 18.10.2014.
 */
public class QueryViewModel implements Parcelable {

    private Uri mQueryUri;
    private String mFolderName;

    public QueryViewModel(Uri uri, String folder){
        mQueryUri = uri;
        mFolderName = folder;
    }

    public Uri getUri() { return mQueryUri; }
    public String getFolder() { return mFolderName; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mQueryUri.toString());
        parcel.writeString(mFolderName);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public QueryViewModel createFromParcel(Parcel in) {
            return new QueryViewModel(in);
        }

        public QueryViewModel[] newArray(int size) {
            return new QueryViewModel[size];
        }
    };

    public QueryViewModel(Parcel in){
        mQueryUri = Uri.parse(in.readString());
        mFolderName = in.readString();

    }
}