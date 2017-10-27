package com.macdems.disturbnow;

import java.util.Calendar;
import java.util.HashMap;
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
import android.widget.TimePicker;
import android.widget.Toast;

public class DNDTileService extends TileService
        implements TimeDialog.OnTimeSetListener {

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.app.action.INTERRUPTION_FILTER_CHANGED")) {
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

    public void toggleTile() {
        //cancelAlarm();
        Tile tile = getQsTile();
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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

    public void setTileToMatchCurrentState() {
        //cancelAlarm();
        Tile tile = getQsTile();
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int currentInterruptionFilter = nm.getCurrentInterruptionFilter();
        if (currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
        } else {
            tile.setState(Tile.STATE_ACTIVE);
            tile.updateTile();
        }
    }

    protected void selectTime() {
        Calendar time = Calendar.getInstance();
        int hour = (time.get(Calendar.HOUR_OF_DAY) + 1) % 24;
        int minute = 5 * ((time.get(Calendar.MINUTE) + 4) / 5);
        if (minute >= 60) {
            hour += 1;
            minute -= 60;
        }
        TimeDialog timeDialog;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean theme = pref.getBoolean("dark_theme", false);
        timeDialog = new TimeDialog(getApplicationContext(),
                theme? android.R.style.Theme_Material_Dialog_NoActionBar :
                       android.R.style.Theme_Material_Light_Dialog_NoActionBar,
                this, hour, minute, true);
        //timeDialog.setTitle(R.string.select_end_time);
        showDialog(timeDialog);
    }

    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());

        int currentHour = time.get(Calendar.HOUR_OF_DAY);
        if (hour < currentHour || (hour == currentHour && minute <= time.get(Calendar.MINUTE))) {
            time.add(Calendar.DATE, 1);
        }
        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, minute);
        time.set(Calendar.SECOND, 0);

        Context context = getApplicationContext();
        DisturbAlarm.setupAlarm(time, context);

        Toast.makeText(context,
                String.format(getString(R.string.turn_off_time), hour, minute),
                Toast.LENGTH_LONG).show();
    }

    public void cancelAlarm() {
        DisturbAlarm.cancelAlarm(getApplicationContext());
    }
}
