package com.praveennaresh.fyp;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Praveen Naresh
 * Created on 16-Feb-16.
 * Settings Activity
 */
public class AppPreferences extends PreferenceActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
