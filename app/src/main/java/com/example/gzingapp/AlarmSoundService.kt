package com.example.gzingapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.gzingapp.utils.AppSettings
import android.util.Log

class AlarmSoundService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val channelId = "gzing_alarm_channel"
    private val notificationId = 3001
    companion object { private const val TAG = "AlarmSoundService" }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Use SERVICE type mediaPlayback for Android 14+ compliance
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            startForeground(notificationId, buildNotification("Alarm active", "Tap stop in navigation to silence"), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(notificationId, buildNotification("Alarm active", "Tap stop in navigation to silence"))
        }
        startLoopingAlarm()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existing = nm.getNotificationChannel(channelId)
            if (existing == null) {
                val ch = NotificationChannel(channelId, "Alarm", NotificationManager.IMPORTANCE_HIGH)
                ch.description = "Geofence alarm"
                nm.createNotificationChannel(ch)
            }
        }
    }

    private fun buildNotification(title: String, text: String): Notification {
        val intent = Intent(this, MapActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("stop_navigation", false)
        }
        val pi = PendingIntent.getActivity(this, 3100, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_navigation)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun startLoopingAlarm() {
        stopAlarm()
        val settings = AppSettings(this)
        val uriString = settings.getAlarmSoundUri()
        val uri: Uri = if (uriString.isNullOrEmpty()) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        } else Uri.parse(uriString)
        try {
            val title = RingtoneManager.getRingtone(this, uri)?.getTitle(this) ?: "Unknown"
            Log.d(TAG, "Starting geofence alarm with sound: title='$title' uri='${uri}'")
        } catch (_: Exception) {
            Log.d(TAG, "Starting geofence alarm with uri='${uri}'")
        }
        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@AlarmSoundService, uri)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            isLooping = true
            setOnPreparedListener { start() }
            setOnErrorListener { _, _, _ -> stopAlarm(); true }
            prepareAsync()
        }
    }

    fun stopAlarm() {
        try { mediaPlayer?.stop() } catch (_: Exception) { }
        try { mediaPlayer?.release() } catch (_: Exception) { }
        mediaPlayer = null
    }
}
