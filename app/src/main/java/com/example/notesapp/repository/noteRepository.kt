package com.example.notesapp.repository

import com.example.notesapp.db.NoteDatabse
import com.example.notesapp.model.Note

class noteRepository(private val db: NoteDatabse) {

    fun getNote() = db.getNotesDao().getAllNote()

    fun searchNote(query: String) = db.getNotesDao().searchNote(query)

    fun addNote(note:Note) = db.getNotesDao().addNote(note)

     fun updateNote(note: Note) = db.getNotesDao().updateNote(note)

     fun deleteNote(note: Note) = db.getNotesDao().deleteNote(note)
}