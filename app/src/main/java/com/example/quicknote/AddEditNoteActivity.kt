package com.example.quicknote

import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
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
import androidx.recyclerview.widget.RecyclerView
import com.example.quicknote.data.Category
import com.example.quicknote.data.Note
import com.example.quicknote.ui.CategoryAdapter
import com.example.quicknote.ui.NoteViewModel
import com.example.quicknote.ui.NoteViewModelFactory
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Aktywność umożliwiająca tworzenie nowej notatki lub edycję istniejącej.
 * Pozwala na wybór kategorii, ustawienie priorytetu oraz zaplanowanie przypomnienia.
 */
class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var editTextTitle: TextInputEditText
    private lateinit var editTextContent: TextInputEditText
    private lateinit var recyclerViewCategories: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var switchPriority: SwitchMaterial
    private lateinit var btnSetReminder: Button
    private lateinit var textViewReminder: TextView
    private lateinit var noteViewModel: NoteViewModel
    /** Przechowuje obiekt notatki w trybie edycji (null dla nowej notatki). */
    private var currentNote: Note? = null
    /** Wybrany czas przypomnienia w milisekundach. */
    private var reminderTime: Long = 0
    /** ID aktualnie zaznaczonej kategorii (0L = brak). */
    private var selectedCategoryId: Long = 0L
    /** Lista wszystkich kategorii pobrana z ViewModela. */
    private var categoriesList: List<Category> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_add_edit_note)

            val root: View = findViewById(R.id.add_edit_content)
            val toolbar: Toolbar = findViewById(R.id.toolbar)
            val appBar: View = toolbar.parent.parent as View // AppBarLayout

            // Dopasowanie układu do pasków systemowych (Toolbar na górze, treść nad paskiem nawigacji)
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                appBar.updatePadding(top = systemBars.top)
                root.updatePadding(bottom = systemBars.bottom)
                insets
            }

            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            editTextTitle = findViewById(R.id.editTextTitle)
            editTextContent = findViewById(R.id.editTextContent)
            recyclerViewCategories = findViewById(R.id.recyclerViewCategories)
            switchPriority = findViewById(R.id.switchPriority)
            btnSetReminder = findViewById(R.id.btnSetReminder)
            textViewReminder = findViewById(R.id.textViewReminder)

            // Kliknięcie otwiera kaskadowo wybór daty i godziny
            btnSetReminder.setOnClickListener {
                showDateTimePicker()
            }

            // Inicjalizacja listy kategorii (chipów) do wyboru
            categoryAdapter = CategoryAdapter(
                onCategoryClick = { category ->
                    if (selectedCategoryId == category.id) {
                        selectedCategoryId = 0L // Ponowne kliknięcie odznacza kategorię
                    } else {
                        selectedCategoryId = category.id
                    }
                    categoryAdapter.setSelectedCategory(selectedCategoryId)
                },
                onAddCategoryClick = {
                    showAddCategoryDialog()
                }
            )
            recyclerViewCategories.adapter = categoryAdapter

            val factory = NoteViewModelFactory(application)
            noteViewModel = ViewModelProvider(this, factory)[NoteViewModel::class.java]

            noteViewModel.allCategories.observe(this) { categories ->
                categoriesList = categories
                categoryAdapter.submitList(categories)
            }

            // Sprawdzenie czy aktywność uruchomiona w trybie edycji (przekazano EXTRA_NOTE)
            if (intent.hasExtra(EXTRA_NOTE)) {
                currentNote = try {
                    intent.getSerializableExtra(EXTRA_NOTE) as? Note
                } catch (_: Exception) {
                    null
                }
                currentNote?.let {
                    title = "Edytuj notatkę"
                    editTextTitle.setText(it.title)
                    editTextContent.setText(it.content)
                    selectedCategoryId = it.categoryId
                    categoryAdapter.setSelectedCategory(selectedCategoryId)
                    switchPriority.isChecked = it.priority == 1
                    reminderTime = it.reminderTime
                    updateReminderText()
                    invalidateOptionsMenu()
                }
            } else {
                title = "Nowa notatka"
            }
        } catch (_: Exception) {
            Toast.makeText(this, "Błąd startu", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    /** Wyświetla dialog do tworzenia nowej kategorii z wyborem koloru. */
    private fun showAddCategoryDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null)
        val editTextName = dialogView.findViewById<TextInputEditText>(R.id.editTextCategoryName)
        var selectedColor = "#EF4444" // Czerwony jako domyślny

        val colors = listOf(
            dialogView.findViewById<View>(R.id.color1),
            dialogView.findViewById<View>(R.id.color2),
            dialogView.findViewById<View>(R.id.color3),
            dialogView.findViewById<View>(R.id.color4),
            dialogView.findViewById<View>(R.id.color5),
            dialogView.findViewById<View>(R.id.color6)
        )

        colors.forEach { view ->
            val colorHex = view.tag.toString()
            view.background.setTint(android.graphics.Color.parseColor(colorHex))
            
            view.setOnClickListener {
                selectedColor = colorHex
                colors.forEach { v -> 
                    v.foreground = null
                    v.alpha = 0.7f 
                }
                view.foreground = ContextCompat.getDrawable(this, R.drawable.circle_selection_background)
                view.alpha = 1.0f
            }
        }
        
        colors[0].foreground = ContextCompat.getDrawable(this, R.drawable.circle_selection_background)
        colors[0].alpha = 1.0f

        AlertDialog.Builder(this)
            .setTitle("Nowa kategoria")
            .setView(dialogView)
            .setPositiveButton("Dodaj", null) // Ustawiamy null, aby obsłużyć kliknięcie ręcznie
            .setNegativeButton("Anuluj", null)
            .create()
            .apply {
                show()
                // Nadpisujemy działanie przycisku "Dodaj", aby dialog nie zamykał się automatycznie
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val name = editTextName.text.toString().trim()
                    if (name.isNotEmpty()) {
                        noteViewModel.addCategory(name, selectedColor) { newId ->
                            runOnUiThread {
                                selectedCategoryId = newId
                                categoryAdapter.setSelectedCategory(selectedCategoryId)
                                val targetPos = categoryAdapter.itemCount - 2
                                if (targetPos >= 0) {
                                    recyclerViewCategories.smoothScrollToPosition(targetPos)
                                }
                            }
                        }
                        dismiss() // Zamykamy tylko dialog, aktywność zostaje
                    } else {
                        Toast.makeText(this@AddEditNoteActivity, "Podaj nazwę kategorii", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_edit_note, menu)
        menu.findItem(R.id.action_share)?.isVisible = currentNote != null
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveNote()
                true
            }
            R.id.action_share -> {
                shareNote(currentNote)
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /** Waliduje i zapisuje notatkę w bazie oraz planuje alarm systemowy. */
    private fun saveNote() {
        val titleText = editTextTitle.text.toString().trim()
        val contentText = editTextContent.text.toString().trim()
        val priority = if (switchPriority.isChecked) 1 else 0

        if (titleText.isEmpty()) {
            Toast.makeText(this, "Podaj tytuł", Toast.LENGTH_SHORT).show()
            return
        }

        val note = if (currentNote == null) {
            Note(
                title = titleText,
                content = contentText,
                categoryId = selectedCategoryId,
                priority = priority,
                reminderTime = reminderTime
            )
        } else {
            currentNote!!.copy(
                title = titleText,
                content = contentText,
                categoryId = selectedCategoryId,
                priority = priority,
                reminderTime = reminderTime
            )
        }

        if (currentNote == null) {
            noteViewModel.insert(note) { newId ->
                if (reminderTime > System.currentTimeMillis()) {
                    val noteWithId = note.copy(id = newId)
                    AlarmHelper.scheduleAlarm(this, noteWithId)
                }
            }
        } else {
            noteViewModel.update(note)
            if (reminderTime > System.currentTimeMillis()) {
                AlarmHelper.scheduleAlarm(this, note)
            } else {
                AlarmHelper.cancelAlarm(this, note.id)
            }
        }

        finish()
    }

    /** Wyświetla selektor daty, a następnie godziny dla przypomnienia. */
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

    /** Aktualizuje etykietę tekstową z wybraną datą przypomnienia. */
    private fun updateReminderText() {
        if (reminderTime > 0) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            textViewReminder.text = sdf.format(Date(reminderTime))
        } else {
            textViewReminder.text = ""
        }
    }

    /** Funkcja pomocnicza do udostępniania treści notatki. */
    private fun shareNote(note: Note?) {
        note?.let {
            val importance = if (it.priority == 1) "Ważna" else "Zwykła"
            val category = categoriesList.find { cat -> cat.id == it.categoryId }?.name ?: "Brak"

            val shareText = """
                Ważność: $importance
                Kategoria: $category
                Tytuł: "${it.title}"
                Tekst: "${it.content}"
            """.trimIndent()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, it.title)
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, "Udostępnij notatkę"))
        }
    }

    override fun finish() {
        super.finish()
        // Animacja powrotu w prawo
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    companion object {
        const val EXTRA_NOTE = "com.example.quicknote.EXTRA_NOTE"
    }
}
