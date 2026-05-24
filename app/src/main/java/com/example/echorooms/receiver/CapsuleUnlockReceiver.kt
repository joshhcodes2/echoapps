package com.example.echorooms.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.echorooms.MainActivity

class CapsuleUnlockReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val roomId = intent.getLongExtra(EXTRA_ROOM_ID, -1L)
        val roomTitle = intent.getStringExtra(EXTRA_ROOM_TITLE) ?: "Capsule"
        val roomEmoji = intent.getStringExtra(EXTRA_ROOM_EMOJI) ?: "🔒"

        if (roomId == -1L) return

        showNotification(context, roomId, roomTitle, roomEmoji)
    }

    private fun showNotification(context: Context, roomId: Long, roomTitle: String, roomEmoji: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Capsule Unlocks",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when locked memory capsules unlock"
                enableLights(true)
                lightColor = android.graphics.Color.CYAN
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("room_id", roomId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            roomId.toInt(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock) // System lock icon
            .setContentTitle("Capsule Decrypted")
            .setContentText("$roomEmoji $roomTitle has unlocked. Tap to enter the capsule.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(roomId.toInt(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "capsule_unlock_channel"
        private const val EXTRA_ROOM_ID = "extra_room_id"
        private const val EXTRA_ROOM_TITLE = "extra_room_title"
        private const val EXTRA_ROOM_EMOJI = "extra_room_emoji"

        fun scheduleUnlockAlarm(context: Context, roomId: Long, unlockTimeMs: Long, title: String, emoji: String) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            
            val intent = Intent(context, CapsuleUnlockReceiver::class.java).apply {
                putExtra(EXTRA_ROOM_ID, roomId)
                putExtra(EXTRA_ROOM_TITLE, title)
                putExtra(EXTRA_ROOM_EMOJI, emoji)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                roomId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        unlockTimeMs,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        unlockTimeMs,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                // Fallback to non-exact if permission is missing
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    unlockTimeMs,
                    pendingIntent
                )
            }
        }
    }
}
