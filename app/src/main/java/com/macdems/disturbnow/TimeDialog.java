/*
 * DoNowDisturb
 * Copyright (C) 2017 Maciej Dems <macdems@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package com.macdems.disturbnow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;

class TimeDialog extends AlertDialog implements DialogInterface.OnClickListener,
        TimePicker.OnTimeChangedListener {
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String IS_24_HOUR = "is24hour";

    private final TimePicker mTimePicker;

    private static int getDialogTheme(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref.getBoolean("dark_theme", false))
            return R.style.AppTheme_Dialog_Dark;
        else
            return R.style.AppTheme_Dialog_Light;
    }

    TimeDialog(Context context) {
        super(context, getDialogTheme(context));

        final Context themeContext = getContext();
        final LayoutInflater inflater = LayoutInflater.from(themeContext);
        final View view = inflater.inflate(R.layout.time_dialog, null);
        setView(view);
        setButton(BUTTON_POSITIVE, themeContext.getString(android.R.string.ok), this);
        setButton(BUTTON_NEGATIVE, themeContext.getString(R.string.keep_silent), this);

        //setTitle(R.string.select_end_time);

        Calendar time = Calendar.getInstance();
        int hour = (time.get(Calendar.HOUR_OF_DAY) + 1) % 24;
        int minute = 5 * ((time.get(Calendar.MINUTE) + 4) / 5);
        if (minute >= 60) {
            hour += 1;
            minute -= 60;
        }

        mTimePicker = (TimePicker) view.findViewById(R.id.timePicker);
        mTimePicker.setIs24HourView(DateFormat.is24HourFormat(context));
        mTimePicker.setHour(hour);
        mTimePicker.setMinute(minute);
        mTimePicker.setOnTimeChangedListener(this);
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        /* do nothing */
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                setAlarm();
                break;
            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }

    private void setAlarm() {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());

        int hour = mTimePicker.getHour();
        int minute = mTimePicker.getMinute();
        int currentHour = time.get(Calendar.HOUR_OF_DAY);
        if (hour < currentHour || (hour == currentHour && minute <= time.get(Calendar.MINUTE))) {
            time.add(Calendar.DATE, 1);
        }
        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, minute);
        time.set(Calendar.SECOND, 0);

        DisturbAlarm.setupAlarm(time, getContext());
    }

    @NonNull
    @Override
    public Bundle onSaveInstanceState() {
        final Bundle state = super.onSaveInstanceState();
        state.putInt(HOUR, mTimePicker.getHour());
        state.putInt(MINUTE, mTimePicker.getMinute());
        state.putBoolean(IS_24_HOUR, mTimePicker.is24HourView());
        return state;
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final int hour = savedInstanceState.getInt(HOUR);
        final int minute = savedInstanceState.getInt(MINUTE);
        mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR));
        mTimePicker.setHour(hour);
        mTimePicker.setMinute(minute);
    }
}
