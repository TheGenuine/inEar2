package de.reneruck.inear2.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import de.reneruck.inear2.AppContext;
import de.reneruck.inear2.R;

public class SettingsFragment extends PreferenceFragment {


    private static final int REQUEST_CODE_OPEN_DIRECTORY = 25;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        findPreference("pref_base_dir").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(preference.getIntent(), REQUEST_CODE_OPEN_DIRECTORY);
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplication());
            prefs.edit().putString("pref_base_dir", data.getData().toString()).apply();
            ((AppContext) getActivity().getApplication()).setAudiobookBaseDir(data.getData().toString());
        }
    }
}
