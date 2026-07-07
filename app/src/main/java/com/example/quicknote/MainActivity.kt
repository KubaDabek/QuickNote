package com.example.quicknote

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quicknote.ui.NoteAdapter
import com.example.quicknote.ui.NoteViewModel
import com.example.quicknote.ui.NoteViewModelFactory
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

        adapter = NoteAdapter(
            onNoteClick = { note ->
                // To be implemented: Open AddEditNoteActivity for editing
            },
            onNoteLongClick = { note ->
                // To be implemented: Show delete confirmation or other options
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
            // To be implemented: Open AddEditNoteActivity for adding
            // val intent = Intent(this, AddEditNoteActivity::class.java)
            // startActivity(intent)
        }
    }
}
