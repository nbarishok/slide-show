package com.test.slideshow;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

import com.test.slideshow.models.QueryViewModel;
import com.test.slideshow.tasks.AsyncListener;
import com.test.slideshow.utilities.Auxiliary;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikita on 20.10.2014.
 */
public class TaskFragment extends Fragment {
    private AsyncListener<List<String>> listener;
    private LoadImageUrisFromSDCard task;
    public static final String TASK_INPUT_KEY = "com.test.slideshow.taskfragment.task_input_key";

    public void onCreate(Bundle savedInstanceState) {
        QueryViewModel vm = null;
        Bundle args = getArguments();
        if (args != null)
            vm = args.getParcelable(TASK_INPUT_KEY);
        if (vm == null) throw new NullPointerException("QueryViewModel is null");
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        task = new LoadImageUrisFromSDCard();
        task.execute(vm);
    }
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (AsyncListener<List<String>>)activity;
    }
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    class LoadImageUrisFromSDCard extends AsyncTask<QueryViewModel, Object, ArrayList<String>> {

        private Exception mException;

        public LoadImageUrisFromSDCard(){
            super();
        }

        @Override
        protected ArrayList<String> doInBackground(QueryViewModel... params) {
            QueryViewModel vm = params[0];
            String formedArg = "%" + vm.getFolder() + "%";
            String[] projection = {MediaStore.Images.Media.DATA};
            String[] selection = new String[]{ formedArg };

            ArrayList<String> result = new ArrayList<String>();
            try {
                Cursor imageUriCursor = Auxiliary.getImageCursorForDir(MyApplication.getContext(), vm.getUri(), projection, selection);

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
            if (listener != null){
                listener.onPostExecute(result, mException);
            }
        }
    }
}