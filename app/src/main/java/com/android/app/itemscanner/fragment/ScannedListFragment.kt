package com.android.app.itemscanner.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.android.app.itemscanner.R
import com.android.app.itemscanner.DatabaseController
import com.android.app.itemscanner.api.ScanSession
import com.android.app.itemscanner.databinding.ScannedListFragmentBinding
import com.android.app.itemscanner.databinding.SessionItemBinding

/**
 * A simple [Fragment] subclass to display the list of folders containing item scanning sessions.
 */
class ScannedListFragment : Fragment() {

    private var _binding: ScannedListFragmentBinding? = null
    private var sessionsList: List<ScanSession> = listOf()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = ScannedListFragmentBinding.inflate(inflater, container, false)
        sessionsList = DatabaseController(requireContext()).getSessions()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (sessionsList.isEmpty()) {
            binding.textviewFirst.visibility = View.VISIBLE
        } else {
            val adapter =
                context?.let { SessionsAdapter(it, R.layout.session_item, sessionsList) }
            binding.sessionsList.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class SessionsAdapter(context: Context, resource: Int, sessions: List<ScanSession>) :
        ArrayAdapter<ScanSession>(context, resource, sessions) {

        private val sessions: List<ScanSession>

        init {
            this.sessions = sessions
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view: View
            val binding: SessionItemBinding
            val session = sessions[position]
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

            binding.sessionTitle.text = session.title
            session.thumbnail?.let {
                binding.thumbnail.setImageBitmap(it)
            }.run {
                binding.thumbnail.setImageDrawable(context.getDrawable(R.drawable.scanner_icon))
            }
            binding.invalidateAll()
            view.tag = binding
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            return convertView
        }
    }
}