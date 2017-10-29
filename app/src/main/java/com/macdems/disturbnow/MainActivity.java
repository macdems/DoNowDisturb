package com.macdems.disturbnow;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

public class MainActivity extends PreferenceActivity {

    public void openAccessControl(View v) {
        Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        startActivity(intent);
    }

    private void setPermissionTabView() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        CardView cardview = (CardView) findViewById(R.id.permission_card);
        cardview.setVisibility(manager.isNotificationPolicyAccessGranted()? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setContentView(R.layout.main);
        setPermissionTabView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setPermissionTabView();
    }
}
