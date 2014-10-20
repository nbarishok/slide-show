package com.test.slideshow.models;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.test.slideshow.tasks.AsyncLoadBitmap;

import java.lang.ref.WeakReference;

/**
 * Created by Nikita on 18.10.2014.
 */
public class AsyncTag {
    private final WeakReference<AsyncLoadBitmap> bitmapWorkerTaskReference;
    private final String mUri;

    public AsyncTag(String res, AsyncLoadBitmap bitmapWorkerTask) {
        bitmapWorkerTaskReference =
                new WeakReference<AsyncLoadBitmap>(bitmapWorkerTask);
        mUri = res;
    }

    public AsyncLoadBitmap getBitmapWorkerTask() {
        return bitmapWorkerTaskReference.get();
    }

    public String getUri(){
        return mUri;
    }
}
