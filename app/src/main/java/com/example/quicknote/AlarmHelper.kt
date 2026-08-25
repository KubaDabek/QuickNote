package com.example.quicknote

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.quicknote.data.Note

/**
 * Obiekt pomocniczy do zarządzania systemowymi alarmami (przypomnieniami).
 * Wykorzystuje AlarmManager do precyzyjnego planowania powiadomień.
 */
object AlarmHelper {
    /** Planuje alarm typu AlarmClock (budzik), który zadziała nawet w trybie uśpienia. */
    fun scheduleAlarm(context: Context, note: Note) {
        // Nie planuj alarmu, jeśli czas już minął
        if (note.reminderTime <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Sprawdzenie wymagane od Androida 12 (S) dla dokładnych alarmów
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return
            }
        }

        // Przygotowanie intencji, która zostanie odebrana przez ReminderReceiver
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("NOTE_ID", note.id)
            putExtra("NOTE_TITLE", note.title)
            putExtra("NOTE_OBJECT", note)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // setAlarmClock to najwyższy priorytet alarmu w systemie
            val alarmClockInfo = AlarmManager.AlarmClockInfo(note.reminderTime, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (e: SecurityException) {
            // Brak uprawnień do dokładnego alarmu
        }
    }

    /** Anuluje zaplanowany wcześniej alarm dla konkretnej notatki. */
    fun cancelAlarm(context: Context, noteId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
