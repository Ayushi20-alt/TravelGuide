package com.example.notesapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.notesapp.Fragments.A
import com.example.notesapp.Fragments.PlacesMapList
import com.example.notesapp.Fragments.b
import com.example.notesapp.Fragments.notesFragment
import com.example.notesapp.R
import com.example.notesapp.databinding.ActivityMainBinding
import com.example.notesapp.db.NoteDatabse
import com.example.notesapp.repository.noteRepository
import com.example.notesapp.viewModel.NoteActivityViewModel
import com.example.notesapp.viewModel.noteActivityViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    lateinit var  noteActivityViewModel: NoteActivityViewModel
    private lateinit var binding:ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)

        // now create the database instance inside the try catch block
        try{
            // database instance is created sucessfully
            setContentView(binding.root)
            val noteRepository = noteRepository(NoteDatabse(this))
            val noteActivityViewModelFactory = noteActivityViewModelFactory(noteRepository)
            noteActivityViewModel = ViewModelProvider(this,noteActivityViewModelFactory)[NoteActivityViewModel::class.java]
        }catch (e : Exception)
        {
            Toast.makeText(this,"Error:- $e", Toast.LENGTH_LONG).show()
            Log.d("ayushi", "$e")
        }

//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
//        bottomNavigationView.background = null
//        bottomNavigationView.menu.getItem(2).isEnabled = false
//        loadFragment(PlacesMapList())
//        bottomNavigationView.setOnItemSelectedListener {
//            when(it.itemId) {
//                R.id.notesFragment -> {
//
//                }
//
//                R.id.placesMapList -> {
//                    loadFragment(PlacesMapList())
//
//                }
//                R.id.a->{
//
//
//                }
//                R.id.b->{
//
//
//                }
//                else -> {
//
//                }
//            }
//            true
//        }

    }

    private fun loadFragment(fragment: Fragment)
    {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.parent_layout, fragment)
        transaction.commit()
    }
}