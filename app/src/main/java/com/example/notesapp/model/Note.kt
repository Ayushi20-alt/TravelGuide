package com.example.notesapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// serializable cause we are passing the whole data class as a object
@Entity
data class  Note(

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val title: String,
    val content: String,
    val date: String,
    val color: Int = -1,

) : Serializable
