package com.example.myapplication

import android.R
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.legacy.content.WakefulBroadcastReceiver
import java.util.*


public class AlarmReceiver : WakefulBroadcastReceiver() {
    var CHANNEL_ID = "CHANNEL_SAMPLE"
    @SuppressLint("ServiceCast")
    override fun onReceive(context: Context?, intent: Intent?) {
        var notificationId = intent?.getIntExtra("notificationId",0)
        var message = intent?.getStringExtra("message")
        var x = generateRandom()
        var mainIntent = Intent(context,ProfileActivity::class.java)
        var contentIntent = PendingIntent.getActivity(context,x,mainIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val notman = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            var channel_name = "My notification"
            var importance = NotificationManager.IMPORTANCE_HIGH

            val channel: NotificationChannel = NotificationChannel(CHANNEL_ID,channel_name,importance) as NotificationChannel
            notman?.createNotificationChannel(channel)
        }
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context,CHANNEL_ID) as NotificationCompat.Builder
        builder?.setSmallIcon(R.drawable.ic_dialog_info)
            ?.setContentTitle("It's time!")
            ?.setContentText(message)
            ?.setWhen(System.currentTimeMillis())
            ?.setAutoCancel(true)
            ?.setContentIntent(contentIntent)
            ?.setPriority(Notification.PRIORITY_HIGH)
            ?.setCategory(NotificationCompat.CATEGORY_REMINDER)
            ?.setDefaults(Notification.DEFAULT_ALL)
            ?.setFullScreenIntent(contentIntent,true)


        if (notman != null) {
            if (notificationId != null) {
                if (builder != null) {
                    notman.notify(x,builder.build())
                }
            }
        }


    }
    fun generateRandom(): Int {
        val random = Random()
        return random.nextInt(9999 - 1000) + 1000
    }

}