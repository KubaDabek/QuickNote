package com.example.quicknote.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteRepository(private val noteDao: NoteDao) {

    fun getAllNotes(): LiveData<List<Note>> {
        return noteDao.getAllNotes()
    }

    suspend fun insertNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.insertNote(note)
        }
    }

    suspend fun updateNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.updateNote(note)
        }
    }

    suspend fun deleteNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNote(note)
        }
    }

    fun searchNotes(query: String): LiveData<List<Note>> {
        return noteDao.searchNotes(query)
    }
}
