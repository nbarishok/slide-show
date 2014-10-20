package com.test.slideshow.utilities.preferences;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.Toast;

import com.test.slideshow.MyApplication;

/**
 * Created by Nikita on 17.10.2014.
 */
public class IntEditTextPreference extends EditTextPreference {
    public IntEditTextPreference(Context context) {
        super(context);
    }

    public IntEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {

        return String.valueOf(getPersistedInt(5));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            String tmp = getText();

            persistString(tmp);

        }
    }

    @Override
    protected boolean persistString(String value) {
        int val = Integer.valueOf(value);

        return persistInt(val);
    }

}
