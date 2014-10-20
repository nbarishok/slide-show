package com.test.slideshow.tasks;

import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.test.slideshow.SlideShowActivity;
import com.test.slideshow.models.QueryViewModel;
import com.test.slideshow.utilities.Auxiliary;

import java.util.ArrayList;

/**
 * Created by Nikita on 19.10.2014.
 */
public class LoadImageUrisFromSDCard extends AsyncTask<QueryViewModel, Object, ArrayList<String>> {


    private SlideShowActivity mContext;
    private Exception mException;
    final Runnable mPostAction;

    public LoadImageUrisFromSDCard(SlideShowActivity context, Runnable postAction){
        super();
        this.mContext = context;
        mPostAction = postAction;
    }

    @Override
    protected ArrayList<String> doInBackground(QueryViewModel... params) {
        QueryViewModel vm = params[0];
        String formedArg = new StringBuilder().append("%").append(vm.getFolder()).append("%").toString();
        String[] projection = {MediaStore.Images.Media.DATA};
        String[] selection = new String[]{ formedArg };

        ArrayList<String> result = new ArrayList<String>();
        try {
            Cursor imageUriCursor = Auxiliary.getImageCursorForDir(mContext, vm.getUri(), projection, selection );

            int size = imageUriCursor.getCount();
            if (size == 0) {
                //SAY USER THERE IS NOTHING TO SHOW
            }
            int columnIndex = imageUriCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            while (imageUriCursor.moveToNext()) {
                String uriString = imageUriCursor.getString(columnIndex);
                result.add(uriString);
            }
        }catch (Exception ex){
            mException = ex;
            return null;
        }

        return result;
    }

    @Override
    protected void onPostExecute(ArrayList<String> result) {
        mContext.onLoadedUris(result, mException);
        if (mPostAction != null)
            mPostAction.run();
    }
}
