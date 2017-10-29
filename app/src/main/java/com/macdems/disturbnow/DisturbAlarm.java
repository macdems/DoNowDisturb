package com.macdems.disturbnow;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;


public class DisturbAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert nm != null;
        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
        //Toast.makeText(context, R.string.turned_off, Toast.LENGTH_LONG).show();
        Log.d("DoNowDisturb", "turned off DND mode");

        cancelNotification(context);
    }

    public static void setupAlarm(Calendar time, Context context) {
        Intent intent = new Intent(context, DisturbAlarm.class);
        PendingIntent alarm = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert manager != null;
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), alarm);

        int hour = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notification = pref.getBoolean("show_notification", false);
        String text = String.format(context.getString(R.string.do_not_disturb_until_message), hour, minute);
        if (notification) {
            addNotification(context, text, alarm);
        } else {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        }
    }

    private static void addNotification(Context context, String text, PendingIntent cancel) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_disturbnow)
                        .setContentTitle(text)
                        .setContentText(context.getString(R.string.click_to_turn_off))
                        .setOngoing(true)
                ;

        builder.setContentIntent(cancel);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.notify(R.id.notification, builder.build());
    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, DisturbAlarm.class);
        PendingIntent alarm = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert manager != null;
        manager.cancel(alarm);
    }

    public static void cancelNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.cancel(R.id.notification);
    }

}
