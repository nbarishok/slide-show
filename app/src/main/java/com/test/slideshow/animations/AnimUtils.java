package com.test.slideshow.animations;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by Nikita on 19.10.2014.
 */
public class AnimUtils {

    public static void hardwareTranslationY(View view, boolean isMovingBottom){
        int height = view.getMeasuredHeight();
        view.animate().translationY(isMovingBottom ? height : -height).setDuration(400).setListener(new HardwareAccelerationAnimationListener(view));
    }

    public static ViewPropertyAnimator hardwareAlpha(View view, float value){
        return view.animate().alpha(value).setDuration(400)
                .setInterpolator(new AccelerateInterpolator()).setListener(new HardwareAccelerationAnimationListener(view));
    }


    public static void backportPostAnimation(ViewPropertyAnimator animator, View view, Runnable postAction){
        if (Build.VERSION.SDK_INT  < Build.VERSION_CODES.JELLY_BEAN){
            animator.setListener(new CustomEndHardwareAccelerationAnimationListener(view, postAction));
        }
        else
            animator.withEndAction(postAction);


    }

    public static void hardwareTranslationYOrigin(ViewGroup viewGroup){
        viewGroup.animate().translationY(0).setDuration(400).setListener(new HardwareAccelerationAnimationListener(viewGroup));
    }
}
