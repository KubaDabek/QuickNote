package com.example.quicknote.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.quicknote.AlarmHelper
import com.example.quicknote.data.AppDatabase
import com.example.quicknote.data.Category
import com.example.quicknote.data.Note
import com.example.quicknote.data.NoteRepository
import kotlinx.coroutines.launch

/**
 * ViewModel odpowiedzialny za dostarczanie danych dla interfejsu użytkownika
 * oraz obsługę logiki biznesowej związanej z notatkami.
 * Obsługuje filtrowanie, wyszukiwanie i sortowanie w czasie rzeczywistym.
 *
 * @param application Kontekst aplikacji.
 */
class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository
    /** Przechowuje aktualną frazę wyszukiwania wpisaną przez użytkownika. */
    private val _searchQuery = MutableLiveData<String>("")
    /** Przechowuje identyfikator wybranej kategorii do filtrowania (-1L = wszystkie). */
    private val _filterCategory = MutableLiveData<Long>(-1L)
    /** Przechowuje aktualnie wybraną opcję sortowania. */
    private val _sortOption = MutableLiveData<SortOption>(SortOption.BY_DATE)
    /** Publiczne LiveData do obserwowania aktualnego stanu sortowania. */
    val currentSortOption: LiveData<SortOption> get() = _sortOption

    /** Definicja dostępnych trybów sortowania notatek. */
    enum class SortOption {
        BY_DATE, BY_PRIORITY, BY_TITLE
    }
    
    /** Obiekt pośredniczący, który łączy zmiany w wyszukiwaniu, kategorii i sortowaniu. */
    private val combinedFilter = MediatorLiveData<Triple<String?, Long?, SortOption?>>().apply {
        addSource(_searchQuery) { query -> value = Triple(query, _filterCategory.value, _sortOption.value) }
        addSource(_filterCategory) { category -> value = Triple(_searchQuery.value, category, _sortOption.value) }
        addSource(_sortOption) { sort -> value = Triple(_searchQuery.value, _filterCategory.value, sort) }
    }

    /** Główna lista notatek, automatycznie aktualizowana przy każdej zmianie filtrów lub sortowania. */
    val allNotes: LiveData<List<Note>> = combinedFilter.switchMap { filter ->
        val query = filter.first ?: ""
        val category = filter.second ?: -1L
        val sort = filter.third ?: SortOption.BY_DATE
        
        repository.searchNotes(query, category).map { notes ->
            when (sort) {
                SortOption.BY_DATE -> notes.sortedByDescending { it.createdAt }
                SortOption.BY_PRIORITY -> notes.sortedWith(compareByDescending<Note> { it.priority }.thenByDescending { it.createdAt })
                SortOption.BY_TITLE -> notes.sortedBy { it.title.lowercase() }
            }
        }
    }

    /** Lista wszystkich dostępnych kategorii użytkownika. */
    val allCategories: LiveData<List<Category>>

    init {
        val database = AppDatabase.getDatabase(application)
        val noteDao = database.noteDao()
        val categoryDao = database.categoryDao()
        repository = NoteRepository(noteDao, categoryDao)
        allCategories = repository.getAllCategories()
    }

    /** Aktualizuje frazę wyszukiwania. */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** Zmienia filtr kategorii na wybrany identyfikator. */
    fun setFilterCategory(categoryId: Long) {
        _filterCategory.value = categoryId
    }

    /** Zwraca aktualnie aktywny identyfikator filtra kategorii. */
    fun getFilterCategory(): Long = _filterCategory.value ?: -1L

    /** Zmienia tryb sortowania listy głównej. */
    fun setSortOption(sortOption: SortOption) {
        _sortOption.value = sortOption
    }

    /** Dodaje nową notatkę i zwraca jej ID przez callback (używane do planowania alarmów). */
    fun insert(note: Note, onResult: (Long) -> Unit = {}) = viewModelScope.launch {
        val id = repository.insertNote(note)
        onResult(id)
    }

    /** Aktualizuje istniejącą notatkę w bazie. */
    fun update(note: Note) = viewModelScope.launch {
        repository.updateNote(note)
    }

    /** Usuwa notatkę i automatycznie anuluje powiązany z nią alarm. */
    fun delete(note: Note) = viewModelScope.launch {
        AlarmHelper.cancelAlarm(getApplication<Application>().applicationContext, note.id)
        repository.deleteNote(note)
    }

    /** Usuwa wszystkie notatki z bazy danych. */
    fun deleteAll() = viewModelScope.launch {
        repository.deleteAllNotes()
    }

    /** Dodaje nową kategorię o określonej nazwie i kolorze. */
    fun addCategory(name: String, colorHex: String, onCategoryAdded: (Long) -> Unit = {}) = viewModelScope.launch {
        val newCategory = Category(name = name, colorHex = colorHex)
        val id = repository.insertCategory(newCategory)
        onCategoryAdded(id)
    }

    /** Usuwa kategorię i czyści powiązania w notatkach. */
    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }
}
