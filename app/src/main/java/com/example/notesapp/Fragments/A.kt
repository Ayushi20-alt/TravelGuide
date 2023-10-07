package com.example.notesapp.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.notesapp.R


class A : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View =  inflater.inflate(R.layout.fragment_a, container, false)

        val button : Button = view.findViewById(R.id.button)
        button.setOnClickListener {
            findNavController().navigate(R.id.action_a2_to_notesFragment)
        }

        val button2 : Button = view.findViewById(R.id.button2)
        button2.setOnClickListener {
            findNavController().navigate(R.id.action_a2_to_placesMapList3)
        }

        val button3 : Button = view.findViewById(R.id.button3)
        button3.setOnClickListener {
            findNavController().navigate(R.id.action_a2_to_b2)
        }
        return view
    }

}