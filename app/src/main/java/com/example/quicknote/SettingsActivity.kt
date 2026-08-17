package com.example.quicknote

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Filter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import com.example.quicknote.ui.NoteViewModel
import com.example.quicknote.ui.NoteViewModelFactory
import com.example.quicknote.ui.ThemeHelper
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class SettingsActivity : AppCompatActivity() {

    private lateinit var noteViewModel: NoteViewModel
    private val themeOptions = arrayOf("Jasny", "Ciemny", "Systemowy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val root: View = findViewById(R.id.settings_content)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val appBar: View = toolbar.parent.parent as View // AppBarLayout

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            appBar.updatePadding(top = systemBars.top)
            root.updatePadding(bottom = systemBars.bottom)
            insets
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val autoCompleteTheme: MaterialAutoCompleteTextView = findViewById(R.id.autoCompleteTheme)
        val btnClearAll: Button = findViewById(R.id.btnClearAll)
        val textViewVersion: TextView = findViewById(R.id.textViewVersion)

        val factory = NoteViewModelFactory(application)
        noteViewModel = ViewModelProvider(this, factory)[NoteViewModel::class.java]

        // Niestandardowy adapter wyłączający filtrowanie, aby lista była zawsze pełna
        val themeAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, themeOptions) {
            override fun getFilter(): Filter {
                return object : Filter() {
                    override fun performFiltering(constraint: CharSequence?): FilterResults {
                        val results = FilterResults()
                        results.values = themeOptions
                        results.count = themeOptions.size
                        return results
                    }
                    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                        notifyDataSetChanged()
                    }
                }
            }
        }
        autoCompleteTheme.setAdapter(themeAdapter)

        val currentTheme = ThemeHelper.getThemePreference(this)
        autoCompleteTheme.setText(themeOptions[currentTheme], false)

        autoCompleteTheme.setOnItemClickListener { _, _, position, _ ->
            // Natychmiast zamykamy listę i zabieramy fokus
            autoCompleteTheme.dismissDropDown()
            autoCompleteTheme.clearFocus()
            
            ThemeHelper.saveThemePreference(this, position)
            ThemeHelper.applyTheme(position)
        }

        btnClearAll.setOnClickListener {
            showClearAllDialog()
        }

        val versionName = try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            pInfo.versionName
        } catch (_: Exception) {
            "1.0"
        }
        textViewVersion.text = "Wersja $versionName"
    }

    private fun showClearAllDialog() {
        AlertDialog.Builder(this)
            .setTitle("Wyczyść wszystko")
            .setMessage("Czy na pewno chcesz usunąć wszystkie notatki? Tej operacji nie da się cofnąć.")
            .setPositiveButton("Usuń wszystko") { _, _ ->
                noteViewModel.deleteAll()
                Toast.makeText(this, "Wszystkie notatki zostały usunięte", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
