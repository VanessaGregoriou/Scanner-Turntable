package com.android.app.itemscanner

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.android.app.itemscanner.api.ScanSession
import com.android.app.itemscanner.databinding.CreateSessionDialogBinding
import java.text.SimpleDateFormat
import java.util.*

class SessionCreateDialogFragment : DialogFragment() {

    companion object {
        private const val FILENAME_FORMAT = "ddMMyyyyHHmm"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding: CreateSessionDialogBinding = DataBindingUtil.inflate(
            layoutInflater, R.layout.create_session_dialog, null, false
        )

        binding.numPhotosSlider.value = 180f
        binding.numPhotosSlider.addOnChangeListener { _, value, _ ->
            binding.editNumPhotos.setText(
                value.toInt().toString()
            )
        }
        binding.editNumPhotos.setText("180")
        binding.editNumPhotos.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.numPhotosSlider.value = s.toString().toFloat()
            }
        })

        val dialogView = binding.root
        dialogView.tag = binding

        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder
                .setView(dialogView)
                .setPositiveButton(R.string.start_scanning) { _, _ ->
                    val action = ScannedListFragmentDirections
                        .actionScannedListFragmentToSessionRecordFragment(
                            binding.sessionNameEdit.text.toString().ifBlank {
                                getString(R.string.default_title,
                                SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                                    .format(System.currentTimeMillis()))
                            },
                            binding.numPhotosSlider.value.toInt()
                        )
                    findNavController().navigate(action)
                }
                .setNegativeButton(R.string.close) { _, _ -> }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}