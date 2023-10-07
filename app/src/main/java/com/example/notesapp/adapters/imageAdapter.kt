package com.example.notesapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.notesapp.R

class imageAdapter(private val imageList : ArrayList<Int>, private val viewPager2: ViewPager2): RecyclerView.Adapter<imageAdapter.ImageViewHolder>(){

    class ImageViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
         val imageView : ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.slider_card, parent,false))
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageResource(imageList[position])
        // to make it to the infinite loop
        if(position == imageList.size-1)
        {
            viewPager2.post(runnable)
        }
    }

    private val runnable = Runnable{
        imageList.addAll(imageList)
        notifyDataSetChanged()
    }
}