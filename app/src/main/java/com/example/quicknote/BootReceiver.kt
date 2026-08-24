package com.example.quicknote

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.quicknote.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val db = AppDatabase.getDatabase(context)
            val noteDao = db.noteDao()
            
            CoroutineScope(Dispatchers.IO).launch {
                val currentTime = System.currentTimeMillis()
                val futureNotes = noteDao.getFutureReminders(currentTime)
                
                futureNotes.forEach { note ->
                    AlarmHelper.scheduleAlarm(context, note)
                }
            }
        }
    }
}
