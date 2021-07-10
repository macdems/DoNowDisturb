/*
 * Do Now Disturb
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

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.preference.PreferenceManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.macdems.disturbnow.DisturbAlarm.Companion.cancelAlarm
import com.macdems.disturbnow.DisturbAlarm.Companion.cancelNotification

class DNDTileService : TileService() {

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == "android.app.action.INTERRUPTION_FILTER_CHANGED") {
                Log.d("DoNowDisturb", "detected interruption filter change")
                setTileToMatchCurrentState()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter()
        filter.addAction("android.app.action.INTERRUPTION_FILTER_CHANGED")
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onTileAdded() {
        super.onTileAdded()
        setTileToMatchCurrentState()
    }

    override fun onStartListening() {
        super.onStartListening()
        setTileToMatchCurrentState()
    }

    override fun onClick() {
        val nm = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        if (nm.isNotificationPolicyAccessGranted) {
            super.onClick()
            toggleTile()
        } else {
            val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            sendBroadcast(closeIntent)
            val intent = Intent(this, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            setTileToMatchCurrentState()
        }
    }

    private fun toggleTile() {
        cancelAlarm()
        val tile = qsTile
        val nm = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        val currentState = nm.currentInterruptionFilter
        if (currentState == NotificationManager.INTERRUPTION_FILTER_ALL) {
            val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val mode = pref.getString("silence_mode", "priority")
            val newState = if (mode == "none") NotificationManager.INTERRUPTION_FILTER_NONE else
                           if (mode == "alarms") NotificationManager.INTERRUPTION_FILTER_ALARMS else
                           NotificationManager.INTERRUPTION_FILTER_PRIORITY
            Log.d("DoNowDisturb", String.format("setting silence mode to '%s' (%d)", mode, newState))
            nm.setInterruptionFilter(newState)
            tile.state = Tile.STATE_ACTIVE
            tile.updateTile()
            selectTime()
        } else {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            tile.state = Tile.STATE_INACTIVE
            tile.updateTile()
        }
    }

    private fun setTileToMatchCurrentState() {
        val tile = qsTile
        val nm = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        val currentInterruptionFilter = nm.currentInterruptionFilter
        if (currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            tile.state = Tile.STATE_INACTIVE
            tile.updateTile()
        } else {
            tile.state = Tile.STATE_ACTIVE
            tile.updateTile()
        }
    }

    private fun selectTime() {
        val timeDialog = TimeDialog(applicationContext)
        showDialog(timeDialog)
    }

    private fun cancelAlarm() {
        val context = applicationContext
        cancelAlarm(context)
        cancelNotification(context)
    }
}