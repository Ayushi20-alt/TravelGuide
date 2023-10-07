package com.example.notesapp.model

import java.io.Serializable

data class UserMap(val title: String, val place : List<Place>) : Serializable
