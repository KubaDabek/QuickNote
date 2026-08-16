package com.example.quicknote.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Klasa reprezentująca encję notatki w bazie danych Room.
 *
 * @property id Unikalny identyfikator notatki (klucz główny, autogenerowany).
 * @property title Tytuł notatki.
 * @property content Treść notatki.
 * @property categoryId Identyfikator kategorii (0=Brak, 1=Praca, 2=Szkoła, 3=Dom, 4=Inne).
 * @property priority Poziom priorytetu (0=Zwykła, 1=Ważna).
 * @property createdAt Znacznik czasu utworzenia notatki (w milisekundach).
 * @property reminderTime Czas zaplanowanego przypomnienia (w milisekundach, 0 jeśli brak).
 */
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
