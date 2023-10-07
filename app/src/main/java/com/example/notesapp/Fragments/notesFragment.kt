package com.example.notesapp.Fragments

import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notesapp.R
import com.example.notesapp.activities.MainActivity
import com.example.notesapp.adapters.RvnotesAdapter
import com.example.notesapp.databinding.FragmentNotesBinding
import com.example.notesapp.utilis.SwipeToDelete
import com.example.notesapp.utilis.hideKeyboard
import com.example.notesapp.viewModel.NoteActivityViewModel
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class notesFragment : Fragment(R.layout.fragment_notes) {

    private lateinit var notesBinding: FragmentNotesBinding
    private val noteActivityViewModel: NoteActivityViewModel by activityViewModels()
    private lateinit var rvAdapter: RvnotesAdapter

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        // in this we will mention the transition
        // whenver user open or closing the fragment
        exitTransition = MaterialElevationScale(false).apply {
            duration = 350
        }
        enterTransition = MaterialElevationScale(true).apply {
            duration = 350
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notesBinding = FragmentNotesBinding.bind(view)
        // we got the context for the main activity
        val activity = activity as MainActivity
      //  val navController = Navigation.findNavController(view)
        requireView().hideKeyboard()

        // we using the couroutine scope so we do not need to hide the title and status bar
        // we can only create other thread for that
        CoroutineScope(Dispatchers.Main).launch {
            delay(10)
            // activity.window.statusBarColor = Color.WHITE
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.parseColor("#9E9D9D")
        }

        // now sending the user from the notefrag to the save or delete frag
        notesBinding.addNotesFav.setOnClickListener {
            notesBinding.appBarLayout.visibility = View.INVISIBLE
           findNavController().navigate(notesFragmentDirections.actionNotesFragmentToSaveorDeleteFragment())
        }

        notesBinding.innerFab.setOnClickListener {
            notesBinding.appBarLayout.visibility = View.INVISIBLE
           findNavController().navigate(notesFragmentDirections.actionNotesFragmentToSaveorDeleteFragment())
        }

        recyclerViewDisplay()
        swipeToDelete(notesBinding.rvnote)

        notesBinding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notesBinding.noData.isVisible = false
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (s.toString().isNotEmpty()) {
                    val text = s.toString()
                    val query = "%$text%"
                    if (query.isNotEmpty()) {
                        // we have to observe cause it is a live data
                        noteActivityViewModel.searchNote(query).observe(viewLifecycleOwner)
                        {
                            // here we have to pass the list in our recycler view
                            rvAdapter.submitList(it)
                        }
                    } else {
                        observerDataChanges()
                    }
                } else {
                    observerDataChanges()
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        notesBinding.search.setOnEditorActionListener{v, actionId, _ ->
            if(actionId==EditorInfo.IME_ACTION_SEARCH)
            {
                v.clearFocus()
                requireView().hideKeyboard()
            }
            return@setOnEditorActionListener true
        }


        // we have to hide the title and status bar when the user will scroll
        notesBinding.rvnote.setOnScrollChangeListener { _, scrollX, scrollY, _, oldScrollY ->
            when {
                scrollY > oldScrollY -> {
                    notesBinding.chatFabText.isVisible = false
                }

                scrollX == scrollY -> {
                    notesBinding.chatFabText.isVisible = true
                }

                else -> {
                    notesBinding.chatFabText.isVisible = true
                }
            }
        }

    }

    // Android callbacks allow your method to fetch results from another method synchronously.
    private fun swipeToDelete(rvnote: RecyclerView) {
         val  swipeToDeleteCallback = object : SwipeToDelete()
         {
             override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                 val position = viewHolder.absoluteAdapterPosition
                 val note = rvAdapter.currentList[position]
                 var actionBtnTrapped = false
                 noteActivityViewModel.deleteNote(note)
                 // suppose user search something and now he wants to delete it
                 notesBinding.search.apply {
                     hideKeyboard()
                     clearFocus()
                 }
                 if(notesBinding.search.text.toString().isEmpty())
                 {
                     observerDataChanges()
                 }
                 val snackBar = Snackbar.make(
                     requireView(),
                     "Note Deleted",
                     Snackbar.LENGTH_LONG
                 ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>(){
                     // it is called if we want to undo the operation
                     override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                         super.onDismissed(transientBottomBar, event)
                     }

                     override fun onShown(transientBottomBar: Snackbar?) {
                         transientBottomBar?.setAction("UNDO"){
                             noteActivityViewModel.saveNote(note)
                             actionBtnTrapped = true
                             notesBinding.noData.isVisible = false
                         }
                         super.onShown(transientBottomBar)
                     }
                 }).apply {
                     animationMode = Snackbar.ANIMATION_MODE_FADE
                     setAnchorView(R.id.add_notes_fav)
                 }

                 snackBar.setActionTextColor(
                     ContextCompat.getColor(
                         requireContext(),
                         R.color.yellowOrange
                     )
                 )
                 snackBar.show()
             }
         }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(rvnote)
    }

    private fun observerDataChanges() {
        noteActivityViewModel.getAllNotes().observe(viewLifecycleOwner)
        {list->
            notesBinding.noData.isVisible = list.isEmpty()
            rvAdapter.submitList(list)
        }
    }

    private fun recyclerViewDisplay() {
     // here we want to make our app funny responsive
        when(resources.configuration.orientation)
        {
            Configuration.ORIENTATION_LANDSCAPE-> setUpRecyclerView(3)
            Configuration.ORIENTATION_PORTRAIT->setUpRecyclerView(2)
        }
    }

    private fun setUpRecyclerView(spanCount: Int) {
       // requireArguments().getString("nameyaay").toString()
          // we will create list adapter not recycler adapter because whenever we will update the item we dont have to called data changes again
        // in case of list adapter

        notesBinding.rvnote.apply {
            layoutManager = StaggeredGridLayoutManager(spanCount,StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)

            rvAdapter = RvnotesAdapter()
            // Defines how this Adapter wants to restore its state after a view reconstruction (e.g. configuration change).
            //PREVENT_WHEN_EMPTY -> Adapter is ready to restore State when it has more than 0 items.
            rvAdapter.stateRestorationPolicy= RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            adapter = rvAdapter
           // postponeEnterTransition(300L,TimeUnit.MILLISECONDS)
            // whenever our view is going to set
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
        observerDataChanges()
    }
}