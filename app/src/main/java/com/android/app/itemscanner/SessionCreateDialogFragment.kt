package com.android.app.itemscanner

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.android.app.itemscanner.databinding.CreateSessionDialogBinding

class SessionCreateDialogFragment(listener: DialogListener) : DialogFragment() {

    private val listener: DialogListener

    init {
        this.listener = listener
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface DialogListener {
        fun onStart(dialog: DialogFragment)
        fun onClose(dialog: DialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding: CreateSessionDialogBinding = DataBindingUtil.inflate(
            layoutInflater, R.layout.create_session_dialog, null, false
        )

        binding.numPhotosSlider.value = 180f
        binding.numPhotosSlider.addOnChangeListener { slider, value, fromUser ->
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
                .setPositiveButton(R.string.start_scanning) { dialog, _ ->

                }
                .setNegativeButton(R.string.close) { _, _ ->
                    listener.onClose(this)
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}