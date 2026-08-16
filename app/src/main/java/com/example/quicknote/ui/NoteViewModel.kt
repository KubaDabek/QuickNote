package com.example.quicknote.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.quicknote.data.AppDatabase
import com.example.quicknote.data.Note
import com.example.quicknote.data.NoteRepository
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository
    private val _searchQuery = MutableLiveData<String>("")
    private val _filterCategory = MutableLiveData<Int>(-1)
    private val _sortOption = MutableLiveData<SortOption>(SortOption.BY_DATE)
    val currentSortOption: LiveData<SortOption> get() = _sortOption

    enum class SortOption {
        BY_DATE, BY_PRIORITY, BY_TITLE
    }
    
    private val combinedFilter = MediatorLiveData<Triple<String?, Int?, SortOption?>>().apply {
        addSource(_searchQuery) { query -> value = Triple(query, _filterCategory.value, _sortOption.value) }
        addSource(_filterCategory) { category -> value = Triple(_searchQuery.value, category, _sortOption.value) }
        addSource(_sortOption) { sort -> value = Triple(_searchQuery.value, _filterCategory.value, sort) }
    }

    val allNotes: LiveData<List<Note>> = combinedFilter.switchMap { filter ->
        val query = filter.first ?: ""
        val category = filter.second ?: -1
        val sort = filter.third ?: SortOption.BY_DATE
        
        repository.searchNotes(query, category).map { notes ->
            when (sort) {
                SortOption.BY_DATE -> notes.sortedByDescending { it.createdAt }
                SortOption.BY_PRIORITY -> notes.sortedWith(compareByDescending<Note> { it.priority }.thenByDescending { it.createdAt })
                SortOption.BY_TITLE -> notes.sortedBy { it.title.lowercase() }
            }
        }
    }

    init {
        val noteDao = AppDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterCategory(categoryId: Int) {
        _filterCategory.value = categoryId
    }

    fun setSortOption(sortOption: SortOption) {
        _sortOption.value = sortOption
    }

    fun insert(note: Note) = viewModelScope.launch {
        repository.insertNote(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        repository.updateNote(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.deleteNote(note)
    }

    fun search(query: String): LiveData<List<Note>> {
        return repository.searchNotes(query)
    }
}
