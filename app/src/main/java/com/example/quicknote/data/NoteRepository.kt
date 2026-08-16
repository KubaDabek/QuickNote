package com.example.quicknote.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Klasa repozytorium zarządzająca dostępem do danych notatek.
 * Stanowi warstwę pośrednią między bazą danych a ViewModel.
 *
 * @property noteDao Obiekt DAO używany do komunikacji z bazą Room.
 */
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

    fun searchNotes(query: String, categoryId: Int = -1): LiveData<List<Note>> {
        return noteDao.searchNotes(query, categoryId)
    }

    suspend fun deleteAllNotes() {
        withContext(Dispatchers.IO) {
            noteDao.deleteAllNotes()
        }
    }
}
