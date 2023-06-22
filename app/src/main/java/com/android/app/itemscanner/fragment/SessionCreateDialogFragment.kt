package com.android.app.itemscanner.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.android.app.itemscanner.R
import com.android.app.itemscanner.fragment.ScannedListFragmentDirections
import com.android.app.itemscanner.databinding.CreateSessionDialogBinding
import kotlin.math.max
import kotlin.math.min

class SessionCreateDialogFragment : DialogFragment() {

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
        binding.editNumPhotos.setText(binding.numPhotosSlider.value.toInt().toString())
        binding.editNumPhotos.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank()) {
                    return
                }
                val num = s.toString().toFloat()
                binding.numPhotosSlider.value = min(
                    max(num, binding.numPhotosSlider.valueFrom),
                    binding.numPhotosSlider.valueTo
                )
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val dialogView = binding.root
        dialogView.tag = binding

        return activity?.let {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder(it)
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_start_button) { _, _ ->
                    val action =
                        ScannedListFragmentDirections.actionScannedListFragmentToSessionRecordFragment(
                            binding.sessionNameEdit.text.toString().ifBlank {
                                getString(R.string.default_title)
                            },
                            binding.numPhotosSlider.value.toInt()
                        )
                    findNavController().navigate(action)
                }.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}