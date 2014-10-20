package com.test.slideshow;

import android.app.Application;
import android.content.Context;

/**
 * Created by Nikita on 18.10.2014.
 */
public class MyApplication extends Application {
    @Override
    public final void onCreate(){
        super.onCreate();
    }
    private static MyApplication instance;
    public MyApplication() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }
}
