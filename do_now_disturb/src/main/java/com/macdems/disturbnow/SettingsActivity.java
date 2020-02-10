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

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import android.view.View;

public class SettingsActivity extends PreferenceActivity {

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
