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

package com.macdems.disturbnow;

import java.util.Objects;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import androidx.annotation.NonNull;
import android.widget.TimePicker;

public class DNDTileService extends TileService {

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            String action = intent.getAction();
            if (Objects.equals(action, "android.app.action.INTERRUPTION_FILTER_CHANGED")) {
                Log.d("DoNowDisturb", "detected interruption filter change");
                setTileToMatchCurrentState();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.app.action.INTERRUPTION_FILTER_CHANGED");
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        setTileToMatchCurrentState();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        setTileToMatchCurrentState();
    }

    @Override
    public void onClick() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert nm != null;
        if (nm.isNotificationPolicyAccessGranted()) {
            super.onClick();
            toggleTile();
        } else {
            Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeIntent);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            setTileToMatchCurrentState();
        }
    }

    private void toggleTile() {
        cancelAlarm();
        Tile tile = getQsTile();
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert nm != null;
        int currentState = nm.getCurrentInterruptionFilter();
        if (currentState == NotificationManager.INTERRUPTION_FILTER_ALL) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String mode = pref.getString("silence_mode", "priority");
            int newState =
                    (Objects.equals(mode, "none"))?   NotificationManager.INTERRUPTION_FILTER_NONE :
                    (Objects.equals(mode, "alarms"))? NotificationManager.INTERRUPTION_FILTER_ALARMS :
                                                      NotificationManager.INTERRUPTION_FILTER_PRIORITY;
            Log.d("DoNowDisturb", String.format("setting silence mode to '%s' (%d)", mode, newState));
            nm.setInterruptionFilter(newState);
            tile.setState(Tile.STATE_ACTIVE);
            tile.updateTile();
            selectTime();
        } else {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
        }
    }

    private void setTileToMatchCurrentState() {
        Tile tile = getQsTile();
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert nm != null;
        int currentInterruptionFilter = nm.getCurrentInterruptionFilter();
        if (currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
        } else {
            tile.setState(Tile.STATE_ACTIVE);
            tile.updateTile();
        }
    }

    private void selectTime() {
        TimeDialog timeDialog = new TimeDialog(getApplicationContext());
        showDialog(timeDialog);
    }

    private void cancelAlarm() {
        Context context = getApplicationContext();
        DisturbAlarm.cancelAlarm(context);
        DisturbAlarm.cancelNotification(context);
    }
}
