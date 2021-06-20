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

import android.content.BroadcastReceiver
import android.content.Intent
import android.app.NotificationManager
import com.macdems.disturbnow.DisturbAlarm
import android.app.PendingIntent
import android.app.AlarmManager
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.macdems.disturbnow.R
import android.widget.Toast
import android.os.Build
import android.app.NotificationChannel
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*

class DisturbAlarm : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val nm = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        //Toast.makeText(context, R.string.turned_off, Toast.LENGTH_LONG).show();
        Log.d("DoNowDisturb", "turned off DND mode")
        cancelNotification(context)
    }

    companion object {

        private const val DO_NOW_DISTURB_CHANNEL = "DO_NOW_DISTURB_CHANNEL"

        fun setupAlarm(time: Calendar, context: Context) {
            val intent = Intent(context, DisturbAlarm::class.java)
            val alarm = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            val manager = (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.timeInMillis, alarm)
            val hour = time[Calendar.HOUR_OF_DAY]
            val minute = time[Calendar.MINUTE]
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val notification = pref.getBoolean("show_notification", false)
            val text = String.format(context.getString(R.string.do_not_disturb_until_message), hour, minute)
            if (notification) {
                addNotification(context, text, alarm)
            } else {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show()
            }
        }

        private fun addNotification(context: Context, text: String, cancel: PendingIntent) {
            val manager = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(DO_NOW_DISTURB_CHANNEL,
                        context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT)
                channel.description = context.getString(R.string.channel_description)
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                manager.createNotificationChannel(channel)
            }
            val builder = NotificationCompat.Builder(context, DO_NOW_DISTURB_CHANNEL)
                    .setSmallIcon(R.drawable.ic_disturbnow)
                    .setContentTitle(text)
                    .setContentText(context.getString(R.string.click_to_turn_off))
                    .setOngoing(true)
            builder.setContentIntent(cancel)
            manager.notify(R.id.notification, builder.build())
        }

        fun cancelAlarm(context: Context) {
            val intent = Intent(context, DisturbAlarm::class.java)
            val alarm = PendingIntent.getBroadcast(context, 0, intent, 0)
            val manager = (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
            manager.cancel(alarm)
        }

        fun cancelNotification(context: Context) {
            val manager = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            manager.cancel(R.id.notification)
        }
    }
}