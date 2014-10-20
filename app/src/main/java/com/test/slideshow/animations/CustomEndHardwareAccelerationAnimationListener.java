package com.test.slideshow.animations;

import android.animation.Animator;
import android.view.View;

/**
 * Created by Nikita on 19.10.2014.
 */
public class CustomEndHardwareAccelerationAnimationListener extends HardwareAccelerationAnimationListener {

    private Runnable mRunnable;

    public CustomEndHardwareAccelerationAnimationListener(View targetView, Runnable postAction) {
        super(targetView);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        if (mRunnable == null) return;
        mRunnable.run();
    }

}
