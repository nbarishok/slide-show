package com.test.slideshow;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.test.slideshow.animations.AnimUtils;
import com.test.slideshow.models.LoadBitmapViewModel;
import com.test.slideshow.receivers.AlarmReceiver;
import com.test.slideshow.receivers.ChargingReceiver;
import com.test.slideshow.tasks.AsyncLoadBitmap;
import com.test.slideshow.tasks.LoadImageUrisFromSDCard;
import com.test.slideshow.models.AsyncTag;
import com.test.slideshow.utilities.Auxiliary;
import com.test.slideshow.utilities.DirectoryChooserDialog;
import com.test.slideshow.utilities.preferences.Prefs;

import java.util.ArrayList;


/**

 */
public class SlideShowActivity extends Activity {

    private boolean mForceStartShow = false;
    private boolean mForceStopShow = false;

    private int mSlideShowState;

    public static Boolean IS_ACTIVE = false;

    public static final int SLIDE_SHOW_RUNNING = 1;
    public static final int SLIDE_SHOW_STOPPED = -1;
    public static final int SLIDE_SHOW_UNINITIALIZED = 0;

    private static final int HIDE_DELAY = 3000;
    private static final int REQUEST_CHOOSER = 1234;

    private static final String INTERVAL_KEY = "com.test.slideshow.FullscreenActivity.intervalkey";
    private static final String STATE_KEY = "com.test.slideshow.FullscreenActivity.statekey";
    private static final String URIARRAY_KEY = "com.test.slideshow.FullscreenActivity.uriarraykey";
    private static final String TIMESTAMP_KEY = "com.test.slideshow.FullscreenActivity.timestampkey";

    private ImageView mDirsChooser;
    private ImageView mSettingsLauncher;
    private ImageView mSlideShow;
    private long mTimestamp;

    private TextView mTvFolderName;

    private LinearLayout mLlTop;
    private LinearLayout mLlBottom;
    private ImageView mStartStopButton;


    private int mCurrentInterval; //persisted in sharedpreferences
    private int mCurrentIndex=0; // -> in bundle

    private ArrayList<String> mUris = null; // -> in bundle

    private int mIvWidth, mIvHeight;

    Handler timerHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Handling broadcastreceiver request
        if (getIntent() != null){
            handleIntent(getIntent());
        }
        if (!Prefs.getIsInited(this)){
            String resultDir = Auxiliary.initInternalDir();
            if (resultDir != null) {
                Prefs.setIsInited(this, true, resultDir);
            }
            else
                Prefs.setIsInited(this, false, null);
        }

        if (savedInstanceState != null){
            mCurrentInterval = savedInstanceState.getInt(INTERVAL_KEY);
            mSlideShowState = savedInstanceState.getInt(STATE_KEY);
            mUris = savedInstanceState.getStringArrayList(URIARRAY_KEY);
            mTimestamp = savedInstanceState.getLong(TIMESTAMP_KEY);
        }
        else {
            mSlideShowState = SLIDE_SHOW_UNINITIALIZED;
            mCurrentInterval = Prefs.getInterval(this);
            mTimestamp = -1;
        }

        setContentView(R.layout.activity_fullscreen);
        mDirsChooser = (ImageView) findViewById(R.id.dir_chooser);
        mSettingsLauncher = (ImageView) findViewById(R.id.settings);
        mSlideShow = (ImageView) findViewById(R.id.iv_slideshow);

        mLlBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        mLlTop = (LinearLayout) findViewById(R.id.ll_top);

        mTvFolderName = (TextView) findViewById(R.id.tv_folder_name);

