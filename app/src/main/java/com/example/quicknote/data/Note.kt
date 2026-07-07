package com.example.quicknote.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val categoryId: Int = 0,
    val priority: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val reminderTime: Long = 0
) : Serializable
