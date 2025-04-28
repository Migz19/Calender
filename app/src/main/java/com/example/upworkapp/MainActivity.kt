package com.example.upworkapp

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.example.upworkapp.databinding.ActivityMainBinding

// MainActivity.kt


import android.app.AlertDialog

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast

import androidx.recyclerview.widget.LinearLayoutManager

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val eventList = mutableListOf<Event>()
    private lateinit var eventsAdapter: EventAdapter
    private var currentDate = Calendar.getInstance()
    private var viewMode = ViewMode.MONTH

    enum class ViewMode {
        MONTH, WEEK
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModeSpinner()
        setupCalendarView()
        setupEventsList()
        setupButtons()
        updateUI()
    }

    private fun setupViewModeSpinner() {
        val viewModes = arrayOf("Month View", "Week View")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, viewModes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerViewMode.adapter = adapter
        binding.spinnerViewMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewMode = if (position == 0) ViewMode.MONTH else ViewMode.WEEK
                updateUI()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            currentDate.set(year, month, dayOfMonth)
            updateUI()
        }
    }

    private fun setupEventsList() {
        eventsAdapter = EventAdapter(
            eventList,
            onItemClick = { position -> showEventDetails(position) },
            onDeleteClick = { position -> deleteEvent(position) }
        )

        binding.recyclerViewEvents.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = eventsAdapter
        }
    }

    private fun setupButtons() {
        binding.buttonAddEvent.setOnClickListener {
            showAddEventDialog()
        }

        binding.buttonPrevious.setOnClickListener {
            if (viewMode == ViewMode.MONTH) {
                currentDate.add(Calendar.MONTH, -1)
            } else {
                currentDate.add(Calendar.WEEK_OF_YEAR, -1)
            }
            updateUI()
        }

        binding.buttonNext.setOnClickListener {
            if (viewMode == ViewMode.MONTH) {
                currentDate.add(Calendar.MONTH, 1)
            } else {
                currentDate.add(Calendar.WEEK_OF_YEAR, 1)
            }
            updateUI()
        }

        binding.buttonToday.setOnClickListener {
            currentDate = Calendar.getInstance()
            updateUI()
        }
    }

    private fun updateUI() {
        updateCalendarDisplay()
        updateDateTitle()
        updateEventsList()
    }

    private fun updateCalendarDisplay() {
        binding.calendarView.date = currentDate.timeInMillis

        // Toggle visibility based on view mode
        if (viewMode == ViewMode.MONTH) {
            binding.calendarView.visibility = View.VISIBLE
            binding.weekViewContainer.visibility = View.GONE
        } else {
            binding.calendarView.visibility = View.GONE
            binding.weekViewContainer.visibility = View.VISIBLE
            updateWeekView()
        }
    }

    private fun updateWeekView() {
        // Create copies of the current date to show the week
        val startOfWeek = Calendar.getInstance().apply {
            timeInMillis = currentDate.timeInMillis
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }

        // Clear previous week days
        binding.weekLinearLayout.removeAllViews()

        // Add 7 day views for the current week
        for (i in 0 until 7) {
            val dayView = layoutInflater.inflate(R.layout.day_view_item, binding.weekLinearLayout, false)
            val dateText = dayView.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.textViewDate)
            val dayText = dayView.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.textViewDay)
            val eventIndicator = dayView.findViewById<View>(R.id.eventIndicator)

            val dayCalendar = Calendar.getInstance().apply {
                timeInMillis = startOfWeek.timeInMillis
                add(Calendar.DAY_OF_MONTH, i)
            }

            // Format date and day name
            val dateFormatter = SimpleDateFormat("d", Locale.getDefault())
            val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())

            dateText.text = dateFormatter.format(dayCalendar.time)
            dayText.text = dayFormatter.format(dayCalendar.time)

            // Highlight current date
            if (isToday(dayCalendar)) {
                dateText.setBackgroundResource(R.drawable.current_day_background)
            }

            // Show event indicator if there are events on this day
            eventIndicator.visibility = if (hasEventsOnDate(dayCalendar)) View.VISIBLE else View.INVISIBLE

            // Set click listener for each day
            dayView.setOnClickListener {
                currentDate.timeInMillis = dayCalendar.timeInMillis
                updateUI()
            }

            binding.weekLinearLayout.addView(dayView)
        }
    }

    private fun isToday(calendar: Calendar): Boolean {
        val today = Calendar.getInstance()
        return (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH))
    }

    private fun hasEventsOnDate(calendar: Calendar): Boolean {
        return eventList.any {
            val eventCal = Calendar.getInstance().apply { time = it.dateTime }
            (eventCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                    eventCal.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                    eventCal.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH))
        }
    }

    private fun updateDateTitle() {
        val titleFormat = if (viewMode == ViewMode.MONTH) {
            SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        } else {
            SimpleDateFormat("MMM d - ", Locale.getDefault())
        }

        val text = if (viewMode == ViewMode.MONTH) {
            titleFormat.format(currentDate.time)
        } else {
            // For week view, show the range
            val startOfWeek = Calendar.getInstance().apply {
                timeInMillis = currentDate.timeInMillis
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            }
            val endOfWeek = Calendar.getInstance().apply {
                timeInMillis = startOfWeek.timeInMillis
                add(Calendar.DAY_OF_MONTH, 6)
            }

            val endFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            "${titleFormat.format(startOfWeek.time)}${endFormat.format(endOfWeek.time)}"
        }

        binding.textViewCurrentDate.text = text
    }

    private fun updateEventsList() {
        val selectedDateEvents = eventList.filter {
            val eventCal = Calendar.getInstance().apply { time = it.dateTime }
            eventCal.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
                    eventCal.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
                    eventCal.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH)
        }.toMutableList()

        eventsAdapter.updateData(selectedDateEvents)

        if (selectedDateEvents.isEmpty()) {
            binding.textViewNoEvents.visibility = View.VISIBLE
            binding.recyclerViewEvents.visibility = View.GONE
        } else {
            binding.textViewNoEvents.visibility = View.GONE
            binding.recyclerViewEvents.visibility = View.VISIBLE
        }
    }

    private fun showAddEventDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
        val titleInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextTitle)
        val locationInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextLocation)
        val notesInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextNotes)
        val dateInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextDate)
        val timeInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextTime)

        // Set current date as default
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        dateInput.setText(dateFormat.format(currentDate.time))
        timeInput.setText(timeFormat.format(Date()))

        // Date picker dialog
        dateInput.setOnClickListener {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = android.app.DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    cal.set(selectedYear, selectedMonth, selectedDay)
                    dateInput.setText(dateFormat.format(cal.time))
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        // Time picker dialog
        timeInput.setOnClickListener {
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)

            val timePickerDialog = android.app.TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    cal.set(Calendar.HOUR_OF_DAY, selectedHour)
                    cal.set(Calendar.MINUTE, selectedMinute)
                    timeInput.setText(timeFormat.format(cal.time))
                },
                hour, minute, true
            )
            timePickerDialog.show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add New Event")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                try {
                    val title = titleInput.text.toString()
                    val location = locationInput.text.toString()
                    val notes = notesInput.text.toString()
                    val dateStr = dateInput.text.toString()
                    val timeStr = timeInput.text.toString()

                    if (title.isBlank()) {
                        Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // Parse date and time
                    val dateTimeStr = "$dateStr $timeStr"
                    val dateTimeFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
                    val dateTime = dateTimeFormat.parse(dateTimeStr) ?: Date()

                    // Create and add new event
                    val newEvent = Event(
                        id = System.currentTimeMillis(),
                        title = title,
                        dateTime = dateTime,
                        location = location,
                        notes = notes
                    )
                    eventList.add(newEvent)
                    updateUI()
                    Toast.makeText(this, "Event added", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error adding event: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEventDetails(position: Int) {
        val selectedDateEvents = eventList.filter {
            val eventCal = Calendar.getInstance().apply { time = it.dateTime }
            eventCal.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
                    eventCal.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
                    eventCal.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH)
        }

        val event = selectedDateEvents[position]
        val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())

        AlertDialog.Builder(this)
            .setTitle(event.title)
            .setMessage(
                "Date & Time: ${dateFormat.format(event.dateTime)}\n" +
                        "Location: ${event.location}\n" +
                        "Notes: ${event.notes}"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun deleteEvent(position: Int) {
        val selectedDateEvents = eventList.filter {
            val eventCal = Calendar.getInstance().apply { time = it.dateTime }
            eventCal.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
                    eventCal.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
                    eventCal.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH)
        }

        val event = selectedDateEvents[position]

        AlertDialog.Builder(this)
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete '${event.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                eventList.remove(event)
                updateUI()
                Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}