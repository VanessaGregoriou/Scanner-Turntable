package com.android.app.itemscanner

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.android.app.itemscanner.databinding.SessionRecordFragmentBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SessionRecordFragment : Fragment() {

    private var _binding: SessionRecordFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = SessionRecordFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sessionStartButton.setOnClickListener { startCamera() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun takePhoto() {}

    private fun startCamera() {
        context?.let { validContext ->

            val cameraProviderFuture = ProcessCameraProvider.getInstance(validContext)

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
                    cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview
                    )

                } catch (exc: Exception) {
                    Log.e(SessionActivity.TAG, "Use case binding failed", exc)
                }

            }, ContextCompat.getMainExecutor(validContext))
        }
    }

}