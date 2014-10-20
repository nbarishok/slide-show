package com.test.slideshow.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.test.slideshow.SlideShowActivity;
import com.test.slideshow.animations.AnimUtils;
import com.test.slideshow.models.LoadBitmapViewModel;
import com.test.slideshow.models.AsyncTag;

import java.lang.ref.WeakReference;

/**
 * Created by Nikita on 18.10.2014.
 */
public class AsyncLoadBitmap extends AsyncTask<LoadBitmapViewModel, Object, Bitmap> {

    private final static String TAG = AsyncLoadBitmap.class.getSimpleName();
    private final SlideShowActivity mContext;
    private final WeakReference<ImageView> mImageViewReference;
    private Exception mException = null;
    private String mUri;

    public  AsyncLoadBitmap(SlideShowActivity context, ImageView iv){
        mContext = context;
        mImageViewReference = new WeakReference<ImageView>(iv);

    }

    /**
     * Uri -> Bitmap -> BitmapDrawable
     * @param uris
     * @return
     */
    @Override
    protected Bitmap doInBackground(LoadBitmapViewModel... uris) {
        if (uris == null || uris.length == 0) throw new NullPointerException("input is empty in " + TAG);
        LoadBitmapViewModel vm = uris[0];

        if (vm == null) throw new NullPointerException("viewmodel is empty in " + TAG);
        mUri = vm.getUriString();
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(vm.getUriString(), options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;

            ImageView iv = mImageViewReference.get();
            if (iv != null){
                int sampleSize = getSampleSize(iv.getHeight(), iv.getWidth(), imageHeight, imageWidth);
                options.inSampleSize = sampleSize;

                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(vm.getUriString(), options);
            }
            else
                bitmap = null;
        }
        catch (Exception e) {
            //Try to recover
            bitmap = null;
            mException = e;
        }
        finally {

        }
        return bitmap;
    }

    private int getSampleSize(int height, int width, int imageHeight, int imageWidth) {
        int result =1 ;
        if (imageHeight > height || imageWidth > width) {

            final int halfHeight = imageHeight / 2;
            final int halfWidth = imageWidth / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / result) > height
                    && (halfWidth / result) > width) {
                result *= 2;
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(Bitmap result) {

        if (isCancelled()){
            result = null;
        }

        if (mImageViewReference != null && result != null) {
            final ImageView imageView = mImageViewReference.get();
            final AsyncLoadBitmap task = getBitmapWorkerTask(imageView);
            if (this == task && imageView != null) {
                final Bitmap finalResult = result;

                AnimUtils.backportPostAnimation(AnimUtils.hardwareAlpha(imageView, 0), imageView, new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(finalResult);
                                AnimUtils.hardwareAlpha(imageView, 1);
                            }
                        });
                imageView.setTag(null);
            }
        }
    }

    public static boolean cancelPotentialWork(String data, ImageView imageView) {


        final AsyncLoadBitmap bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.mUri;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == null || bitmapData != data) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static AsyncLoadBitmap getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            Object tag = imageView.getTag();
            if (tag != null){
                if (tag instanceof AsyncTag){
                    final AsyncTag asyncTag = (AsyncTag) tag;
                    return asyncTag.getBitmapWorkerTask();
                }
            }
        }
        return null;
    }
}
