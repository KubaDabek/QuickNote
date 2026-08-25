package com.example.quicknote.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.graphics.Color
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quicknote.R
import com.example.quicknote.data.Category
import com.example.quicknote.data.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter dla RecyclerView wyświetlający listę notatek na ekranie głównym.
 * Obsługuje dynamiczne wiązanie danych, kolory kategorii i ikony priorytetu.
 */
class NoteAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    private var categories: List<Category> = emptyList()

    /** Aktualizuje wewnętrzną listę kategorii dla poprawnego wyświetlania nazw w notatkach. */
    fun setCategories(categories: List<Category>) {
        this.categories = categories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        val category = categories.find { it.id == note.categoryId }
        holder.bind(note, category, onNoteClick, onNoteLongClick)
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        private val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        private val textViewContent: TextView = itemView.findViewById(R.id.textViewContent)
        private val textViewCategory: TextView = itemView.findViewById(R.id.textViewCategory)
        private val imageViewPriority: ImageView = itemView.findViewById(R.id.imageViewPriority)
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun bind(
            note: Note,
            category: Category?,
            onNoteClick: (Note) -> Unit,
            onNoteLongClick: (Note) -> Unit
        ) {
            textViewTitle.text = note.title
            textViewContent.text = note.content
            textViewDate.text = dateFormat.format(Date(note.createdAt))

            if (note.priority == 1) {
                imageViewPriority.visibility = View.VISIBLE
                textViewTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.priority_orange))
            } else {
                imageViewPriority.visibility = View.GONE
                textViewTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
            }

            if (category != null) {
                textViewCategory.visibility = View.VISIBLE
                textViewCategory.text = category.name
                try {
                    val color = Color.parseColor(category.colorHex)
                    textViewCategory.backgroundTintList = ColorStateList.valueOf(color)
                } catch (e: Exception) {
                    textViewCategory.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
                }
            } else {
                textViewCategory.visibility = View.GONE
            }

            itemView.setOnClickListener { onNoteClick(note) }
            itemView.setOnLongClickListener {
                onNoteLongClick(note)
                true
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}
