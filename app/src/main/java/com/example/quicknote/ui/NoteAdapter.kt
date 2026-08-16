package com.example.quicknote.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quicknote.R
import com.example.quicknote.data.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note, onNoteClick, onNoteLongClick)
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        private val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        private val textViewContent: TextView = itemView.findViewById(R.id.textViewContent)
        private val textViewCategory: TextView = itemView.findViewById(R.id.textViewCategory)
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun bind(
            note: Note,
            onNoteClick: (Note) -> Unit,
            onNoteLongClick: (Note) -> Unit
        ) {
            textViewTitle.text = note.title
            textViewContent.text = note.content
            textViewDate.text = dateFormat.format(Date(note.createdAt))

            if (note.categoryId != 0) {
                textViewCategory.visibility = View.VISIBLE
                val (catName, catColor) = when (note.categoryId) {
                    1 -> "PRACA" to R.color.cat_work
                    2 -> "SZKOŁA" to R.color.cat_school
                    3 -> "DOM" to R.color.cat_home
                    else -> "INNE" to R.color.cat_other
                }
                textViewCategory.text = catName
                textViewCategory.backgroundTintList = ContextCompat.getColorStateList(itemView.context, catColor)
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
