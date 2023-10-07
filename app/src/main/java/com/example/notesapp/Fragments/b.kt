package com.example.notesapp.Fragments

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.video.VideoRecordEvent.Finalize
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.notesapp.R
import com.example.notesapp.databinding.FragmentBBinding
import com.example.notesapp.utilis.constants
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class b : Fragment() {


    private lateinit var binding : FragmentBBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory : File
    private lateinit var cameraSelector : CameraSelector
    private var isFrontCameraSelected = false
    private var isTorchOn = false

    // video
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService

    @OptIn(ExperimentalCamera2Interop::class) override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBBinding.inflate(inflater, container, false)
      //  outputDirectory = getOutputDirectory()

        if(allPermissionsGarnted())
        {
            startCamera()
        }
        else{
            activity?.let { ActivityCompat.requestPermissions(it, constants.REQUIRED_PERMISSIONS,constants.REQUEST_CODE_PERMISSIONS) }
        }

        binding.btnTakePhoto.setOnClickListener {
            takePhoto()
        }
        binding.rotate.setOnClickListener{
            togglecamera()
        }

        binding.captureVideo.setOnClickListener {
            captureVideo()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()

        return binding.getRoot();
    }

    private fun captureVideo() {
        binding.captureVideo.text = R.string.start_capture.toString()

        val currentRecording = recording
        if (currentRecording != null) {
            // A recording is already in progress, stop it
            currentRecording.stop()
            recording = null
            binding.captureVideo.text = R.string.start_capture.toString()
            return
        }

        val name: String = SimpleDateFormat(
            "yyy-MM-dd-HH-mm-ss-SSS",
            Locale.getDefault()
        ).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/CameraX-Recorder")
        }

        val options = MediaStoreOutputOptions.Builder(requireContext().contentResolver, EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture?.output?.prepareRecording(requireContext(), options)
            ?.start(ContextCompat.getMainExecutor(requireContext())) { videoRecordEvent ->
                // Handle different recording events
                when (videoRecordEvent) {
                    is VideoRecordEvent.Start -> {
                        // Handle the start of a new active recording
                        binding.captureVideo.text = R.string.stop_capture.toString()
                    }
                    is VideoRecordEvent.Pause -> {
                        // Handle the case where the active recording is paused
                    }
                    is VideoRecordEvent.Resume -> {
                        // Handle the case where the active recording is resumed
                    }
                    is Finalize -> {
                        val finalizeEvent = videoRecordEvent
                        // Handle the finalize event for the active recording, checking Finalize.getError()
                        val error = finalizeEvent.error
                        if (error != Finalize.ERROR_NONE) {
                            // Handle error during recording finalization
                            Toast.makeText(
                                requireContext(),
                                "There was an error during video finalization: ${videoRecordEvent.error}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "The video has been successfully recorded",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        binding.captureVideo.text = R.string.stop_capture.toString()
                    }
                }
                // All events, including VideoRecordEvent.Status, contain RecordingStats.
                // This can be used to update the UI or track the recording duration.
                val recordingStats = videoRecordEvent.recordingStats
            }
    }

//    private fun captureVideo() {
//        binding.captureVideo.text = R.string.start_capture.toString()
//        val recording1 = recording
//        if (recording1 != null) {
//            recording1.stop()
//            recording = null
//            return
//        }
//        val name = SimpleDateFormat(
//            "yyyy-MM-dd-HH-mm-ss-SSS",
//            Locale.getDefault()
//        ).format(System.currentTimeMillis())
//        val contentValues = ContentValues()
//        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
//        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
//
//        val options = MediaStoreOutputOptions.Builder(
//            requireActivity().contentResolver,
//            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//        )
//            .setContentValues(contentValues).build()
//
//        if (activity?.let {
//                ActivityCompat.checkSelfPermission(
//                    it,
//                    android.Manifest.permission.RECORD_AUDIO
//                )
//            } != PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }
//        recording =
//            videoCapture!!.output.prepareRecording(requireActivity(), options).withAudioEnabled()
//                .start(
//                    ContextCompat.getMainExecutor(requireActivity())
//                ) { videoRecordEvent: VideoRecordEvent? ->
//                    if (videoRecordEvent is VideoRecordEvent.Start) {
//                        binding.captureVideo.setEnabled(true)
//                    } else if (videoRecordEvent is Finalize) {
//                        if (!videoRecordEvent.hasError()) {
//                            val msg =
//                                "Video capture succeeded: " + videoRecordEvent.outputResults
//                                    .outputUri
//                            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
//                        } else {
////                            recording!!.close()
//                            recording = null
//                            val msg =
//                                "Error: " + videoRecordEvent.error
//                            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
//                        }
//                        binding.captureVideo.text = R.string.start_capture.toString()
//                    }
//                }
//    }

//    private fun captureVideo() {
//        binding.captureVideo.isEnabled = false
//
//        val currecording = recording
//        if (currecording != null) {
//            currecording.stop()
//            recording = null
//            return
//        }
//        // create and start a new recording session
//        val name = SimpleDateFormat(constants.FILE_NAME_FORMAT, Locale.US)
//            .format(System.currentTimeMillis())
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
//            }
//        }
//
//        val mediaStoreOutputOptions = MediaStoreOutputOptions
//                .Builder(requireActivity().contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
//            .setContentValues(contentValues)
//            .build()
//
//        recording = videoCapture!!.output
//            .prepareRecording(requireContext(), mediaStoreOutputOptions)
//            .apply {
//                // Enable Audio for recording
//                if (
//                    PermissionChecker.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) ==
//                    PermissionChecker.PERMISSION_GRANTED
//                ) {
//                    withAudioEnabled()
//                }
//            }
//            .start(ContextCompat.getMainExecutor(requireContext())) { recordEvent ->
//                when(recordEvent) {
//                    is VideoRecordEvent.Start -> {
//                        binding.captureVideo.apply {
//                            text = getString(R.string.stop_capture)
//                            isEnabled = true
//                        }
//                    }
//                    is VideoRecordEvent.Finalize -> {
//                        if (!recordEvent.hasError()) {
//                            val msg =
//                                "video capture succeeded: " + "${recordEvent.outputResults.outputUri}"
//                            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
//                            Log.d(tag, msg)
//                        } else {
//                            recording?.close()
//                            recording = null
//                            Toast.makeText(activity, "video capture ends with error: " + "${recordEvent.error}", Toast.LENGTH_LONG).show()
//                        }
//                        binding.captureVideo.apply {
//                            text = getString(R.string.stop_capture)
//                            isEnabled = true
//                        }
//                    }
//                }
//            }
//    }

    private fun togglecamera() {
        val newCameraSelector = if (isFrontCameraSelected) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        val cameraProviderFuture = activity?.let { ProcessCameraProvider.getInstance(it) }
        val preview = Preview.Builder().build().also { mPreview->
            mPreview.setSurfaceProvider(
                binding.viewFinder.surfaceProvider
            )
        }

        if (cameraProviderFuture != null) {
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                try {
                    // Unbind all previous use cases
                    cameraProvider.unbindAll()

                    // Bind the camera to the lifecycle with the new camera selector
                    cameraProvider.bindToLifecycle(
                        this, // LifecycleOwner
                        newCameraSelector,
                        preview, // Attach the preview use case
                        imageCapture // Attach other use cases like ImageCapture, if needed
                    )

                    // Update the current camera selection
                    isFrontCameraSelected = !isFrontCameraSelected

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, activity?.let { ContextCompat.getMainExecutor(it) })
        }
    }


    private fun getOutputDirectory(): File? {
        val mediaDir = activity?.externalMediaDirs?.firstOrNull()?.let { mfile->
            File(mfile,resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else activity?.filesDir
    }

    private fun takePhoto() {
        val imageCapture = imageCapture?: return
        val photoFile = File(
            getOutputDirectory(),
            SimpleDateFormat(constants.FILE_NAME_FORMAT, Locale.getDefault()).format(System.currentTimeMillis())+ ".jpg")

        val outputOption  = ImageCapture
            .OutputFileOptions
            .Builder(photoFile)
            .build()

        activity?.let { ContextCompat.getMainExecutor(it) }?.let {
            imageCapture.takePicture(
                outputOption, it,
                object :ImageCapture.OnImageSavedCallback{
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val savedUri = Uri.fromFile(photoFile)
                        val msg = "Photo Saved"
                        Toast.makeText(activity, "$msg $savedUri", Toast.LENGTH_LONG).show()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e(constants.TAG, "onError: ${exception.message}", exception)
                    }

                }
            )
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if(requestCode == constants.REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGarnted())
            {
                // our code
                startCamera()
            }
            else{
//            Toast.makeText(this,"permission not granted", Toast.LENGTH_LONG).show()
                Snackbar.make(binding.root,"The camera permission is required", Snackbar.LENGTH_INDEFINITE).show()

            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = activity?.let { ProcessCameraProvider.getInstance(it) }
        if (cameraProviderFuture != null) {
            cameraProviderFuture.addListener({
                val cameraProvider : ProcessCameraProvider =  cameraProviderFuture.get()
                val preview = Preview.Builder().build().also { mPreview->
                    mPreview.setSurfaceProvider(
                        binding.viewFinder.surfaceProvider
                    )
                }
                imageCapture = ImageCapture.Builder().build()

                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                val autoFocusPoint = SurfaceOrientedMeteringPointFactory(1f, 1f)
                    .createPoint(.5f, .5f)

                // video
                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(
                            Quality.HIGHEST,
                            FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                        )
                    )
                    .build()
                videoCapture = VideoCapture.withOutput(recorder)

                if (cameraProvider != null) {
                    cameraProvider.unbindAll()

                    // bind use cases to camera
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        this,cameraSelector,preview,imageCapture ).let { camera ->
                        val cameraControl = camera.cameraControl
                        // For example, to set the focus distance to 1 foot (30.48 cm):
                        val focusDistance = 30.48f // in centimeters
                        cameraControl.setLinearZoom(0.0f) // Reset zoom
                        cameraControl.setZoomRatio(focusDistance)
                        val autoFocusAction = FocusMeteringAction.Builder(
                            autoFocusPoint,
                            FocusMeteringAction.FLAG_AF
                        ).apply {
                            //start auto-focusing after 2 seconds
                            setAutoCancelDuration(2, TimeUnit.SECONDS)
                        }.build()
                        camera.cameraControl.startFocusAndMetering(autoFocusAction)
                        binding.enabletorch.setOnClickListener {
                            isTorchOn = !isTorchOn
                            cameraControl.enableTorch(isTorchOn)
                        }
                    }


                    // Set the focus distance to 1 foot (30.48 cm).
                }catch (e : Exception)
                {
                    Log.d(constants.TAG, "startCamera Fail : ",e)
                }
            }, activity?.let { ContextCompat.getMainExecutor(it) })
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun allPermissionsGarnted() =
        constants.REQUIRED_PERMISSIONS.all {
            activity?.let { it1 ->
                ContextCompat.checkSelfPermission(
                    it1, it
                )
            } == PackageManager.PERMISSION_GRANTED
        }
}