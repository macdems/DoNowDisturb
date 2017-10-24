package com.macdems.disturbnow;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;


public class DisturbAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
        Toast.makeText(context, R.string.turned_off, Toast.LENGTH_LONG).show();
        Log.d("DoNowDisturb", "turned off DND mode");
    }

    public static void setupAlarm(Calendar time, Context context) {
        Intent intent = new Intent(context, DisturbAlarm.class);
        PendingIntent alarm = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), alarm);
    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, DisturbAlarm.class);
        PendingIntent alarm = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(alarm);
    }

}
