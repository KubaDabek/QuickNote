package com.example.quicknote

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quicknote.data.Note
import com.example.quicknote.ui.NoteAdapter
import com.example.quicknote.ui.NoteViewModel
import com.example.quicknote.ui.NoteViewModelFactory
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val textViewEmpty: TextView = findViewById(R.id.textViewEmpty)
        val fabAdd: FloatingActionButton = findViewById(R.id.fabAdd)
        val chipGroupFilter: ChipGroup = findViewById(R.id.chipGroupFilter)

        chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: R.id.chipAll
            val categoryId = when (checkedId) {
                R.id.chipWork -> 1
                R.id.chipSchool -> 2
                R.id.chipHome -> 3
                R.id.chipOther -> 4
                else -> -1
            }
            noteViewModel.setFilterCategory(categoryId)
        }

        adapter = NoteAdapter(
            onNoteClick = { note ->
                openEditNoteScreen(note)
            },
            onNoteLongClick = { note ->
                showOptionsDialog(note)
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val factory = NoteViewModelFactory(application)
        noteViewModel = ViewModelProvider(this, factory)[NoteViewModel::class.java]

        noteViewModel.allNotes.observe(this) { notes ->
            adapter.submitList(notes)
            if (notes.isEmpty()) {
                textViewEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                textViewEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }

        fabAdd.setOnClickListener {
            val intent = Intent(this, AddEditNoteActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchView.windowToken, 0)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                noteViewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })

        return true
    }

    private fun openEditNoteScreen(note: Note) {
        val intent = Intent(this, AddEditNoteActivity::class.java)
        intent.putExtra(AddEditNoteActivity.EXTRA_NOTE, note)
        startActivity(intent)
    }

    private fun showOptionsDialog(note: Note) {
        val options = arrayOf("Edytuj", "Usuń")
        AlertDialog.Builder(this)
            .setTitle("Opcje")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openEditNoteScreen(note)
                    1 -> showDeleteConfirmationDialog(note)
                }
            }
            .show()
    }

    private fun showDeleteConfirmationDialog(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Usuń notatkę")
            .setMessage("Czy na pewno chcesz usunąć tę notatkę?")
            .setPositiveButton("Usuń") { _, _ ->
                noteViewModel.delete(note)
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }
}
