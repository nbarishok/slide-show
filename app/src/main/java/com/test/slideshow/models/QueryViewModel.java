package com.test.slideshow.models;

import android.net.Uri;

/**
 * Created by Nikita on 18.10.2014.
 */
public class QueryViewModel {

    private Uri mQueryUri;
    private String mFolderName;

    public QueryViewModel(Uri uri, String folder){
        mQueryUri = uri;
        mFolderName = folder;
    }

    public Uri getUri() { return mQueryUri; }
    public String getFolder() { return mFolderName; }
}