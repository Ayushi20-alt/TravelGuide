package com.example.notesapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.*
import com.example.notesapp.model.Note

// in dao we only mention the methods and working inside this entities
@Dao
interface DAO {

    // we will use kotlin coroutines so that it does work on the background thread and do not work on the main thread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
     fun addNote(note:Note)

    @Update
     fun updateNote(note:Note)

    // we are returning the livedate which already work on the background thread so we do not need to use suspend
    @Query("SELECT * FROM Note ORDER BY id DESC")
    fun getAllNote() : LiveData<List<Note>>

    @Query("SELECT * FROM Note WHERE title LIKE :query OR content LIKE :query OR date LIKE :query ORDER BY id DESC")
    fun searchNote(query: String): LiveData<List<Note>>

    @Delete
     fun deleteNote(note:Note)
}