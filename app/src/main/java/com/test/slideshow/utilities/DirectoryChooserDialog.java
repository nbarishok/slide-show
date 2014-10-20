package com.test.slideshow.utilities;

/**
 * Created by Nikita on 17.10.2014.
 */
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.test.slideshow.R;
import com.test.slideshow.models.DirModel;
import com.test.slideshow.utilities.preferences.Prefs;

public class DirectoryChooserDialog
{
    private boolean mIsNewFolderEnabled = true;
    private String mSdcardDirectory = "";
    private Context mContext;
    private TextView mTitleView;

    private String mDir = "";
    private List<DirModel> mSubdirs = null;
    private ChosenDirectoryListener m_chosenDirectoryListener = null;
    private ArrayAdapter<DirModel> mListAdapter = null;

    //////////////////////////////////////////////////////
    // Callback interface for selected directory
    //////////////////////////////////////////////////////
    public interface ChosenDirectoryListener
    {
        public void onChosenDir(String chosenDir);
    }

    public DirectoryChooserDialog(Context context, ChosenDirectoryListener chosenDirectoryListener)
    {
        mContext = context;
        mSdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        m_chosenDirectoryListener = chosenDirectoryListener;

        try
        {
            mSdcardDirectory = new File(mSdcardDirectory).getCanonicalPath();
        }
        catch (IOException ioe)
        {
        }
    }

    public DirectoryChooserDialog(Context context, String rootDir,  ChosenDirectoryListener chosenDirectoryListener)
    {
        mContext = context;
        mSdcardDirectory = rootDir;//Environment.getExternalStorageDirectory().getAbsolutePath();
        m_chosenDirectoryListener = chosenDirectoryListener;

        try
        {
            mSdcardDirectory = new File(mSdcardDirectory).getCanonicalPath();
        }
        catch (IOException ioe)
        {
        }
    }

    public void setNewFolderEnabled(boolean isNewFolderEnabled)
    {
        mIsNewFolderEnabled = isNewFolderEnabled;
    }

    public boolean getNewFolderEnabled()
    {
        return mIsNewFolderEnabled;
    }

    public void chooseDirectory()
    {
        // Initial directory is sdcard directory
        chooseDirectory(mSdcardDirectory);
    }

