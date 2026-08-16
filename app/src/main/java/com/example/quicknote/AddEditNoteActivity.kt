package com.example.quicknote

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.example.quicknote.data.Note
import com.example.quicknote.ui.NoteViewModel
import com.example.quicknote.ui.NoteViewModelFactory
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var editTextTitle: TextInputEditText
    private lateinit var editTextContent: TextInputEditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var switchPriority: SwitchMaterial
    private lateinit var btnSetReminder: Button
    private lateinit var textViewReminder: TextView
    private lateinit var noteViewModel: NoteViewModel
    private var currentNote: Note? = null
    private var reminderTime: Long = 0
    private val categories = arrayOf("Brak", "Praca", "Szkoła", "Dom", "Inne")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_add_edit_note)

            val toolbar: Toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            editTextTitle = findViewById(R.id.editTextTitle)
            editTextContent = findViewById(R.id.editTextContent)
            spinnerCategory = findViewById(R.id.spinnerCategory)
            switchPriority = findViewById(R.id.switchPriority)
            btnSetReminder = findViewById(R.id.btnSetReminder)
            textViewReminder = findViewById(R.id.textViewReminder)

            btnSetReminder.setOnClickListener {
                showDateTimePicker()
            }

            val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = categoryAdapter

            val factory = NoteViewModelFactory(application)
            noteViewModel = ViewModelProvider(this, factory)[NoteViewModel::class.java]

            if (intent.hasExtra(EXTRA_NOTE)) {
                currentNote = try {
                    intent.getSerializableExtra(EXTRA_NOTE) as? Note
                } catch (e: Exception) {
                    null
                }
                currentNote?.let {
                    title = "Edytuj notatkę"
                    editTextTitle.setText(it.title)
                    editTextContent.setText(it.content)
                    spinnerCategory.setSelection(it.categoryId)
                    switchPriority.isChecked = it.priority == 1
                    reminderTime = it.reminderTime
                    updateReminderText()
                }
            } else {
                title = "Nowa notatka"
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Błąd startu: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_edit_note, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveNote()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveNote() {
        val titleText = editTextTitle.text.toString().trim()
        val contentText = editTextContent.text.toString().trim()
        val categoryId = spinnerCategory.selectedItemPosition
        val priority = if (switchPriority.isChecked) 1 else 0

        if (titleText.isEmpty()) {
            Toast.makeText(this, "Podaj tytuł", Toast.LENGTH_SHORT).show()
            return
        }

        val note = if (currentNote == null) {
            Note(
                title = titleText,
                content = contentText,
                categoryId = categoryId,
                priority = priority,
                reminderTime = reminderTime
            )
        } else {
            currentNote!!.copy(
                title = titleText,
                content = contentText,
                categoryId = categoryId,
                priority = priority,
                reminderTime = reminderTime
            )
        }

        if (currentNote == null) {
            noteViewModel.insert(note)
            // Uwaga: W przypadku nowej notatki ID jest generowane przez Room.
            // Aby ustawić alarm z poprawnym ID dla nowej notatki, w prawdziwej aplikacji
            // należałoby poczekać na wynik inserta lub użyć innego identyfikatora.
            // Zgodnie z poleceniem planujemy alarm tutaj.
            if (reminderTime > System.currentTimeMillis()) {
                scheduleAlarm(note)
            }
        } else {
            noteViewModel.update(note)
            if (reminderTime > System.currentTimeMillis()) {
                scheduleAlarm(note)
            }
        }

        finish()
    }

    private fun scheduleAlarm(note: Note) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        
        // Sprawdzenie uprawnienia dla dokładnych alarmów na nowszych systemach
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("NOTE_ID", note.id)
            putExtra("NOTE_TITLE", note.title)
            putExtra("NOTE_OBJECT", note)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            note.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            Toast.makeText(this, "Brak uprawnień do ustawienia alarmu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDateTimePicker() {
        val currentCalendar = Calendar.getInstance()
        if (reminderTime > 0) {
            currentCalendar.timeInMillis = reminderTime
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)

                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        selectedCalendar.set(Calendar.MINUTE, minute)
                        selectedCalendar.set(Calendar.SECOND, 0)
                        selectedCalendar.set(Calendar.MILLISECOND, 0)

                        reminderTime = selectedCalendar.timeInMillis
                        updateReminderText()
                    },
                    currentCalendar.get(Calendar.HOUR_OF_DAY),
                    currentCalendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateReminderText() {
        if (reminderTime > 0) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            textViewReminder.text = sdf.format(Date(reminderTime))
        } else {
            textViewReminder.text = ""
        }
    }

    companion object {
        const val EXTRA_NOTE = "com.example.quicknote.EXTRA_NOTE"
    }
}
