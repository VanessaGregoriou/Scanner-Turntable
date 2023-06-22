package com.android.app.itemscanner.fragment

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.app.itemscanner.*
import com.android.app.itemscanner.api.ScanSession
import com.android.app.itemscanner.databinding.FragmentSessionRecordBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.zip.Adler32
import java.util.zip.CheckedOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.properties.Delegates

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SessionRecordFragment : Fragment() {
    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val DIRECTORY_PATH = "/storage/emulated/0/Pictures"
    }

    private var _binding: FragmentSessionRecordBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var databaseController: DatabaseController
    private lateinit var usbSerialController: UsbSerialController

    private lateinit var title: String
    private var numPhotos by Delegates.notNull<Int>()
    private lateinit var image: Bitmap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSessionRecordBinding.inflate(inflater, container, false)

        val context = requireContext()
        databaseController = DatabaseController(context)
        usbSerialController = UsbSerialController(requireActivity())
        usbSerialController.openDevice()

        val args = navArgs<SessionRecordFragmentArgs>().value
        var sessionName = args.sessionName
        var index = 0
        while (File(titleOutputFile(context, sessionName)).exists()) {
            sessionName = "${args.sessionName}_(${++index})"
        }
        title = sessionName
        numPhotos = args.numPhotos

        startCamera()

        // Set up the listeners for take photo and video capture buttons
        binding.sessionStartButton.setOnClickListener { startRecording() }

        cameraExecutor = Executors.newSingleThreadExecutor()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
        usbSerialController.closeDevice()
    }

    private fun titleOutputFile(context: Context, title: String) =
        "${context.filesDir}/${title}.zip"

    private fun imagePath(title: String) =
        "$DIRECTORY_PATH/${title}"

    private fun startRecording() {
        val context = context ?: return

        binding.sessionStartButton.visibility = View.GONE
        binding.photosCarousel.visibility = View.VISIBLE

        val progressText =
            getString(R.string.images_progress_text, 0, numPhotos)
        binding.progressText.text = progressText
        binding.progressText.invalidate()

        val zipFile = File(titleOutputFile(context, title))
        val fileOutputStream = FileOutputStream(zipFile)
        val checksum = CheckedOutputStream(fileOutputStream, Adler32())
        val zipOutputStream = ZipOutputStream(checksum)

        takePhoto(0, zipOutputStream) {
            zipOutputStream.finish()
            zipOutputStream.close()
            checksum.close()
            fileOutputStream.close()

            Log.i(TAG, "Scanning complete: ${zipFile.absolutePath}")
            databaseController.insert(
                ScanSession(
                    title = title,
                    numPhotos = numPhotos,
                    image = image,
                    zipFile = zipFile.toUri()
                )
            )

            findNavController().navigate(
                SessionRecordFragmentDirections.actionSessionRecordFragmentToScannedListFragment()
            )
        }
    }

    private fun takePhoto(
        index: Int,
        zipOutputStream: ZipOutputStream,
        finishSession: Runnable
    ) {
        val context = context ?: return
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${title}")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        // Set up image capture listener, which is triggered after photo has been taken
        // /storage/emulated/0/Pictures/scan_****/****.jpg
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    val msg = "Photo capture failed: ${exc.message}"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    Log.e(TAG, msg, exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val progressText =
                        getString(R.string.images_progress_text, index + 1, numPhotos)
                    binding.progressText.text = progressText
                    binding.progressText.invalidate()
                    val msg = "Photo capture succeeded: $progressText"
                    Log.d(TAG, msg)

                    val imgFilePath = "$title/$name.jpg"
                    zipPhoto(imgFilePath, zipOutputStream)

                    val imageFile = File(imagePath(imgFilePath))

                    val imageView = ImageView(context)
                    imageView.setImageURI(imageFile.toUri())
                    imageView.layoutParams =
                        ViewGroup.LayoutParams(
                            binding.photosCarousel.height,
                            binding.photosCarousel.height
                        )
                    binding.previewImages.addView(imageView)

                    binding.horizontalScrollView.postDelayed(Runnable {
                        binding.horizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
                    }, 100L)

                    if (index == 0) {
                        val imageSource = BitmapFactory.decodeFile(imagePath(imgFilePath))
                        image = imageSource
                    }
                    if (index + 1 < numPhotos) {
                        triggerTurntable { takePhoto(index + 1, zipOutputStream, finishSession) }
                    } else {
                        Toast.makeText(context, "Scanning complete.", Toast.LENGTH_SHORT).show()
                        File(imagePath(title)).deleteRecursively()
                        finishSession.run()
                    }
                }
            }
        )
    }

    private fun triggerTurntable(onResponse: Runnable) {
        usbSerialController.writeWithResponse(
            getString(R.string.trigger_turntable),
            getString(R.string.turntable_response),
            onResponse
        )
    }

    private fun zipPhoto(imageFilePath: String, zipOutputStream: ZipOutputStream) {
        val imageFile = File(imagePath(imageFilePath))
        val fin = FileInputStream(imageFile)
        val zipEntry = ZipEntry(imageFilePath)
        zipOutputStream.putNextEntry(zipEntry)
        var length: Int
        val buffer = ByteArray(1024)
        while (fin.read(buffer).also { length = it } > 0) {
            zipOutputStream.write(buffer, 0, length)
        }
        fin.close()
        zipOutputStream.closeEntry()
    }

    private fun startCamera() {
        val context = context ?: return
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        imageCapture = ImageCapture.Builder().build()

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }
}