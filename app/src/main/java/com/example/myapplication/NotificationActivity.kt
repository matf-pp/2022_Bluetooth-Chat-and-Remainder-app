package com.example.myapplication

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityNotificationBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                set()
            }
        }
        binding.cancelBtn.setOnClickListener {
            cancel()
        }
    }
   // @RequiresApi(Build.VERSION_CODES.M)
    @RequiresApi(Build.VERSION_CODES.O)

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
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,alarmStart,pendingIntent)
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
    var CHANNEL_ID = "CHANNEL_SAMPLE"

}