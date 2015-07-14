package de.reneruck.inear2.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import de.reneruck.inear2.R;

public class SettingsFragment extends PreferenceFragment {
   
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
