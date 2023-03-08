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
import com.android.app.itemscanner.SessionsAdapter
import com.android.app.itemscanner.api.ScanSession
import com.android.app.itemscanner.databinding.ScannedListFragmentBinding

/**
 * A simple [Fragment] subclass to display the list of folders containing item scanning sessions.
 */
class ScannedListFragment : Fragment() {

    private var _binding: ScannedListFragmentBinding? = null
    private lateinit var database: DatabaseController
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
        database = DatabaseController(requireContext())
        sessionsList = database.getSessions()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (sessionsList.isEmpty()) {
            binding.textviewFirst.visibility = View.VISIBLE
        } else {
            val adapter =
                context?.let { SessionsAdapter(it, R.layout.session_item, sessionsList, database) }
            binding.sessionsList.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}