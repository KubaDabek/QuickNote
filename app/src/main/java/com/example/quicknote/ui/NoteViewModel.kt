package com.example.quicknote.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.quicknote.data.AppDatabase
import com.example.quicknote.data.Note
import com.example.quicknote.data.NoteRepository
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository
    val allNotes: LiveData<List<Note>>

    init {
        val noteDao = AppDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNotes = repository.getAllNotes()
    }

    fun insert(note: Note) = viewModelScope.launch {
        repository.insertNote(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        repository.updateNote(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.deleteNote(note)
    }

    fun search(query: String): LiveData<List<Note>> {
        return repository.searchNotes(query)
    }
}
