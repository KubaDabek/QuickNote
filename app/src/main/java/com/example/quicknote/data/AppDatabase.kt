package com.example.quicknote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Główna klasa bazy danych Room dla aplikacji.
 * Przechowuje tabele notatek i kategorii.
 */
@Database(entities = [Note::class, Category::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    /** Zwraca obiekt DAO dla notatek. */
    abstract fun noteDao(): NoteDao
    /** Zwraca obiekt DAO dla kategorii. */
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Inicjalizuje lub zwraca istniejącą instancję bazy danych (Singleton). */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "note_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
