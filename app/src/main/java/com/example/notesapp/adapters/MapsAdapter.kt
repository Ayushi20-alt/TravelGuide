package com.example.notesapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.model.UserMap

private const val TAG = "MapsAdapter"
class MapsAdapter(val context: Context, val userMaps: List<UserMap>, val onClickListener: OnClickListner) : RecyclerView.Adapter<MapsAdapter.ViewHolder>() {

    interface OnClickListner{
        fun onItemclicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_user_map, parent, false))
    }

    override fun getItemCount() = userMaps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userMap = userMaps[position]
        holder.itemView.setOnClickListener {
          //  Toast.makeText(context,"Tapped on $position", Toast.LENGTH_LONG).show()
            onClickListener.onItemclicked(position)
        }
        val textViewtitle = holder.itemView.findViewById<TextView>(R.id.tvMapTitle)
        textViewtitle.text = userMap.title
    }

    class ViewHolder(itemView : View):RecyclerView.ViewHolder(itemView)
}
