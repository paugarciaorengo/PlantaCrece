package com.pim.planta.ui.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.pim.planta.R;
import com.pim.planta.preferences.TimePreferenceDialogFragmentCompat;
import com.pim.planta.ui.settings.AppSelectionActivity;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // Lógica para abrir AppSelectionActivity
        Preference appSelectionPref = findPreference("pref_app_selection");
        if (appSelectionPref != null) {
            appSelectionPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getContext(), AppSelectionActivity.class);
                startActivity(intent);
                return true;
            });
        }
    }

    @Override
    public void onDisplayPreferenceDialog(androidx.preference.Preference preference) {
        if (preference instanceof com.pim.planta.preferences.TimePreference) {
            TimePreferenceDialogFragmentCompat dialogFragment =
                    TimePreferenceDialogFragmentCompat.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getParentFragmentManager(), null);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

}
