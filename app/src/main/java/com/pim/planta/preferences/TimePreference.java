package com.pim.planta.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import androidx.preference.DialogPreference;

import com.pim.planta.R;

import java.util.Calendar;

public class TimePreference extends DialogPreference {
    private int hour = 20;
    private int minute = 0;

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.pref_time_picker);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        String value = getPersistedString((String) defaultValue);
        if (value != null && value.contains(":")) {
            String[] parts = value.split(":");
            try {
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {}
        }
        updateSummary();
    }

    public void setTime(int h, int m) {
        hour = h;
        minute = m;
        persistString(String.format("%02d:%02d", hour, minute));
        updateSummary();
    }

    private void updateSummary() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        String formattedTime = DateFormat.getTimeFormat(getContext()).format(cal.getTime());
        setSummary(formattedTime);
    }

    public int getHour() { return hour; }
    public int getMinute() { return minute; }
}
