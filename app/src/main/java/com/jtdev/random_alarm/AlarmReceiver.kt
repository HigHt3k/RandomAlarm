package com.jtdev.random_alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private var mediaPlayer: MediaPlayer? = null

        // Get the singleton instance of the media player
        fun getMediaPlayer(context: Context): MediaPlayer {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.alarm_sound)
                mediaPlayer?.isLooping = true
            }
            return mediaPlayer!!
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("AlarmReceiver", "Broadcast received: ${intent?.action}")

        if (intent?.action == "com.jtdev.random_alarm.START_ALARM") {
            // Start playing the alarm sound
            mediaPlayer = getMediaPlayer(context!!)
            mediaPlayer?.start()
            Log.d("AlarmReceiver", "Alarm started playing for mediaPlayer $mediaPlayer")
        } else if (intent?.action == "com.jtdev.random_alarm.STOP_ALARM") {
            // Stop the alarm sound
            mediaPlayer?.let {
                if (it.isPlaying) {
                    Log.d("MediaPlayer", "Was playing, stopping the player")
                    it.stop()
                }
                it.release()
            }
            Log.d("AlarmReceiver", "Alarm stop playing")
        }
    }
}
