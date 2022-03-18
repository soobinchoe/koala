package com.traydcorp.koala.ui.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.traydcorp.koala.ui.home.HomeActivity.Companion.ACTION_DELETE
import com.traydcorp.koala.ui.home.HomeActivity.Companion.ACTION_NEXT
import com.traydcorp.koala.ui.home.HomeActivity.Companion.ACTION_PLAY

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val actionIntent = Intent(context, PlayerService::class.java)
        if (intent?.action != null){
            when (intent.action) {
                ACTION_PLAY -> {
                    actionIntent.putExtra("actionName", intent.action)
                    context?.startService(actionIntent)
                }

                ACTION_NEXT -> {
                    actionIntent.putExtra("actionName", intent.action)
                    context?.startService(actionIntent)
                }

                ACTION_DELETE -> {
                    actionIntent.putExtra("actionName", intent.action)
                    context?.startService(actionIntent)
                }
            }
        }
    }
}