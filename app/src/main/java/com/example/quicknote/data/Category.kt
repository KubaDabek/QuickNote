package com.example.quicknote.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Klasa reprezentująca kategorię notatki.
 *
 * @property id Unikalny identyfikator kategorii.
 * @property name Nazwa wyświetlana kategorii.
 * @property colorHex Kolor kategorii zapisany w formacie Hex (np. #RRGGBB).
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String
) : Serializable
