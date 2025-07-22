package com.pim.planta.preferences;

import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import androidx.preference.PreferenceDialogFragmentCompat;

public class TimePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    private TimePicker timePicker;

    public static TimePreferenceDialogFragmentCompat newInstance(String key) {
        final TimePreferenceDialogFragmentCompat fragment = new TimePreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected View onCreateDialogView(android.content.Context context) {
        return View.inflate(context, com.pim.planta.R.layout.pref_time_picker, null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        timePicker = view.findViewById(com.pim.planta.R.id.time_picker);

        // Obtener el valor actual
        String value = getPreference().getSharedPreferences()
                .getString(getPreference().getKey(), "20:00");

        String[] parts = value.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        timePicker.setHour(hour);
        timePicker.setMinute(minute);
        timePicker.setIs24HourView(true);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String time = String.format("%02d:%02d", hour, minute);

            // Usamos setTime() del TimePreference
            TimePreference preference = (TimePreference) getPreference();
            if (preference.callChangeListener(time)) {
                preference.setTime(hour, minute);
            }
        }
    }
}
