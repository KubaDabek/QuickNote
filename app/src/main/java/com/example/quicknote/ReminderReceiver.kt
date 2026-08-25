package com.example.quicknote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.quicknote.data.Note

/**
 * Odbiorca sygnału z AlarmManager. Odpowiada za wyświetlenie powiadomienia
 * użytkownikowi w zaplanowanym czasie.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val noteTitle = intent.getStringExtra("NOTE_TITLE") ?: "Przypomnienie o notatce"
        val noteId = intent.getLongExtra("NOTE_ID", 0L)
        val noteObject = intent.getSerializableExtra("NOTE_OBJECT", Note::class.java)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "reminders"

        // Konfiguracja kanału powiadomień (wymagane od Androida 8.0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Przypomnienia",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kanał dla powiadomień o notatkach"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Akcja po kliknięciu w powiadomienie - otwarcie notatki do edycji
        val activityIntent = Intent(context, AddEditNoteActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AddEditNoteActivity.EXTRA_NOTE, noteObject)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId.toInt(),
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Budowanie powiadomienia z wysokim priorytetem i dźwiękiem
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("QuickNote")
            .setContentText(noteTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Wyświetlenie powiadomienia (używamy noteId, aby każde było unikalne)
        notificationManager.notify(noteId.toInt(), notification)
    }
}
