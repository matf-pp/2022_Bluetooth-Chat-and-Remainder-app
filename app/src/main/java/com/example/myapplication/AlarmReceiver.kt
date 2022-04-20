package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings.Global.getString
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService


public class AlarmReceiver : BroadcastReceiver() {
    var CHANNEL_ID = "CHANNEL_SAMPLE"
    @SuppressLint("ServiceCast")
    override fun onReceive(context: Context?, intent: Intent?) {
        var notificationId = intent?.getIntExtra("notificationId",0)
        var message = intent?.getStringExtra("message")

        var mainIntent = Intent(context,ProfileActivity::class.java)
        var contentIntent = PendingIntent.getActivity(context,0,mainIntent,0)

        val notman = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            var channel_name = "My notification"
            var importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel: NotificationChannel = NotificationChannel(CHANNEL_ID,channel_name,importance) as NotificationChannel
            notman?.createNotificationChannel(channel)
        }
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context,CHANNEL_ID) as NotificationCompat.Builder
        builder?.setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            ?.setContentTitle("It's time!")
            ?.setContentText(message)
            ?.setWhen(System.currentTimeMillis())
            ?.setAutoCancel(true)
            ?.setContentIntent(contentIntent)
            ?.setPriority(Notification.PRIORITY_DEFAULT)
            ?.setDefaults(Notification.DEFAULT_ALL)


        if (notman != null) {
            if (notificationId != null) {
                if (builder != null) {
                    notman.notify(notificationId,builder.build())
                }
            }
        }

    }
}