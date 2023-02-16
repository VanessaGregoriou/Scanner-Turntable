package com.android.app.itemscanner

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

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
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater
            builder
                .setTitle(R.string.add_first_session)
                .setView(inflater.inflate(R.layout.create_session_dialog, null))
                .setPositiveButton(R.string.start_scanning) { _, _ ->
                    listener.onStart(this)
                }
                .setNegativeButton(R.string.close) { _, _ ->
                    listener.onClose(this)
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}