package com.example.notesapp.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.Button
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.notesapp.R
import com.example.notesapp.adapters.imageAdapter
import kotlin.math.abs


class A : Fragment() {

    private lateinit var ViewPager2: ViewPager2
    private lateinit var handler: Handler
    private lateinit var imageList: ArrayList<Int>
    private lateinit var adapter: imageAdapter
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View =  inflater.inflate(R.layout.fragment_a, container, false)

        ViewPager2 = view.findViewById(R.id.viewPager2)

        init()
        setUpTransformer()
        ViewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable,2000)
            }
        })

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

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable,2000)
    }
    private val runnable = Runnable {
        ViewPager2.currentItem = ViewPager2.currentItem + 1
    }
    private fun setUpTransformer() {
         val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.14f
        }
        ViewPager2.setPageTransformer(transformer)
    }

    private fun init() {
        handler = Handler(Looper.myLooper()!!)
        imageList = ArrayList()

        imageList.add(R.drawable.notes1)
        imageList.add(R.drawable.notes2)
        imageList.add(R.drawable.notes3)
        imageList.add(R.drawable.notes4)
        imageList.add(R.drawable.notes5)
        imageList.add(R.drawable.notes8)
        imageList.add(R.drawable.notes7)
        imageList.add(R.drawable.notes9)
        imageList.add(R.drawable.notes8)
        imageList.add(R.drawable.notes10)

        adapter = imageAdapter(imageList, ViewPager2)

        ViewPager2.adapter  = adapter
        ViewPager2.offscreenPageLimit = 3
        ViewPager2.clipToPadding = false
        ViewPager2.clipChildren = false
        ViewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    }
}