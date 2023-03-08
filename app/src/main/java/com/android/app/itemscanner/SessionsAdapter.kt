package com.android.app.itemscanner

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.android.app.itemscanner.api.ScanSession
import com.android.app.itemscanner.api.SessionItemViewModel
import com.android.app.itemscanner.databinding.SessionItemBinding
import java.io.File


class SessionsAdapter(
    context: Context,
    resource: Int,
    sessions: List<ScanSession>,
    databaseController: DatabaseController
) :
    ArrayAdapter<ScanSession>(context, resource, sessions) {

    private val databaseController: DatabaseController
    private val sessions: List<ScanSession>

    init {
        this.databaseController = databaseController
        this.sessions = sessions
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view: View
        val binding: SessionItemBinding
        if (convertView == null) {
            binding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.session_item, parent, false
            )
            view = binding.root
        } else {
            view = convertView
            binding = convertView.tag as SessionItemBinding
        }

        view.tag = binding
        binding.sessionViewModel =
            SessionItemViewModelImpl(context, binding, databaseController, sessions[position])
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        return convertView
    }

    private class SessionItemViewModelImpl(
        context: Context,
        binding: SessionItemBinding,
        database: DatabaseController,
        scanSession: ScanSession
    ) : SessionItemViewModel {
        private val context: Context
        private val binding: SessionItemBinding
        private val database: DatabaseController
        private val scanSession: ScanSession
        private var showExtraDetails: Boolean

        init {
            this.context = context
            this.binding = binding
            this.database = database
            this.scanSession = scanSession
            this.showExtraDetails = true
        }

        override fun getTitle(): String {
            return scanSession.title
        }

        override fun getThumbnail(): Drawable? {
            return scanSession.image?.let {
                BitmapDrawable(context.resources, it)
            }.run {
                context.getDrawable(R.drawable.scanner_icon)
            }
        }

        override fun getCreationData(): String {
            return scanSession.creationTime.toString()
        }

        override fun getNumPhotos(): Int {
            return scanSession.numPhotos
        }

        override fun showExtraDetails(): Boolean {
            return showExtraDetails
        }

        override fun onToggleDetailsClick(): View.OnClickListener {
            return View.OnClickListener {
                this.showExtraDetails = !this.showExtraDetails
                binding.invalidateAll()
            }
        }

        override fun onRenameClick(): View.OnClickListener {
            return View.OnClickListener {
                // Set up the input
                val input = EditText(context)
                input.inputType = InputType.TYPE_CLASS_TEXT
                AlertDialog.Builder(context)
                    .setTitle("New title")
                    .setView(input)
                    .setPositiveButton("OK") { _, _ ->
                        database.editTitle(input.text.toString(), scanSession)
                        binding.invalidateAll()
                    }.show()
            }
        }

        override fun onShareClick(): View.OnClickListener {
            return View.OnClickListener {
                // getExternalFilesDir() + "/Pictures" should match the declaration in fileprovider.xml paths
                val file = File(scanSession.zipFile.path)

                // wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration

                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        FileProvider.getUriForFile(
                            context, "com.android.app.itemscanner.fileprovider", file
                        )
                    )
                    type = "application/zip"
                }

                val shareIntent = Intent.createChooser(sendIntent, scanSession.zipFile.path)
                context.startActivity(shareIntent)
            }
        }

        override fun onDeleteClick(): View.OnClickListener {
            return View.OnClickListener {
                database.deleteSession(scanSession)
            }
        }

    }
}