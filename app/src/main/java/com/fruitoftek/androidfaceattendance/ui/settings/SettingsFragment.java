package com.fruitoftek.androidfaceattendance.ui.settings;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import com.fruitoftek.androidfaceattendance.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    // TODO: Add "Delay" as setting with a default of 120 secs
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}