        mStartStopButton = (ImageView) findViewById(R.id.btn);
        mStartStopButton.setOnClickListener(mSlideShowStartStopListener);
    }

    private View.OnClickListener mSlideShowStartStopListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ImageView b = (ImageView) view;
            if (b == null) return;

            if (b.getTag().equals(MyApplication.getContext().getString(R.string.do_slide))){
                startSlideShow();
            }
            else{
                stopSlideShow();
            }
        }
    };

    Runnable postAction = new Runnable() {
        @Override
        public void run() {
            resumeSlideShow();
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        mDirsChooser.setOnClickListener(dirsListener);
        mSettingsLauncher.setOnClickListener(settingsLauncher);

        if (mUris == null)
            loadImageUris(postAction);
        else
            resumeSlideShow();
        mIvHeight = mSlideShow.getMeasuredHeight();
        mIvWidth = mSlideShow.getMeasuredWidth();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mDirsChooser.setOnClickListener(null);
        mSettingsLauncher.setOnClickListener(null);
        pauseSlideShow();
    }

    @Override
    public void onStop()
    {
        IS_ACTIVE = false;
        super.onStop();
    }

    @Override
    public void onStart()
    {
        IS_ACTIVE = true;
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle out){
        out.putInt(INTERVAL_KEY, mCurrentInterval);
        out.putInt(STATE_KEY, mSlideShowState);
        out.putLong(TIMESTAMP_KEY, mTimestamp);
        super.onSaveInstanceState(out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CHOOSER) return;
        if (resultCode != RESULT_OK) return;
        Bundle extras = data.getExtras();
        if (extras != null){
            if (extras.containsKey(SettingsActivity.INTERVAL_CHANGE_KEY))
                mCurrentInterval = data.getIntExtra(SettingsActivity.INTERVAL_CHANGE_KEY, 5);
            if (extras.containsKey(SettingsActivity.STORAGE_CHANGE_KEY)){
                //have to load new uris in onResume
                mUris = null;
            }
        }

        updateDirDescription();
    }

    private Handler mHandler = new Handler();
    Runnable hideBars = new Runnable() {
        @Override
        public void run() {
            hideIfVisible(mLlTop,false);
            hideIfVisible(mLlBottom, true);
        }
    };

    Runnable showBars = new Runnable() {
        @Override
        public void run() {
            show(mLlBottom);
            show(mLlTop);
        }
    };

    private View.OnClickListener mTopWidgetHideShowListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mHandler.post(showBars);
            mHandler.postDelayed( hideBars, HIDE_DELAY);
        }
    };

    private View.OnClickListener settingsLauncher = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SlideShowActivity.this,SettingsActivity.class);
            startActivityForResult(intent, REQUEST_CHOOSER);
        }
    };

    private View.OnClickListener dirsListener = new View.OnClickListener() {
        private String mChosenDir = "";

        @Override
        public void onClick(View v) {
            // Create DirectoryChooserDialog and register a callback

            if (!Auxiliary.checkStorageAvailable(SlideShowActivity.this)){
                Toast.makeText(SlideShowActivity.this, "Внешняя память недоступна, в настройках выберите внутреннюю", Toast.LENGTH_LONG).show();
                return;
            }

            final String rootDir = Prefs.getRootDir(SlideShowActivity.this);

            DirectoryChooserDialog directoryChooserDialog =
                    new DirectoryChooserDialog(SlideShowActivity.this, rootDir,
                            new DirectoryChooserDialog.ChosenDirectoryListener() {
                                @Override
                                public void onChosenDir(String chosenDir) {
                                    Prefs.setDir(SlideShowActivity.this, chosenDir);
                                    loadImageUris(null);
                                    Toast.makeText(
                                            SlideShowActivity.this, "Текущая директория: " +
                                                    chosenDir, Toast.LENGTH_SHORT).show();
                                }
                            });
            // Toggle new folder button enabling
            directoryChooserDialog.setNewFolderEnabled(false);
            // Load directory chooser dialog for initial 'mChosenDir' directory.
            // The registered callback will be called upon final directory selection.
            directoryChooserDialog.chooseDirectory(mChosenDir);
        }
    };

    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            //update image view with new image from File System
            //wait for specified time to run a new cycle
            String uri = mUris.get(mCurrentIndex);
            if (AsyncLoadBitmap.cancelPotentialWork(uri, mSlideShow)) {
                final AsyncLoadBitmap task = new AsyncLoadBitmap(SlideShowActivity.this, mSlideShow);
                final AsyncTag aTag =
                        new AsyncTag(uri, task);
                mSlideShow.setTag(aTag);
                task.execute(new LoadBitmapViewModel( uri, mIvHeight, mIvWidth));

                if (mCurrentIndex == mUris.size()-1)
                    mCurrentIndex = 0;
                else mCurrentIndex++;

                timerHandler.postDelayed(timerRunnable, mCurrentInterval * 1000);
            }
        }
    };

    //TODO refactor.. slide show actions can be encapsulated with State pattern (Uninitialized, Running, Stopped, Paused)
    private void startSlideShow(){
        if (mSlideShowState == SLIDE_SHOW_RUNNING){
            Toast.makeText(this, "Слайд-шоу уже запущено", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mUris.size() < 1)
            Toast.makeText(this, "В выбранной папке нет картинок", Toast.LENGTH_LONG).show();
        else if (!Auxiliary.checkStorageAvailable(SlideShowActivity.this)){
            Toast.makeText(SlideShowActivity.this, "Внешняя память недоступна, в настройках выберите внутреннюю", Toast.LENGTH_LONG).show();
        }
        else {
            doStartTransitions();
            timerRunnable.run();
        }
    }

    private void stopSlideShow(){
        if (mSlideShowState != SLIDE_SHOW_RUNNING) return;
        pauseSlideShow();
        doStopTransitions();
        mCurrentIndex = 0;
    }

    private void resumeSlideShow() {
        if (mSlideShowState != SLIDE_SHOW_RUNNING) {
            if (mForceStartShow){
                mForceStartShow = false; //resuming our slide show cause we were forced to
            }
            else{
                mForceStopShow = false;
                return;
            } //we are in uninitialized or stopped state and not forced to run, so just return
        }
        if (mForceStopShow){
            mForceStopShow = false; // we are supposed to run but were forced to stop -> so stop
            stopSlideShow();
            return;
        }
        if (mUris.size() < 1)
            Toast.makeText(this, "В выбранной папке нет картинок", Toast.LENGTH_LONG).show();
        else if (!Auxiliary.checkStorageAvailable(SlideShowActivity.this)){
            Toast.makeText(SlideShowActivity.this, "Внешняя память недоступна, в настройках выберите внутреннюю", Toast.LENGTH_LONG).show();
        }else{
        //run! automatically using current position which is maintained across activity lifecycle
            doStartTransitions();
            timerRunnable.run();
        }
    }

    private void pauseSlideShow(){
        timerHandler.removeCallbacks(timerRunnable);

        mSlideShow.setOnClickListener(null);
        mHandler.removeCallbacks(hideBars);
    }

    private void doStartTransitions(){
        mSlideShowState = SLIDE_SHOW_RUNNING;

        hideIfVisible(mLlTop, false);
        hideIfVisible(mLlBottom, true);
        mStartStopButton.setTag(getString(R.string.stop_slide));
        mStartStopButton.setImageDrawable(MyApplication.getContext().getResources().getDrawable(R.drawable.ic_action_stop));
        mSlideShow.setOnClickListener(mTopWidgetHideShowListener);
    }

    private void doStopTransitions(){
        mSlideShowState = SLIDE_SHOW_STOPPED;

        mHandler.post(showBars);
        mStartStopButton.setTag(getString(R.string.do_slide));
        mStartStopButton.setImageDrawable(MyApplication.getContext().getResources().getDrawable(R.drawable.ic_action_slideshow));
    }


    private void loadImageUris(Runnable postAction) {

        if (!Auxiliary.checkStorageAvailable(SlideShowActivity.this)){
            Toast.makeText(SlideShowActivity.this, "Внешняя память недоступна, в настройках выберите внутреннюю", Toast.LENGTH_LONG).show();
            return;
        }

        hideIfVisible(mLlBottom, true);
        stopSlideShow();
        mLoadUrisTask = new LoadImageUrisFromSDCard(this, postAction);
        mLoadUrisTask.execute(Prefs.getQueryViewModel(this));
    }

    private void hideIfVisible(ViewGroup viewGroup, boolean isMovingBottom) {
        if (viewGroup.getVisibility() == View.GONE) return;
        AnimUtils.hardwareTranslationY(viewGroup, isMovingBottom);

    }

    private void show(ViewGroup viewGroup){
        if (viewGroup.getVisibility() == View.GONE) return;
        AnimUtils.hardwareTranslationYOrigin(viewGroup);
    }

    private LoadImageUrisFromSDCard mLoadUrisTask;

    public void onLoadedUris(ArrayList<String> res, Exception ex){
        if (ex != null)
        {
            //TODO add adequate logic
            Toast.makeText(this, "Во время загрузки данных произошла ошибка", Toast.LENGTH_SHORT).show();
            return;
        }
        mUris = res;
        show(mLlBottom);
        updateDirDescription();
        mCurrentIndex = 0;
    }

    private void updateDirDescription(){
        String updateText = new StringBuilder().append(getString(R.string.current_dir_title)).append(Prefs.getDir(this)).toString();
        mTvFolderName.setText(updateText);
    }


    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent){

        //handling case when app is being launched from history
        boolean launchedFromHistory = intent != null ? (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0 : false;
        if (launchedFromHistory) return;

        long tempStamp = -1;
        if (intent.hasExtra(AlarmReceiver.TIMESTAMP_KEY))
        {
            tempStamp = intent.getExtras().getLong(AlarmReceiver.TIMESTAMP_KEY);
            if (mTimestamp == tempStamp) return;
            mTimestamp = tempStamp;
        }

        String action = intent.getAction();

        if (action == AlarmReceiver.ALARM_CUSTOM_ACTION) //we are interested only in AlarmReceivers' actions
        {

            Bundle bundle = intent.getExtras();
            if (bundle.keySet().contains(AlarmReceiver.ALARM_MANAGER_KEY))
            {
                int actionInt = bundle.getInt(AlarmReceiver.ALARM_MANAGER_KEY);
                if (actionInt == 1)
                {
                    mForceStartShow = true;
                    Toast.makeText(this, "Запуск слайд-шоу по таймеру", Toast.LENGTH_LONG).show();
                }
                else if (actionInt == -1){
                    {
                        mForceStopShow = true;
                        Toast.makeText(this, "Остановка слайд-шоу по таймеру", Toast.LENGTH_LONG).show();
                    }
                }
            }
            else if (bundle.keySet().contains(AlarmReceiver.BOOT_COMPETED_KEY)){
                mForceStartShow = true;
                Toast.makeText(this, "Запуск слайд-шоу по окончанию загрузки", Toast.LENGTH_LONG).show();
            }
        }
        else if (action == ChargingReceiver.POWER_CONNECTED){
            Bundle bundle = intent.getExtras();
            if (bundle.keySet().contains(ChargingReceiver.IS_CONNECTED)){
                int actionInt = bundle.getInt(ChargingReceiver.IS_CONNECTED);
                if (actionInt == 1) {
                    mForceStartShow = true;
                    Toast.makeText(this, "Запуск слайд-шоу по началу зарядки", Toast.LENGTH_LONG).show();
                }
                else if (actionInt == -1){
                    mForceStopShow = true;
                    Toast.makeText(this, "Остановка слайд-шоу по окончанию зарядки", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}