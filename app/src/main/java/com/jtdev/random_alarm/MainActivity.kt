package com.jtdev.random_alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jtdev.random_alarm.MyApplication.Companion.context
import com.jtdev.random_alarm.ui.theme.MyApplicationTheme
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val alarmReceiver = AlarmReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(alarmReceiver, IntentFilter().apply {
            addAction("com.jtdev.random_alarm.START_ALARM")
            addAction("com.jtdev.random_alarm.STOP_ALARM")
        })

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SetAlarmButton()
                }
            }
        }
        Log.d("MainActivity", "Activity created")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(alarmReceiver)
    }
}

@Composable
fun SetAlarmButton() {
    var selectedTime by remember { mutableStateOf(12) } // Default selected time is 12 hours

    var alarmTimeLeft by remember { mutableStateOf<Long?>(null) }
    var alarmActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Button(
                onClick = {
                    if (selectedTime > 1) {
                        selectedTime--
                    }
                }
            ) {
                Text("-")
            }

            Text(
                text = "Time until alarm: $selectedTime hours",
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    if (selectedTime < 23) {
                        selectedTime++
                    }
                }
            ) {
                Text("+")
            }
        }

        Button(
            onClick = {
                Log.d("MainActivity", "Button clicked")
                if (alarmActive) {
                    cancelAlarm(context = context, alarmId = 1)
                    alarmTimeLeft = null
                    alarmActive = false
                } else {
                    val maxAlarmTime = selectedTime * 60 * 60 * 1000L // Convert hours to milliseconds
                    //val maxAlarmTime = 5 * 1000L
                    val alarmTime =  (System.currentTimeMillis()..System.currentTimeMillis() + maxAlarmTime + 1).random()

                    alarmTimeLeft = alarmTime
                    setRandomAlarm(context = context, alarmTime = alarmTime, alarmId = 1)
                    Log.d("MainActivity", "Setting alarm for: ${convertUnixTimeToNormalTime(alarmTime)}")
                    alarmActive = true
                }
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            if (alarmActive) {
                Text("Cancel Alarm")
            } else {
                Text("Set Random Alarm")
            }
        }

        if (alarmTimeLeft != null) {
            CountdownTimer(alarmTimeLeft!!)
        }

        if(alarmActive) {
            Button(
                onClick = {
                    stopAlarm(context = MyApplication.context, alarmId = 1)
                    alarmTimeLeft = null
                    alarmActive = false
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Stop Alarm")
            }
        }
    }
}

@Composable
fun CountdownTimer(alarmTime: Long) {
    var timeLeft by remember { mutableStateOf(alarmTime - System.currentTimeMillis()) }

    LaunchedEffect(alarmTime) {
        val timer = object : CountDownTimer(timeLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
            }

            override fun onFinish() {
                timeLeft = 0
            }
        }
        timer.start()
    }

    val hours = TimeUnit.MILLISECONDS.toHours(timeLeft)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeft) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeft) % 60

    val formattedTime = String.format("%02dh %02dm %02ds", hours, minutes, seconds)

    Text("Time left until alarm: $formattedTime")
}

fun setRandomAlarm(context: Context, alarmTime: Long, alarmId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.action = "com.jtdev.random_alarm.START_ALARM"
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        alarmId, // Use a unique ID as the requestCode
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
    Log.d("MainActivity", "Alarm set for: ${convertUnixTimeToNormalTime(alarmTime)} for context $context")
}


fun convertUnixTimeToNormalTime(unixTime: Long): String {
    // Create a Date object using the Unix time
    val date = Date(unixTime)

    // Create a SimpleDateFormat object to format the date
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // Format the date and return the formatted string
    return sdf.format(date)
}

fun cancelAlarm(context: Context, alarmId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.action = "com.jtdev.random_alarm.START_ALARM" // Match the action used for setting alarm
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        alarmId, // Use the same unique ID as used for setting the alarm
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    Log.d("MainActivity", "Cancelling alarm for context $context")
    alarmManager.cancel(pendingIntent)
}

fun stopAlarm(context: Context, alarmId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intentStop = Intent(context, AlarmReceiver::class.java)
    intentStop.action = "com.jtdev.random_alarm.STOP_ALARM"
    val pendingIntentStop = PendingIntent.getBroadcast(
        context,
        alarmId,
        intentStop,
        PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntentStop)
    Log.d("MainActivity", "Stopping alarm for context $context with intent $pendingIntentStop")
}

@Preview(showBackground = true)
@Composable
fun SetAlarmButtonPreview() {
    MyApplicationTheme {
        SetAlarmButton()
    }
}
