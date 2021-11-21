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

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class SettingsActivity : AppCompatActivity() {

    fun openAccessControl(@Suppress("UNUSED_PARAMETER") v: View?) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        startActivity(intent)
    }

    private fun setPermissionTabView() {
        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        val cardview: CardView = findViewById(R.id.permission_card)
        cardview.visibility = if (manager.isNotificationPolicyAccessGranted) View.GONE else View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content, SettingsFragment())
            .commit()
        setContentView(R.layout.main)
        setPermissionTabView()
    }

    override fun onResume() {
        super.onResume()
        setPermissionTabView()
    }
}