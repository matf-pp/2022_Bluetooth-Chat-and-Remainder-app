package com.example.myapplication

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityNotificationBinding
import java.util.*


private var notificationId = 1

class NotificationActivity : AppCompatActivity() {

    companion object{
        var minutes: List<Int> = ArrayList(25)
        var alarmManagers = arrayOfNulls<AlarmManager>(25)
        var intents = arrayOfNulls<PendingIntent>(25)
        var info = 0

    }


    private lateinit var binding: ActivityNotificationBinding
    private lateinit var actionBar: ActionBar


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar=supportActionBar!!
        actionBar.title="Reminders"

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
            if(NotificationActivity.info == 25)
                NotificationActivity.info = 0
            intent=Intent(this,AlarmReceiver::class.java)
            intent?.putExtra("notificationId", notificationId)
            intent?.putExtra("message",notification)


            var pendingIntent = PendingIntent.getBroadcast(this,info,intent,0)

            NotificationActivity.alarmManagers[info] = getSystemService(ALARM_SERVICE) as AlarmManager?

            var hour = time.hour
            var minute = time.minute

            //Create time
            var start = Calendar.getInstance()
            start.set(Calendar.HOUR_OF_DAY,hour)
            start.set(Calendar.MINUTE,minute)
            start.set(Calendar.SECOND,0)
            var alarmStart = start.timeInMillis

            //setAlarm
            if (alarmManagers[info] != null) {
                alarmManagers[info]?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,alarmStart,pendingIntent)
                Toast.makeText(this,"Done!",Toast.LENGTH_SHORT).show()
                finish()
                intents[info]=pendingIntent
                info = info + 1
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