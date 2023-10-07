package com.example.notesapp.Fragments



import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.notesapp.R
import com.example.notesapp.activities.MainActivity
import com.example.notesapp.databinding.BottomSheetLayoutBinding
import com.example.notesapp.databinding.FragmentSaveorDeleteBinding
import com.example.notesapp.model.Note
import com.example.notesapp.utilis.hideKeyboard
import com.example.notesapp.viewModel.NoteActivityViewModel
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class saveorDeleteFragment : Fragment(R.layout.fragment_saveor_delete), TextToSpeech.OnInitListener {

    // for animations
    private val rotateOpen : Animation by lazy { AnimationUtils.loadAnimation(activity, R.anim.rotate_open) }
    private val rotateClose : Animation by lazy { AnimationUtils.loadAnimation(activity,R.anim.rotate_close) }
    private val fromBottom : Animation by lazy { AnimationUtils.loadAnimation(activity,R.anim.from_bottom) }
    private val toBottom : Animation by lazy { AnimationUtils.loadAnimation(activity,R.anim.to_bottom) }
    private var clicked = false

    private lateinit var navController: NavController
    private val args: saveorDeleteFragmentArgs by navArgs()
    private lateinit var contentBinding: FragmentSaveorDeleteBinding
    private var note: Note? = null
    private var color = -1
    private lateinit var result: String
    private val noteActivityViewModel: NoteActivityViewModel by activityViewModels()
    private val currentDate = SimpleDateFormat.getInstance().format(Date())
    private val job = CoroutineScope(Dispatchers.Main)
    private var tts: TextToSpeech? = null
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val animation = MaterialContainerTransform().apply {
//            drawingViewId = R.id.parent_layout
//            scrimColor = Color.TRANSPARENT
//            duration = 300L
//        }
//        sharedElementEnterTransition = animation
//        sharedElementReturnTransition = animation
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding = FragmentSaveorDeleteBinding.bind(view)

        navController = Navigation.findNavController(view)
        val activity = activity as MainActivity
        

        ViewCompat.setTransitionName(
            contentBinding.noteContentFragmentParent,
            "recylerView_${args.note?.id}"
        )

        contentBinding.backbtn.setOnClickListener {
            requireView().hideKeyboard()
            navController.popBackStack()
        }

        contentBinding.saveNote.setOnClickListener {
            savenote()
        }

        contentBinding.convertor.setOnClickListener {
            onAddButtonClicked()
        }

        // text to speech convertor
        tts = TextToSpeech(activity, this)
        contentBinding.textToSpeechBtn.isEnabled = false
        contentBinding.textToSpeechBtn.setOnClickListener {
            Speak()
        }

        // speech to text convertor
        contentBinding.speechToTextBtn.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something....")

            // whether the hardware is apt or not
            try {
                activityResultLauncher.launch(intent)
            } catch (exp: ActivityNotFoundException) {
                Toast.makeText(activity, "Device does not Supported", Toast.LENGTH_LONG).show()
            }
        }
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    val speechText =
                        result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<Editable>
                    contentBinding.etNoteContent.text = speechText[0]
                }
            }


        // for setting focus on edit text
        try {
            contentBinding.etNoteContent.setOnFocusChangeListener { _, hasFocus ->
                // agr focus hai mtlb ki user ne click kiya likhne ko to bottom bar show ho aur style bar bhi
                if (hasFocus) {
                    contentBinding.bottomBar.visibility = View.VISIBLE
                    contentBinding.etNoteContent.setStylesBar(contentBinding.styleBar)
                } else {
                    contentBinding.bottomBar.visibility = View.GONE
                }
            }
        } catch (e: Throwable) {
            Log.d("TAG", e.stackTrace.toString())
        }

        // if user pick any colour we haveto change the background colour of our application
        contentBinding.fabColorPicker.setOnClickListener {
            val bottomSheetDialog =
                BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
            val bottomSheetView: View = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
            with(bottomSheetDialog)
            {
                setContentView(bottomSheetView)
                show()
            }
            val bottomSheetBindng = BottomSheetLayoutBinding.bind(bottomSheetView)
            bottomSheetBindng.apply {
                colorPicker.apply {
                    setSelectedColor(color)
                    setOnColorSelectedListener { value ->
                        color = value
                        // we using apply so that we dont have to write on click listner again
                        contentBinding.apply {
                            noteContentFragmentParent.setBackgroundColor(color)
                            toolbarFragmentNoteContent.setBackgroundColor(color)
                            bottomBar.setBackgroundColor(color)
                            activity.window.statusBarColor = color
                        }
                        bottomSheetBindng.bottomSheetParent.setCardBackgroundColor(color)
                    }
                }
                bottomSheetParent.setCardBackgroundColor(color)
            }
            bottomSheetView.post {
                bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        // opens with existing note item
        setUpNote()
    }

    private fun onAddButtonClicked() {
         setVisibility(clicked)
         setAnimation(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked : Boolean) {
         if(!clicked)
         {
             contentBinding.speechToTextBtn.visibility = View.VISIBLE
             contentBinding.textToSpeechBtn.visibility = View.VISIBLE
         }
        else
         {
             contentBinding.speechToTextBtn.visibility = View.INVISIBLE
             contentBinding.textToSpeechBtn.visibility = View.INVISIBLE
         }
    }

    private fun setAnimation(clicked: Boolean) {
        if(!clicked)
        {
            contentBinding.speechToTextBtn.startAnimation(fromBottom)
            contentBinding.textToSpeechBtn.startAnimation(fromBottom)
            contentBinding.convertor.startAnimation(rotateOpen)
        }else{
            contentBinding.speechToTextBtn.startAnimation(toBottom)
            contentBinding.textToSpeechBtn.startAnimation(toBottom)
            contentBinding.convertor.startAnimation(rotateClose)
        }
    }

    private fun Speak(){
        val text = contentBinding.etNoteContent.text.toString()
        tts?.speak(text,TextToSpeech.QUEUE_FLUSH,null, "")
    }

    override fun onDestroy() {
        if(tts!=null)
        {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    private fun setUpNote() {
        val note = args.note
        val title = contentBinding.etTitle
        val content = contentBinding.etNoteContent
        val lastEdited = contentBinding.lastEdited
        if(note == null)
        {
            contentBinding.lastEdited.text = getString(R.string.edited_on, SimpleDateFormat.getDateInstance().format(Date()))
        }
        if(note != null)
        {
            title.setText(note.title)
            content.renderMD(note.content)
            lastEdited.text = getString(R.string.edited_on,note.date)
            color = note.color
            contentBinding.apply {
                job.launch {
                    delay(10)
                    noteContentFragmentParent.setBackgroundColor(color)
                }
                toolbarFragmentNoteContent.setBackgroundColor(color)
                bottomBar.setBackgroundColor(color)
            }
            activity?.window?.statusBarColor = note.color
        }
    }

    private fun savenote() {
        if(contentBinding.etNoteContent.text.toString().isEmpty() || contentBinding.etTitle.text.toString().isEmpty())
        {
            Toast.makeText(activity,"Something is Empty",Toast.LENGTH_LONG).show()
        }
        else
        {
            note = args.note
            when(note)
            {
               null ->{ noteActivityViewModel.saveNote(
                   Note(
                       0,
                       contentBinding.etTitle.text.toString(),
                       contentBinding.etNoteContent.getMD(),
                       currentDate,
                       color
                   )
               )

                   result = "Note Saved"
                   setFragmentResult(
                       "key",
                       bundleOf("bundleKey" to result)
                   )
                navController.navigate(saveorDeleteFragmentDirections.actionSaveorDeleteFragmentToNotesFragment())
               }
               else ->
               {
                   // update the note
                   updateNote()
                   navController.popBackStack()
               }
            }
        }
    }

    private fun updateNote() {
        if(note!= null)
        {
            noteActivityViewModel.updateNote(
                Note(
                    note!!.id,
                    contentBinding.etTitle.text.toString(),
                    contentBinding.etNoteContent.getMD(),
                    currentDate,
                    color
                )
            )
        }
    }

    override fun onInit(p0: Int) {
        if(p0 == TextToSpeech.SUCCESS) {
            var output = tts?.setLanguage(Locale.US)
            if (output == TextToSpeech.LANG_MISSING_DATA || output == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                Toast.makeText(activity,"Lang Not Supported", Toast.LENGTH_LONG).show()
            }
            else{
                contentBinding.textToSpeechBtn.isEnabled = true
            }
        }else{
            Toast.makeText(activity,"Initialization Failed", Toast.LENGTH_LONG).show()
        }
    }

}


