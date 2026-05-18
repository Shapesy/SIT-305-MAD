package com.example.eventplanner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.eventplanner.adapter.EventAdapter
import com.example.eventplanner.data.Event
import com.example.eventplanner.databinding.FragmentEventListBinding
import com.example.eventplanner.viewmodel.EventViewModel
import com.google.android.material.snackbar.Snackbar

class EventListFragment : Fragment() {

    private var _binding: FragmentEventListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = EventAdapter(
            onEditClick = { event -> navigateToEdit(event) },
            onDeleteClick = { event -> deleteEvent(event) }
        )

        binding.recyclerViewEvents.adapter = adapter

        viewModel.allEvents.observe(viewLifecycleOwner) { events ->
            adapter.submitList(events)
            binding.tvNoEvents.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun navigateToEdit(event: Event) {
        val action = EventListFragmentDirections.actionEventListToAddEdit(event.id)
        findNavController().navigate(action)
    }

    private fun deleteEvent(event: Event) {
        viewModel.deleteEvent(event)
        Snackbar.make(binding.root, "\"${event.title}\" deleted", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                viewModel.insertEvent(event.copy(id = 0))
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
