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
 * @property categoryId Identyfikator kategorii powiązany z Category.id.
 * @property priority Poziom priorytetu (0=Zwykła, 1=Ważna).
 * @property createdAt Znacznik czasu utworzenia notatki (w milisekundach).
 * @property reminderTime Czas zaplanowanego przypomnienia (w milisekundach, 0 jeśli brak).
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Tytuł wpisany przez użytkownika. */
    val title: String,
    /** Treść notatki. */
    val content: String,
    /** ID kategorii z tabeli 'categories' (0 = brak). */
    val categoryId: Long = 0,
    /** Priorytet: 0 (zwykła) lub 1 (ważna). */
    val priority: Int = 0,
    /** Data utworzenia w milisekundach. */
    val createdAt: Long = System.currentTimeMillis(),
    /** Czas przypomnienia w milisekundach (0 = brak). */
    val reminderTime: Long = 0
) : Serializable
