package com.example.notesapp.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.notesapp.model.Note

// it will be automatically called whenever we will update the note
// DiffUtils is used to track changes made to the RecyclerView Adapter.
// DiffUtil notifies the RecyclerView of any changes to the data set using the following methods: notifyItemMoved. notifyItemRangeChanged.
class DiffUtilCallback: DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.id == newItem.id
    }
}