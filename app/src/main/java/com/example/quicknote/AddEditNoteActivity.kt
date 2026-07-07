package com.example.quicknote

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.example.quicknote.data.Note
import com.example.quicknote.ui.NoteViewModel
import com.example.quicknote.ui.NoteViewModelFactory
import com.google.android.material.textfield.TextInputEditText

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var editTextTitle: TextInputEditText
    private lateinit var editTextContent: TextInputEditText
    private lateinit var noteViewModel: NoteViewModel
    private var currentNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextContent = findViewById(R.id.editTextContent)

        val factory = NoteViewModelFactory(application)
        noteViewModel = ViewModelProvider(this, factory)[NoteViewModel::class.java]

        if (intent.hasExtra(EXTRA_NOTE)) {
            currentNote = intent.getSerializableExtra(EXTRA_NOTE) as? Note
            currentNote?.let {
                title = "Edytuj notatkę"
                editTextTitle.setText(it.title)
                editTextContent.setText(it.content)
            }
        } else {
            title = "Nowa notatka"
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

        if (titleText.isEmpty()) {
            Toast.makeText(this, "Podaj tytuł", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentNote == null) {
            val newNote = Note(title = titleText, content = contentText)
            noteViewModel.insert(newNote)
        } else {
            val updatedNote = currentNote!!.copy(title = titleText, content = contentText)
            noteViewModel.update(updatedNote)
        }

        finish()
    }

    companion object {
        const val EXTRA_NOTE = "com.example.quicknote.EXTRA_NOTE"
    }
}
