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

    /** Pobiera wszystkie notatki jako LiveData. */
    fun getAllNotes(): LiveData<List<Note>> {
        return noteDao.getAllNotes()
    }

    /** Zapisuje notatkę w bazie danych i zwraca jej identyfikator. Operacja na wątku IO. */
    suspend fun insertNote(note: Note): Long {
        return withContext(Dispatchers.IO) {
            noteDao.insertNote(note)
        }
    }

    /** Aktualizuje notatkę w bazie danych. Operacja na wątku IO. */
    suspend fun updateNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.updateNote(note)
        }
    }

    /** Usuwa notatkę z bazy danych. Operacja na wątku IO. */
    suspend fun deleteNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNote(note)
        }
    }

    /** Wykonuje wyszukiwanie notatek z uwzględnieniem frazy i kategorii. */
    fun searchNotes(query: String, categoryId: Long = -1): LiveData<List<Note>> {
        return noteDao.searchNotes(query, categoryId)
    }

    /** Usuwa wszystkie rekordy z tabeli notatek. */
    suspend fun deleteAllNotes() {
        withContext(Dispatchers.IO) {
            noteDao.deleteAllNotes()
        }
    }

    /** Pobiera listę wszystkich zdefiniowanych kategorii. */
    fun getAllCategories(): LiveData<List<Category>> {
        return categoryDao.getAllCategories()
    }

    /** Wstawia nową kategorię i zwraca jej ID. */
    suspend fun insertCategory(category: Category): Long {
        return withContext(Dispatchers.IO) {
            categoryDao.insertCategory(category)
        }
    }

    /** Usuwa kategorię i czyści powiązania w notatkach. */
    suspend fun deleteCategory(category: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.deleteCategoryAndClearNotes(category)
        }
    }
}
