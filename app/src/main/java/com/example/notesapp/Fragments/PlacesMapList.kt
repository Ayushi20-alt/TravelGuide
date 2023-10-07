package com.example.notesapp.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.activities.CreateMapActivity
import com.example.notesapp.activities.DisplayMapsActivity
import com.example.notesapp.adapters.MapsAdapter
import com.example.notesapp.model.Place
import com.example.notesapp.model.UserMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

const val TAG_TITLE = "EXTRA_MAP_TITLE"
private const val FILENAME = "UserMaps.data"
// replacement of extra_user_map
const val TAG = "MAP"
private const val REQUEST_CODE = 1234
class PlacesMapList : Fragment() {

    private lateinit var userMaps : MutableList<UserMap>
    private lateinit var mapsAdapter: MapsAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_places_map_list, container, false)

        val rvMaps = view.findViewById<RecyclerView>(R.id.places_recyler)

//        val userMapsFromFile = context?.let { deserializeUserMaps(it) }
//        userMaps = generateSampleData().toMutableList()
//        if (userMapsFromFile != null) {
//            userMaps.addAll(userMapsFromFile)
//        }
        userMaps = activity?.let { deserializeUserMaps(it).toMutableList() }!!
        // set layout manger on the recyler view
        rvMaps.layoutManager = LinearLayoutManager(activity)
        // set adapter on recycler view
        mapsAdapter = activity?.let {
            MapsAdapter(it, userMaps, object : MapsAdapter.OnClickListner{
                override fun onItemclicked(position: Int) {
                   // Toast.makeText(activity,"OnItemClicked $position",Toast.LENGTH_LONG).show()
                    val intent = Intent(activity, DisplayMapsActivity::class.java)
                    intent.putExtra(TAG, userMaps[position])
                    startActivity(intent)
                }

            })
        }!!

        rvMaps.adapter = mapsAdapter
        // When user taps on view in Rv, navigate to the new activity
        val fabCreate = view.findViewById<FloatingActionButton>(R.id.fabCreateSearch)
        fabCreate.setOnClickListener {
          showAlertDialogBox()
        }
        return view
    }

    private fun showAlertDialogBox() {
        val mapFormView = LayoutInflater.from(activity).inflate(R.layout.dialog_create_map, null)
        val dialog = activity?.let {
            AlertDialog.Builder(it).setTitle("Map Title").setView(mapFormView)
                .setNegativeButton("Cancel", null).setPositiveButton("Ok", null).show()
        }

        if (dialog != null) {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                // working on data validation
                // getting the ids
                val title = mapFormView.findViewById<EditText>(R.id.etTitle).text.toString()
                if(title.isEmpty())
                {
                    Toast.makeText(activity,"Please Fill all the Fields!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                // Navigate to create map Activity
                val intent = Intent(activity, CreateMapActivity::class.java)
                intent.putExtra(TAG_TITLE, title)
                startActivityForResult(intent, REQUEST_CODE)

                if (dialog != null) {
                    dialog.dismiss()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
      //  Toast.makeText(activity,"invoked",Toast.LENGTH_LONG).show()
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            // GET new map data from Data
            val userMap = data?.getSerializableExtra(TAG) as UserMap
           // Toast.makeText(activity, "onActivityResult with new Map Title ${userMap.title}", Toast.LENGTH_LONG).show()
            userMaps.add(userMap)
            mapsAdapter.notifyItemInserted(userMaps.size-1)
            activity?.let { serializeUserMaps(it, userMaps) }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun serializeUserMaps(context: Context, userMaps: List<UserMap>){
     //   Toast.makeText(activity,"seraializeUserMaps", Toast.LENGTH_LONG).show()
        ObjectOutputStream(FileOutputStream(getDataFile(context))).use { it.writeObject(userMaps)

        }
    }

    private fun deserializeUserMaps(context: Context) : List<UserMap>{
      //  Toast.makeText(activity,"deseraializeUserMaps", Toast.LENGTH_LONG).show()
        val datafile = getDataFile(context)
        if(!datafile.exists())
        {
            Toast.makeText(activity,"data file does not exist yet", Toast.LENGTH_LONG).show()
            return emptyList()
        }
        ObjectInputStream(FileInputStream(datafile)).use {
            return it.readObject() as List<UserMap>
        }
    }

    private fun getDataFile(context : Context): File {
        Toast.makeText(activity,"Getting files from Directory ${context.filesDir}", Toast.LENGTH_LONG).show()
        return File(context.filesDir, FILENAME)
    }

    private fun generateSampleData(): List<UserMap> {
        return listOf(
            UserMap(
                "Memories from University",
                listOf(
                    Place("Branner Hall", "Best dorm at Stanford", 37.426, -122.163),
                    Place("Gates CS building", "Many long nights in this basement", 37.430, -122.173),
                    Place("Pinkberry", "First date with my wife", 37.444, -122.170)
                )
            ),
            UserMap("January vacation planning!",
                listOf(
                    Place("Tokyo", "Overnight layover", 35.67, 139.65),
                    Place("Ranchi", "Family visit + wedding!", 23.34, 85.31),
                    Place("Singapore", "Inspired by \"Crazy Rich Asians\"", 1.35, 103.82)
                )),
            UserMap("Singapore travel itinerary",
                listOf(
                    Place("Gardens by the Bay", "Amazing urban nature park", 1.282, 103.864),
                    Place("Jurong Bird Park", "Family-friendly park with many varieties of birds", 1.319, 103.706),
                    Place("Sentosa", "Island resort with panoramic views", 1.249, 103.830),
                    Place("Botanic Gardens", "One of the world's greatest tropical gardens", 1.3138, 103.8159)
                )
            ),
            UserMap("My favorite places in the Midwest",
                listOf(
                    Place("Chicago", "Urban center of the midwest, the \"Windy City\"", 41.878, -87.630),
                    Place("Rochester, Michigan", "The best of Detroit suburbia", 42.681, -83.134),
                    Place("Mackinaw City", "The entrance into the Upper Peninsula", 45.777, -84.727),
                    Place("Michigan State University", "Home to the Spartans", 42.701, -84.482),
                    Place("University of Michigan", "Home to the Wolverines", 42.278, -83.738)
                )
            ),
            UserMap("Restaurants to try",
                listOf(
                    Place("Champ's Diner", "Retro diner in Brooklyn", 40.709, -73.941),
                    Place("Althea", "Chicago upscale dining with an amazing view", 41.895, -87.625),
                    Place("Shizen", "Elegant sushi in San Francisco", 37.768, -122.422),
                    Place("Citizen Eatery", "Bright cafe in Austin with a pink rabbit", 30.322, -97.739),
                    Place("Kati Thai", "Authentic Portland Thai food, served with love", 45.505, -122.635)
                )
            )
        )
    }

}