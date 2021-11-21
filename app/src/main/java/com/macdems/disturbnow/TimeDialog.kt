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
package com.macdems.disturbnow

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.widget.TimePicker
import android.widget.TimePicker.OnTimeChangedListener
import com.macdems.disturbnow.DisturbAlarm.Companion.setupAlarm
import java.util.*

internal class TimeDialog(context: Context) : AlertDialog(context, getDialogTheme(context)), DialogInterface.OnClickListener, OnTimeChangedListener {

    private val mTimePicker: TimePicker

    override fun onTimeChanged(view: TimePicker, hourOfDay: Int, minute: Int) {
        /* do nothing */
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            BUTTON_POSITIVE -> setAlarm()
            BUTTON_NEGATIVE -> {
                val nm = (context
                        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                cancel()
            }
            BUTTON_NEUTRAL -> cancel()
        }
    }

    private fun setAlarm() {
        val time = Calendar.getInstance()
        time.timeInMillis = System.currentTimeMillis()
        val hour = mTimePicker.hour
        val minute = mTimePicker.minute
        val currentHour = time[Calendar.HOUR_OF_DAY]
        if (hour < currentHour || hour == currentHour && minute <= time[Calendar.MINUTE]) {
            time.add(Calendar.DATE, 1)
        }
        time[Calendar.HOUR_OF_DAY] = hour
        time[Calendar.MINUTE] = minute
        time[Calendar.SECOND] = 0
        setupAlarm(time, context)
    }

    override fun onSaveInstanceState(): Bundle {
        val state = super.onSaveInstanceState()
        state.putInt(HOUR, mTimePicker.hour)
        state.putInt(MINUTE, mTimePicker.minute)
        state.putBoolean(IS_24_HOUR, mTimePicker.is24HourView)
        return state
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val hour = savedInstanceState.getInt(HOUR)
        val minute = savedInstanceState.getInt(MINUTE)
        mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR))
        mTimePicker.hour = hour
        mTimePicker.minute = minute
    }

    companion object {
        private const val HOUR = "hour"
        private const val MINUTE = "minute"
        private const val IS_24_HOUR = "is24hour"
        private fun getDialogTheme(context: Context): Int {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            return if (pref.getBoolean("dark_theme", false)) R.style.AppTheme_Dialog_Dark else R.style.AppTheme_Dialog_Light
        }
    }

    init {
        val themeContext = getContext()
        val inflater = LayoutInflater.from(themeContext)
        val view = inflater.inflate(R.layout.time_dialog, null)
        setView(view)
        setButton(BUTTON_POSITIVE, themeContext.getString(android.R.string.ok), this)
        setButton(BUTTON_NEUTRAL, themeContext.getString(R.string.keep_silent), this)
        setButton(BUTTON_NEGATIVE, themeContext.getString(android.R.string.cancel), this)

        //setTitle(R.string.select_end_time);
        val time = Calendar.getInstance()
        var hour = (time[Calendar.HOUR_OF_DAY] + 1) % 24
        var minute = 5 * ((time[Calendar.MINUTE] + 4) / 5)
        if (minute >= 60) {
            hour += 1
            minute -= 60
        }
        mTimePicker = view.findViewById<View>(R.id.timePicker) as TimePicker
        mTimePicker.setIs24HourView(DateFormat.is24HourFormat(context))
        mTimePicker.hour = hour
        mTimePicker.minute = minute
        mTimePicker.setOnTimeChangedListener(this)
    }
}