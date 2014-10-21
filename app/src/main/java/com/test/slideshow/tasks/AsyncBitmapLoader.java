package com.test.slideshow.tasks;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.AsyncTaskLoader;

import com.test.slideshow.models.LoadBitmapViewModel;

/**
 * Created by Nikita on 20.10.2014.
 */
public class AsyncBitmapLoader extends AsyncTaskLoader<AsyncBitmapLoader.BitmapWrapper> {

    public static class BitmapWrapper{
        private Bitmap mBitmap;
        private Exception mException;

        public BitmapWrapper( Bitmap b, Exception e){
            mBitmap = b;
            mException = e;
        }

        public Bitmap getBitmap() { return  mBitmap;}
        public Exception getException() { return  mException; }
    }

    private LoadBitmapViewModel mViewModel;
    private BitmapWrapper mWrapper;

    public AsyncBitmapLoader(Context context, LoadBitmapViewModel viewModel) {
        super(context);
        mViewModel = viewModel;
    }

    @Override
    public BitmapWrapper loadInBackground() {
        String uri = mViewModel.getUriString();
        Bitmap bitmap = null;
        Exception ex = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(uri, options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;

            int sampleSize = AsyncLoadBitmap.getSampleSize(mViewModel.getHeight(), mViewModel.getWidth(), imageHeight, imageWidth);
            options.inSampleSize = sampleSize;

            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(uri, options);

        }
        catch (Exception e) {
            //Try to recover
            bitmap = null;
            ex = e; // Add wrapper that'll have bitmap + exception to return
        }
        finally {

        }
        return new BitmapWrapper(bitmap, ex);
    }

    @Override
    public void deliverResult(BitmapWrapper wrapper) {
        if (isReset()) {
            if (wrapper != null) {
                releaseResources(wrapper);
                return;
            }
        }

        BitmapWrapper oldWrapper = mWrapper;
        mWrapper = wrapper;

        if (isStarted()) {
            super.deliverResult(wrapper);
        }

        // Invalidate the old data as we don't need it any more.
        if (oldWrapper != null && oldWrapper.getBitmap() != wrapper.getBitmap()) {
            releaseResources(wrapper);
        }
    }

    @Override
    protected void onStartLoading() {

        if (mWrapper != null) {
            deliverResult(mWrapper);
        }
        if (mWrapper == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {

        onStopLoading();
        if (mWrapper != null) {
            releaseResources(mWrapper);
            mWrapper = null;
        }
    }

    @Override
    public void onCanceled(BitmapWrapper wrapper) {
        super.onCanceled(wrapper);
        releaseResources(wrapper);
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
    }

    private void releaseResources(BitmapWrapper wrapper) {
        wrapper = null;
    }
}
