package com.example.quicknote.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

import androidx.room.Transaction

/**
 * Interfejs DAO dla tabeli kategorii.
 * Umożliwia zarządzanie kategoriami oraz ich powiązaniami z notatkami.
 */
@Dao
interface CategoryDao {
    /** Pobiera listę wszystkich kategorii posortowaną alfabetycznie. */
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    /** Dodaje nową kategorię i zwraca jej ID. */
    @Insert
    suspend fun insertCategory(category: Category): Long

    /** Resetuje przypisanie kategorii w notatkach przed usunięciem samej kategorii. */
    @Query("UPDATE notes SET categoryId = -1 WHERE categoryId = :categoryId")
    suspend fun clearNotesCategory(categoryId: Long)

    /** Usuwa kategorię z bazy danych. */
    @Delete
    suspend fun deleteCategoryInternal(category: Category)

    /** Usuwa kategorię w bezpieczny sposób, odpinając ją najpierw od wszystkich notatek. */
    @Transaction
    suspend fun deleteCategoryAndClearNotes(category: Category) {
        clearNotesCategory(category.id)
        deleteCategoryInternal(category)
    }
}
