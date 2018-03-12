package com.example.android.popularmovies;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;

/**
 * Created by Mohamed on 2/19/2018.
 *
 */

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener ,
        android.support.v7.preference.Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_main_activity_settings);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();

        for (int i = 0; i < count; i++) {
            android.support.v7.preference.Preference p = prefScreen.getPreference(i);

            String value = sharedPreferences.getString(p.getKey(), "");
            setPreferenceSummary(p, value);
        }

        android.support.v7.preference.Preference columnsInPortrait = findPreference(
                getString(R.string.columns_in_portrait_key));
        android.support.v7.preference.Preference columnsInLandscape = findPreference(
                getString(R.string.columns_in_landscape_key));

        columnsInPortrait.setOnPreferenceChangeListener(this);
        columnsInLandscape.setOnPreferenceChangeListener(this);

        // Whoa ... I searched below a lot finding nothing but i read article how to make complete
        // custom preference layout & I was gonna do that but for last hope and to save a lot of
        // code i wanted to take a look at all methods in EditTextPreference and below one saved me
        // in layout I had to make id @android:id/edit as if not an error crash the app saying
        // that layout must have that id
        ((EditTextPreference) columnsInPortrait).setDialogLayoutResource(
                R.layout.number_preference_edit_text_dialog);
        ((EditTextPreference) columnsInLandscape).setDialogLayoutResource(
                R.layout.number_preference_edit_text_dialog);
    }

    private void setPreferenceSummary(android.support.v7.preference.Preference preference, String value) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference instanceof EditTextPreference) {
            preference.setSummary(value);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        android.support.v7.preference.Preference preference = findPreference(key);
        if (preference != null) {
            String value = sharedPreferences.getString(preference.getKey(), "");
            setPreferenceSummary(preference, value);
        }
    }

    @Override
    public boolean onPreferenceChange(android.support.v7.preference.Preference preference, Object newValue) {
        Toast errorMsg = Toast.makeText(getContext(), "Range is from 1 to 9 only",
                Toast.LENGTH_SHORT);

        try {
            String columnsNumberString = (String) newValue;

            try {
                int columnsNumber = Integer.parseInt(columnsNumberString);

                if (columnsNumber > 9 || columnsNumber < 1){
                    errorMsg.show();
                    return false;
                }
            }catch (Exception e){
                errorMsg.show();
                e.printStackTrace();
                return false;
            }
        }catch (Exception e){
            errorMsg.show();
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // un- & registering onSharedPreferencesChangeListener

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
