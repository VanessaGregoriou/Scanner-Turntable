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

class SessionCreateDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding: CreateSessionDialogBinding = DataBindingUtil.inflate(
            layoutInflater, R.layout.create_session_dialog, null, false
        )

        binding.numPhotosSlider.value = 10f
        binding.numPhotosSlider.addOnChangeListener { _, value, _ ->
            binding.editNumPhotos.setText(
                value.toInt().toString()
            )
        }
        binding.editNumPhotos.setText(binding.numPhotosSlider.value.toInt().toString())
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
                .setPositiveButton(R.string.dialog_start_button) { _, _ ->
                    val action =
                        ScannedListFragmentDirections.actionScannedListFragmentToSessionRecordFragment(
                            binding.sessionNameEdit.text.toString().ifBlank {
                                getString(R.string.default_title)
                            },
                            binding.numPhotosSlider.value.toInt()
                        )
                    findNavController().navigate(action)
                }
                .setNegativeButton(R.string.dialog_close_button) { _, _ -> }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}