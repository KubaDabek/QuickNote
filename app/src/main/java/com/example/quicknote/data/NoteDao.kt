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
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND (:categoryId = -1 OR categoryId = :categoryId OR ((SELECT name FROM categories WHERE id = :categoryId) = 'Inne' AND categoryId <= 0)) ORDER BY createdAt DESC")
    fun searchNotes(query: String, categoryId: Long = -1): LiveData<List<Note>>

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Query("SELECT * FROM notes WHERE reminderTime > :currentTime")
    suspend fun getFutureReminders(currentTime: Long): List<Note>
}