    public void chooseDirectory(String dir)
    {
        File dirFile = new File(dir);
        if (! dirFile.exists() || ! dirFile.isDirectory())
        {
            dir = mSdcardDirectory;
        }

        try
        {
            dir = new File(dir).getCanonicalPath();
        }
        catch (IOException ioe)
        {
            return;
        }

        mDir = dir;
        mSubdirs = getDirectories(dir);

        class DirectoryOnClickListener implements DialogInterface.OnClickListener
        {
            public void onClick(DialogInterface dialog, int item)
            {
                // Navigate into the sub-directory
                DirModel currentDir = (DirModel)((AlertDialog) dialog).getListView().getAdapter().getItem(item);
                if (currentDir.isEmpty()) return;

                mDir += "/" + currentDir;
                updateDirectory();
            }
        }

        AlertDialog.Builder dialogBuilder =
                createDirectoryChooserDialog(dir, mSubdirs, new DirectoryOnClickListener());

        dialogBuilder.setPositiveButton("OK", new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // Current directory chosen
                if (m_chosenDirectoryListener != null)
                {
                    // Call registered listener supplied with the chosen directory
                    m_chosenDirectoryListener.onChosenDir(mDir);
                }
            }
        }).setNegativeButton("Cancel", null);

        final AlertDialog dirsDialog = dialogBuilder.create();

        dirsDialog.setOnKeyListener(new OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    // Back button pressed
                    if ( mDir.equals(mSdcardDirectory) )
                    {
                        // The very top level directory, do nothing
                        return false;
                    }
                    else
                    {
                        // Navigate back to an upper directory
                        mDir = new File(mDir).getParent();
                        updateDirectory();
                    }

                    return true;
                }
                else
                {
                    return false;
                }
            }
        });

        // Show directory chooser dialog
        dirsDialog.show();
        styleButton(dirsDialog.getButton(DialogInterface.BUTTON_NEGATIVE));
        styleButton(dirsDialog.getButton(DialogInterface.BUTTON_POSITIVE));
    }

    private void styleButton(Button button){
        if(button == null) return;

        button.setBackgroundColor(mContext.getResources().getColor(R.color.background_darker));
        button.setTextColor(mContext.getResources().getColor(R.color.white_overlay));

    }
    private boolean createSubDir(String newDir)
    {
        File newDirFile = new File(newDir);
        if (! newDirFile.exists() )
        {
            return newDirFile.mkdir();
        }

        return false;
    }

    private List<DirModel> getDirectories(String dir)
    {
        List<DirModel> dirs = new ArrayList<DirModel>();

        try
        {
            File dirFile = new File(dir);
            if (! dirFile.exists() || ! dirFile.isDirectory())
            {
                return dirs;
            }

            for (File file : dirFile.listFiles())
            {
                if ( file.isDirectory() )
                {
                    dirs.add( new DirModel(file.getName()) );
                }
            }
        }
        catch (Exception e)
        {
        }

        Collections.sort(dirs, new Comparator<DirModel>()
        {
            public int compare(DirModel o1, DirModel o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return dirs;
    }

    private AlertDialog.Builder createDirectoryChooserDialog(String title, List<DirModel> listItems,
                                                             DialogInterface.OnClickListener onClickListener)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);

        // Create custom view for AlertDialog title containing
        // current directory TextView and possible 'New folder' button.
        // Current directory TextView allows long directory path to be wrapped to multiple lines.
        LinearLayout titleLayout = new LinearLayout(mContext);
        titleLayout.setOrientation(LinearLayout.VERTICAL);

        mTitleView = new TextView(mContext);
        mTitleView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        mTitleView.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
        mTitleView.setTextColor(mContext.getResources().getColor(R.color.white_overlay));
        mTitleView.setBackgroundColor(mContext.getResources().getColor(R.color.black_overlay));
        mTitleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        updateTitleWithPicsNumber(title);

        Button newDirButton = new Button(mContext);
        newDirButton.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        newDirButton.setText("New folder");
        newDirButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final EditText input = new EditText(mContext);

                // Show new folder name input dialog
                new AlertDialog.Builder(mContext).
                        setTitle("New folder name").
                        setView(input).setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        Editable newDir = input.getText();
                        String newDirName = newDir.toString();
                        // Create new directory
                        if ( createSubDir(mDir + "/" + newDirName) )
                        {
                            // Navigate into the new directory
                            mDir += "/" + newDirName;
                            updateDirectory();
                        }
                        else
                        {
                            Toast.makeText(
                                    mContext, "Failed to create '" + newDirName +
                                            "' folder", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("Cancel", null).show();
            }
        });

        if (!mIsNewFolderEnabled)
        {
            newDirButton.setVisibility(View.GONE);
        }

        titleLayout.addView(mTitleView);
        titleLayout.addView(newDirButton);

        dialogBuilder.setCustomTitle(titleLayout);

        mListAdapter = createListAdapter(listItems);

        dialogBuilder.setSingleChoiceItems(mListAdapter, -1, onClickListener);
        dialogBuilder.setCancelable(false);

        return dialogBuilder;
    }

    private void updateDirectory()
    {
        mSubdirs.clear();

        List<DirModel> subDirs = getDirectories(mDir);

        updateTitleWithPicsNumber(mDir);

        if (subDirs.size() == 0)
        {
            subDirs.add(new DirModel(""));
        }

        mSubdirs.addAll( subDirs );
        mListAdapter.notifyDataSetChanged();
    }

    private void updateTitleWithPicsNumber(String title){

        Uri uri = Prefs.isExternalDirNow(mContext) ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI : MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        String formedArg = new StringBuilder().append("%").append(mDir).append("%").toString();
        String[] projection = {MediaStore.Images.Media.DATA};
        String[] selection = new String[]{formedArg};
        Cursor imageCursor = Auxiliary.getImageCursorForDir(mContext, uri, projection, selection);

        int size = imageCursor.getCount();

        mTitleView.setText(new StringBuilder(title).append(" (").append(size).append(")"));
    }


    private ArrayAdapter<DirModel> createListAdapter(List<DirModel> items)
    {
        return new ArrayAdapter<DirModel>(mContext,
                R.layout.select_dialog_item, android.R.id.text1, items)
        {
            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent)
            {
                View v = super.getView(position, convertView, parent);

                if (v instanceof TextView)
                {
                    // Enable list item (directory) text wrapping
                    TextView tv = (TextView) v;
                    tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                    if (tv.getText().equals("")){
                        tv.setText(mContext.getString(R.string.empty_dir));
                    }
                    else
                    {
                        tv.setEnabled(true);
                    }
                }
                return v;
            }
        };
    }
}
