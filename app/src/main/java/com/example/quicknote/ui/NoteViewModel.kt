package com.example.quicknote.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
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
    
    private val combinedFilter = MediatorLiveData<Pair<String?, Int?>>().apply {
        addSource(_searchQuery) { query -> value = Pair(query, _filterCategory.value) }
        addSource(_filterCategory) { category -> value = Pair(_searchQuery.value, category) }
    }

    val allNotes: LiveData<List<Note>> = combinedFilter.switchMap { filter ->
        val query = filter.first ?: ""
        val category = filter.second ?: -1
        repository.searchNotes(query, category)
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
