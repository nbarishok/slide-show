package com.test.slideshow.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.test.slideshow.SlideShowActivity;

/**
 * Created by Nikita on 19.10.2014.
 */
public class ChargingReceiver extends BroadcastReceiver {


    public static final String POWER_CONNECTED = "com.test.slideshow.receivers.ChargingReceiver.POWER_CONNECTED";
    public static final String IS_CONNECTED = "com.test.slideshow.receivers.ChargingReceiver.POWER_CONNECTED";


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i=new Intent(context, SlideShowActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setAction(POWER_CONNECTED);

        String action = intent.getAction();
        if (action.equals(Intent.ACTION_POWER_CONNECTED))
            i.putExtra(IS_CONNECTED, 1);
        else if (action.equals(Intent.ACTION_POWER_DISCONNECTED) && SlideShowActivity.IS_ACTIVE)
            i.putExtra(IS_CONNECTED, -1);

        if (i.hasExtra(IS_CONNECTED)) {
            i.putExtra(AlarmReceiver.TIMESTAMP_KEY, System.currentTimeMillis());
            context.startActivity(i);
        }
    }
}
