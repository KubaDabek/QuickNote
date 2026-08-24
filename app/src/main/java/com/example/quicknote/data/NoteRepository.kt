package com.example.quicknote.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Klasa repozytorium zarządzająca dostępem do danych notatek.
 * Stanowi warstwę pośrednią między bazą danych a ViewModel.
 *
 * @property noteDao Obiekt DAO dla notatek.
 * @property categoryDao Obiekt DAO dla kategorii.
 */
class NoteRepository(private val noteDao: NoteDao, private val categoryDao: CategoryDao) {

    fun getAllNotes(): LiveData<List<Note>> {
        return noteDao.getAllNotes()
    }

    suspend fun insertNote(note: Note): Long {
        return withContext(Dispatchers.IO) {
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

    fun searchNotes(query: String, categoryId: Long = -1): LiveData<List<Note>> {
        return noteDao.searchNotes(query, categoryId)
    }

    suspend fun deleteAllNotes() {
        withContext(Dispatchers.IO) {
            noteDao.deleteAllNotes()
        }
    }

    fun getAllCategories(): LiveData<List<Category>> {
        return categoryDao.getAllCategories()
    }

    suspend fun insertCategory(category: Category): Long {
        return withContext(Dispatchers.IO) {
            categoryDao.insertCategory(category)
        }
    }

    suspend fun deleteCategory(category: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.deleteCategoryAndClearNotes(category)
        }
    }
}
