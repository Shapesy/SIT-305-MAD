package com.example.eventplanner.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.eventplanner.R
import com.example.eventplanner.data.Event
import com.example.eventplanner.databinding.FragmentAddEditEventBinding
import com.example.eventplanner.viewmodel.EventViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEditEventFragment : Fragment() {

    private var _binding: FragmentAddEditEventBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventViewModel by activityViewModels()

    private val args: AddEditEventFragmentArgs by navArgs()

    private val selectedDateTime = Calendar.getInstance()
    private var isDateSelected = false
    private var existingEvent: Event? = null

    private val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy  •  HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryDropdown()

        if (args.eventId != -1) {
            loadExistingEvent(args.eventId)
        }

        binding.btnPickDateTime.setOnClickListener { showDatePicker() }
        binding.btnSaveEvent.setOnClickListener { attemptSave() }
    }

    private fun setupCategoryDropdown() {
        val categories = resources.getStringArray(R.array.event_categories)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun loadExistingEvent(eventId: Int) {
        lifecycleScope.launch {
            existingEvent = viewModel.getEventById(eventId) ?: return@launch

            existingEvent!!.let { event ->
                binding.etTitle.setText(event.title)
                binding.etLocation.setText(event.location)

                val categories = resources.getStringArray(R.array.event_categories)
                val idx = categories.indexOf(event.category)
                if (idx >= 0) binding.spinnerCategory.setSelection(idx)

                selectedDateTime.timeInMillis = event.dateTime
                isDateSelected = true
                updateDateTimeLabel()

                binding.btnSaveEvent.text = getString(R.string.btn_update_event)
                binding.tvFormTitle.text = getString(R.string.label_edit_event)
            }
        }
    }

    private fun showDatePicker() {
        val now = Calendar.getInstance()
        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDateTime.set(Calendar.YEAR, year)
                selectedDateTime.set(Calendar.MONTH, month)
                selectedDateTime.set(Calendar.DAY_OF_MONTH, day)
                showTimePicker()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )

        if (existingEvent == null) {
            dialog.datePicker.minDate = System.currentTimeMillis()
        }

        dialog.show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hour)
                selectedDateTime.set(Calendar.MINUTE, minute)
                selectedDateTime.set(Calendar.SECOND, 0)
                selectedDateTime.set(Calendar.MILLISECOND, 0)
                isDateSelected = true
                updateDateTimeLabel()
            },
            selectedDateTime.get(Calendar.HOUR_OF_DAY),
            selectedDateTime.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDateTimeLabel() {
        binding.tvSelectedDateTime.text = dateFormat.format(selectedDateTime.time)
        binding.tvSelectedDateTime.setTextColor(
            requireContext().getColor(R.color.text_primary)
        )
    }

    private fun attemptSave() {
        val title = binding.etTitle.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem?.toString() ?: ""

        if (title.isBlank()) {
            binding.tilTitle.error = getString(R.string.error_title_required)
            Snackbar.make(binding.root, R.string.error_title_required, Snackbar.LENGTH_SHORT).show()
            return
        }
        binding.tilTitle.error = null

        if (!isDateSelected) {
            Snackbar.make(binding.root, R.string.error_date_required, Snackbar.LENGTH_SHORT).show()
            return
        }

        if (existingEvent == null && selectedDateTime.timeInMillis <= System.currentTimeMillis()) {
            Snackbar.make(binding.root, R.string.error_past_date, Snackbar.LENGTH_LONG).show()
            return
        }

        val event = if (existingEvent != null) {
            existingEvent!!.copy(
                title = title,
                category = category,
                location = location,
                dateTime = selectedDateTime.timeInMillis
            )
        } else {
            Event(
                title = title,
                category = category,
                location = location,
                dateTime = selectedDateTime.timeInMillis
            )
        }

        if (existingEvent != null) {
            viewModel.updateEvent(event)
            Snackbar.make(binding.root, R.string.event_updated, Snackbar.LENGTH_SHORT).show()
        } else {
            viewModel.insertEvent(event)
            Snackbar.make(binding.root, R.string.event_added, Snackbar.LENGTH_SHORT).show()
        }

        requireActivity()
            .findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            ?.selectedItemId = R.id.eventListFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
