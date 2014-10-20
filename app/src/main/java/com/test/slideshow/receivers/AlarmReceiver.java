package com.test.slideshow.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.test.slideshow.SlideShowActivity;
import com.test.slideshow.MyApplication;
import com.test.slideshow.R;
import com.test.slideshow.utilities.preferences.TimePreference;

import java.util.Calendar;

/**
 * Created by Nikita on 18.10.2014.
 */
public class AlarmReceiver extends BroadcastReceiver {
        public static final String ALARM_CUSTOM_ACTION = "com.test.slideshow.receivers.OnAlarmReceiver.ALARM_CUSTOM_ACTION";
    public static final String ALARM_MANAGER_KEY = "com.test.slideshow.receivers.OnAlarmReceiver.alarm_manager_key"; //value of 1 means it's time to start slideshow, value of -1 -- stop
    public static final String BOOT_COMPETED_KEY = "com.test.slideshow.receivers.OnAlarmReceiver.boot_completed_key";


    public static final String TIMESTAMP_KEY = "com.test.slideshow.receivers.OnAlarmReceiver.TIMESTAMP_KEY";

        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO refactor ..separate it to different broadcasts (or factory pattern, to handle everything in single broadcast)
            if (intent != null){

                String action = intent.getAction();

                if (action == Intent.ACTION_BOOT_COMPLETED){
                    Intent i=new Intent(context, SlideShowActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setAction(ALARM_CUSTOM_ACTION);

                    i.putExtra(BOOT_COMPETED_KEY, 1); // in our case this is just to distinguish that it is BOOT_COMPLETED, not ALARM_MANAGER event
                    context.startActivity(i);
                }
                else{
                Bundle b = intent.getExtras();
                if (b != null){
                    if (b.keySet().contains(ALARM_MANAGER_KEY)){
                        int res = b.getInt(ALARM_MANAGER_KEY);

                        if (res == -1) {
                            //trying to stop
                            if (!SlideShowActivity.IS_ACTIVE) //no need to stop, activity is not in foreground
                                return;
                        }

                        Intent i=new Intent(context, SlideShowActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.setAction(ALARM_CUSTOM_ACTION);

                        i.putExtra(ALARM_MANAGER_KEY, res);
                        i.putExtra(TIMESTAMP_KEY, System.currentTimeMillis());
                        context.startActivity(i);
                    }
                }
            }
            }

        }


    public static void setAlarmBegin(Context context){
        AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        long startTimeMillis = getTimeInMillis(context, true);

        if (startTimeMillis<System.currentTimeMillis()) {
            Toast.makeText(MyApplication.getContext(), "Выбранное время уже прошло\nВыберите другое", Toast.LENGTH_SHORT).show();
            return;
        }

        PendingIntent pIntent = getPendingIntent(context, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            mgr.setExact(AlarmManager.RTC_WAKEUP, startTimeMillis, pIntent);
        else mgr.set(AlarmManager.RTC_WAKEUP, startTimeMillis, pIntent);

        CharSequence timeDif = DateUtils.getRelativeTimeSpanString(startTimeMillis);
        Toast.makeText(MyApplication.getContext(), "Запуск слайд-шоу через " + timeDif, Toast.LENGTH_SHORT).show();
    }

    public static void setAlarmEnd(Context context){
        AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        long endTimeInMillis = getTimeInMillis(context, false);
        long beginTimeInMillis = getTimeInMillis(context, true);


        if (endTimeInMillis<System.currentTimeMillis() || endTimeInMillis < beginTimeInMillis) {
            Toast.makeText(MyApplication.getContext(), "Выбранное время раньше, чем время старта слайд-шоу\nВыберите другое", Toast.LENGTH_SHORT).show();
            return;
        }

        PendingIntent pIntent = getPendingIntent(context, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            mgr.setExact(AlarmManager.RTC_WAKEUP, endTimeInMillis, pIntent); //use setExact for precise delivery (since API 19)..we dont care about battery life here
        else mgr.set(AlarmManager.RTC_WAKEUP, endTimeInMillis, pIntent);
        CharSequence timeDif = DateUtils.getRelativeTimeSpanString(endTimeInMillis);
        Toast.makeText(MyApplication.getContext(), "Остановка слайд-шоу через " + timeDif, Toast.LENGTH_SHORT).show();
    }

    public static long getTimeInMillis(Context context, boolean isBegin){
        Calendar cal=Calendar.getInstance();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(context);
        String time=prefs.getString(context.getString(isBegin ? R.string.start_time_key : R.string.end_time_key), isBegin ? "12:00" : "13:00");

        cal.set(Calendar.HOUR_OF_DAY, TimePreference.getHour(time));
        cal.set(Calendar.MINUTE, TimePreference.getMinute(time));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    public static void cancelAlarm(Context context, boolean isStart) {
        AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        mgr.cancel(getPendingIntent(context, isStart));
    }

    private static PendingIntent getPendingIntent(Context ctxt, boolean isStart) {
        Intent i=new Intent(ctxt, AlarmReceiver.class);
        i.putExtra(ALARM_MANAGER_KEY, isStart ? 1 : -1);
        return(PendingIntent.getBroadcast(ctxt, isStart ? 1 : -1, i, 0));
    }

}
