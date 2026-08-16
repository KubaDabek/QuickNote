package com.example.quicknote

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quicknote.data.Note
import com.example.quicknote.ui.NoteAdapter
import com.example.quicknote.ui.NoteViewModel
import com.example.quicknote.ui.NoteViewModelFactory
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var adapter: NoteAdapter
    private var currentSortId: Int = R.id.sort_date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val textViewEmpty: TextView = findViewById(R.id.textViewEmpty)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        val fabAdd: FloatingActionButton = findViewById(R.id.fabAdd)
        val chipGroupFilter: ChipGroup = findViewById(R.id.chipGroupFilter)

        chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: R.id.chipAll
            val categoryId = when (checkedId) {
                R.id.chipWork -> 1
                R.id.chipSchool -> 2
                R.id.chipHome -> 3
                R.id.chipOther -> 4
                else -> -1
            }
            noteViewModel.setFilterCategory(categoryId)
        }

        adapter = NoteAdapter(
            onNoteClick = { note ->
                openEditNoteScreen(note)
            },
            onNoteLongClick = { note ->
                showOptionsDialog(note)
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        setupSwipeToDelete(recyclerView)

        val factory = NoteViewModelFactory(application)
        noteViewModel = ViewModelProvider(this, factory)[NoteViewModel::class.java]

        noteViewModel.allNotes.observe(this) { notes ->
            progressBar.visibility = View.GONE
            adapter.submitList(notes)
            if (notes.isEmpty()) {
                textViewEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                textViewEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }

        noteViewModel.currentSortOption.observe(this) { sortOption ->
            currentSortId = when (sortOption) {
                NoteViewModel.SortOption.BY_DATE -> R.id.sort_date
                NoteViewModel.SortOption.BY_PRIORITY -> R.id.sort_priority
                NoteViewModel.SortOption.BY_TITLE -> R.id.sort_title
                else -> R.id.sort_date
            }
            invalidateOptionsMenu()
        }

        fabAdd.setOnClickListener {
            val intent = Intent(this, AddEditNoteActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchView.windowToken, 0)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                noteViewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // Znajdź sub-menu sortowania i zaznacz odpowiednią opcję "ptaszkiem"
        val sortItem = menu.findItem(R.id.action_sort)
        sortItem?.subMenu?.let { subMenu ->
            for (i in 0 until subMenu.size()) {
                val item = subMenu.getItem(i)
                item.isChecked = (item.itemId == currentSortId)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                true
            }
            R.id.sort_date -> {
                noteViewModel.setSortOption(NoteViewModel.SortOption.BY_DATE)
                true
            }
            R.id.sort_priority -> {
                noteViewModel.setSortOption(NoteViewModel.SortOption.BY_PRIORITY)
                true
            }
            R.id.sort_title -> {
                noteViewModel.setSortOption(NoteViewModel.SortOption.BY_TITLE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openEditNoteScreen(note: Note) {
        val intent = Intent(this, AddEditNoteActivity::class.java)
        intent.putExtra(AddEditNoteActivity.EXTRA_NOTE, note)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun showOptionsDialog(note: Note) {
        val options = arrayOf("Edytuj", "Udostępnij", "Usuń")
        AlertDialog.Builder(this)
            .setTitle("Opcje")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openEditNoteScreen(note)
                    1 -> shareNote(note)
                    2 -> showDeleteConfirmationDialog(note)
                }
            }
            .show()
    }

    private fun shareNote(note: Note) {
        val importance = if (note.priority == 1) "Ważna" else "Zwykła"
        val category = when (note.categoryId) {
            1 -> "Praca"
            2 -> "Szkoła"
            3 -> "Dom"
            4 -> "Inne"
            else -> "Brak"
        }

        val shareText = """
            Ważność: $importance
            Kategoria: $category
            Tytuł: "${note.title}"
            Tekst: "${note.content}"
        """.trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, note.title)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, "Udostępnij notatkę"))
    }

    private fun showDeleteConfirmationDialog(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Usuń notatkę")
            .setMessage("Czy na pewno chcesz usunąć tę notatkę?")
            .setPositiveButton("Usuń") { _, _ ->
                noteViewModel.delete(note)
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun setupSwipeToDelete(recyclerView: RecyclerView) {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val noteToDelete = adapter.currentList[position]

                noteViewModel.delete(noteToDelete)

                Snackbar.make(recyclerView, "Usunięto notatkę", Snackbar.LENGTH_LONG)
                    .setAction("Cofnij") {
                        noteViewModel.insert(noteToDelete)
                    }
                    .show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}
