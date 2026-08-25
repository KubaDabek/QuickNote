package com.example.quicknote.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Interfejs Data Access Object (DAO) dla tabeli notatek.
 * Zawiera metody do wykonywania operacji CRUD oraz zaawansowanego wyszukiwania.
 */
@Dao
interface NoteDao {
    /** Zwraca wszystkie notatki posortowane od najnowszych. */
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): LiveData<List<Note>>

    /** Wstawia nową notatkę lub zastępuje istniejącą o tym samym ID. Zwraca ID nowej notatki. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    /** Aktualizuje dane istniejącej notatki. */
    @Update
    suspend fun updateNote(note: Note)

    /** Usuwa wybraną notatkę z bazy danych. */
    @Delete
    suspend fun deleteNote(note: Note)

    /** Wyszukuje notatki po tytule lub treści, uwzględniając wybrany filtr kategorii.
     * Obsługuje specjalną logikę dla kategorii "Inne". */
    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND (:categoryId = -1 OR categoryId = :categoryId OR ((SELECT name FROM categories WHERE id = :categoryId) = 'Inne' AND categoryId <= 0)) ORDER BY createdAt DESC")
    fun searchNotes(query: String, categoryId: Long = -1): LiveData<List<Note>>

    /** Usuwa wszystkie notatki z tabeli. */
    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    /** Pobiera listę notatek, których czas przypomnienia jest w przyszłości (używane przy starcie systemu). */
    @Query("SELECT * FROM notes WHERE reminderTime > :currentTime")
    suspend fun getFutureReminders(currentTime: Long): List<Note>
}
