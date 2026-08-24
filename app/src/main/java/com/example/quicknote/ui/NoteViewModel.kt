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
    private val _searchQuery = MutableLiveData<String>("")
    private val _filterCategory = MutableLiveData<Long>(-1L)
    private val _sortOption = MutableLiveData<SortOption>(SortOption.BY_DATE)
    val currentSortOption: LiveData<SortOption> get() = _sortOption

    enum class SortOption {
        BY_DATE, BY_PRIORITY, BY_TITLE
    }
    
    private val combinedFilter = MediatorLiveData<Triple<String?, Long?, SortOption?>>().apply {
        addSource(_searchQuery) { query -> value = Triple(query, _filterCategory.value, _sortOption.value) }
        addSource(_filterCategory) { category -> value = Triple(_searchQuery.value, category, _sortOption.value) }
        addSource(_sortOption) { sort -> value = Triple(_searchQuery.value, _filterCategory.value, sort) }
    }

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

    val allCategories: LiveData<List<Category>>

    init {
        val database = AppDatabase.getDatabase(application)
        val noteDao = database.noteDao()
        val categoryDao = database.categoryDao()
        repository = NoteRepository(noteDao, categoryDao)
        allCategories = repository.getAllCategories()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterCategory(categoryId: Long) {
        _filterCategory.value = categoryId
    }

    fun getFilterCategory(): Long = _filterCategory.value ?: -1L

    fun setSortOption(sortOption: SortOption) {
        _sortOption.value = sortOption
    }

    fun insert(note: Note, onResult: (Long) -> Unit = {}) = viewModelScope.launch {
        val id = repository.insertNote(note)
        onResult(id)
    }

    fun update(note: Note) = viewModelScope.launch {
        repository.updateNote(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        AlarmHelper.cancelAlarm(getApplication<Application>().applicationContext, note.id)
        repository.deleteNote(note)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAllNotes()
    }

    fun addCategory(name: String, colorHex: String, onCategoryAdded: (Long) -> Unit = {}) = viewModelScope.launch {
        val newCategory = Category(name = name, colorHex = colorHex)
        val id = repository.insertCategory(newCategory)
        onCategoryAdded(id)
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }
}
