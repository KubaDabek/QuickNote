package com.example.quicknote.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quicknote.R
import com.example.quicknote.data.Category
import com.google.android.material.card.MaterialCardView

class CategoryAdapter(
    private val onCategoryClick: (Category) -> Unit,
    private val onAddCategoryClick: () -> Unit
) : ListAdapter<Category, RecyclerView.ViewHolder>(CategoryDiffCallback()) {

    private var selectedCategoryId: Long = -1
    private val TYPE_CATEGORY = 0
    private val TYPE_ADD = 1

    fun setSelectedCategory(id: Long) {
        val oldSelectedId = selectedCategoryId
        selectedCategoryId = id
        notifyDataSetChanged() // W tym przypadku notifyDataSetChanged jest akceptowalne dla małej listy chipów
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) TYPE_ADD else TYPE_CATEGORY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_CATEGORY) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_chip, parent, false)
            CategoryViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_add, parent, false)
            AddViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CategoryViewHolder) {
            val category = getItem(position)
            holder.bind(category, category.id == selectedCategoryId, onCategoryClick)
        } else if (holder is AddViewHolder) {
            holder.bind(onAddCategoryClick)
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardCategory: MaterialCardView = itemView.findViewById(R.id.cardCategory)
        private val viewCategoryColor: View = itemView.findViewById(R.id.viewCategoryColor)
        private val textViewCategoryName: TextView = itemView.findViewById(R.id.textViewCategoryName)

        fun bind(category: Category, isSelected: Boolean, onClick: (Category) -> Unit) {
            textViewCategoryName.text = category.name
            try {
                viewCategoryColor.background.setTint(Color.parseColor(category.colorHex))
            } catch (e: Exception) {
                viewCategoryColor.background.setTint(Color.GRAY)
            }

            if (isSelected) {
                cardCategory.strokeWidth = 4
            } else {
                cardCategory.strokeWidth = 0
            }

            itemView.setOnClickListener { onClick(category) }
        }
    }

    class AddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(onClick: () -> Unit) {
            itemView.setOnClickListener { onClick() }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}
