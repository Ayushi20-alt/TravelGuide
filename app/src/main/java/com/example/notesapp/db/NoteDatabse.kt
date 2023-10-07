package com.example.notesapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.notesapp.model.Note

@Database(
    entities = [Note::class],
    version = 1,
    exportSchema = false
)
abstract class NoteDatabse : RoomDatabase() {

    abstract fun getNotesDao() : DAO
    // we will use singleton pattern cause we want only one instance at a single time
    companion object{
        @Volatile
        private var instance : NoteDatabse?= null
        // lock is used to create database inside the background thread
        // and we want to use only one thread
        private val Lock = Any()

        // calling the function
        operator fun invoke(context: Context) = instance ?: synchronized(Lock){
            instance ?: createDatabase(context).also {
                instance = it
            }
        }


        private fun createDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            NoteDatabse::class.java,
            "note_database"
        ).build()
    }
}