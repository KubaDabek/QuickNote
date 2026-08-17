package com.example.quicknote.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

import androidx.room.Transaction

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    @Insert
    suspend fun insertCategory(category: Category): Long

    @Query("UPDATE notes SET categoryId = -1 WHERE categoryId = :categoryId")
    suspend fun clearNotesCategory(categoryId: Long)

    @Delete
    suspend fun deleteCategoryInternal(category: Category)

    @Transaction
    suspend fun deleteCategoryAndClearNotes(category: Category) {
        clearNotesCategory(category.id)
        deleteCategoryInternal(category)
    }
}
