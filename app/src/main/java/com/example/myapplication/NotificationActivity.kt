package com.example.myapplication

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import com.example.myapplication.databinding.ActivityNotificationBinding
import java.util.*



private var notificationId = 1

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var actionBar: ActionBar

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar=supportActionBar!!
        actionBar.title="Profile"

        binding.setBtn.setOnClickListener {
            set()
        }
        binding.cancelBtn.setOnClickListener {
            cancel()
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun set()
    {
        var notification = binding.editTask.text.toString()
        var time=binding.timePicker
        var intent=Intent(this,AlarmReceiver::class.java)
        intent.putExtra("notificationId", notificationId)
        intent.putExtra("message",notification)

        var pendingIntent = PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_CANCEL_CURRENT)

        val alarm: AlarmManager? = getSystemService(ALARM_SERVICE) as AlarmManager?

        var hour = time.hour
        var minute = time.minute

        //Create time
        var start = Calendar.getInstance()
        start.set(Calendar.HOUR_OF_DAY,hour)
        start.set(Calendar.MINUTE,minute)
        start.set(Calendar.SECOND,0)
        var alarmStart = start.timeInMillis

        //setAlarm
        if (alarm != null) {
            alarm.set(AlarmManager.RTC_WAKEUP,alarmStart,pendingIntent)
            Toast.makeText(this,"Done!",Toast.LENGTH_SHORT).show()
            finish()
        }
        else
        {
            Toast.makeText(this,"Alarm wasn't set!",Toast.LENGTH_SHORT).show()
            finish()
        }

    }
    private fun cancel()
    {
        Toast.makeText(this,"Canceled.",Toast.LENGTH_SHORT).show()
        finish()
    }
}