package com.matthaug.taskmanager.fragments

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.Currency
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.matthaug.taskmanager.R
import com.matthaug.taskmanager.models.Task
import java.util.*

class AddTaskFragment : Fragment() {

    private var taskToEdit: Task? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_task, container, false)

        val taskNameEditText: EditText = view.findViewById(R.id.taskName)
        val taskDueDateEditText: EditText = view.findViewById(R.id.taskDueDate)
        val taskPriorityEditText: EditText = view.findViewById(R.id.taskPriority)
        val costAssociatedCheckBox: CheckBox = view.findViewById(R.id.costAssociatedCheckBox)
        val taskCostEditText: EditText = view.findViewById(R.id.taskCost)
        val currencySpinner: Spinner = view.findViewById(R.id.currencySpinner)
        val completedCheckBox: CheckBox = view.findViewById(R.id.completedCheckBox)
        val saveButton: Button = view.findViewById(R.id.saveButton)

        // Populate currency spinner with all avail currencies
        val currencies = Currency.getAvailableCurrencies().map { it.currencyCode }.sorted()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner.adapter = adapter

        val defaultIndex = currencies.indexOfFirst { it.toString() == "CAD" }

        if (defaultIndex >= 0) {
            currencySpinner.setSelection(defaultIndex)
        }


        // Show Date Picker
        taskDueDateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val formattedDate = "%02d/%02d/%04d".format(dayOfMonth, month + 1, year)
                    taskDueDateEditText.setText(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // If editing, populate values
        arguments?.let {
            val id = it.getInt("taskId", -1)
            if (id != -1) {
                val name = it.getString("taskName", "")
                val dueDate = it.getString("taskDueDate", "")
                val priority = it.getString("taskPriority", "")
                val costAssociated = it.getBoolean("costAssociated", false)
                val currency = Currency.getInstance(it.getString("currency", "CAD"))
                val cost = it.getDouble("cost", 0.0)
                val completed = it.getBoolean("completed", false)
                val overdue = it.getBoolean("overdue", false)

                taskToEdit = Task(id, name, dueDate, priority, costAssociated, currency, cost, completed, overdue)

                // Populate UI
                taskNameEditText.setText(name)
                taskDueDateEditText.setText(dueDate)
                taskPriorityEditText.setText(priority)
                costAssociatedCheckBox.isChecked = costAssociated
                taskCostEditText.setText(cost.toString())
                completedCheckBox.isChecked = completed
                currencySpinner.setSelection(currencies.indexOf(currency.currencyCode))
            }
        }


        saveButton.setOnClickListener {
            val name = taskNameEditText.text.toString()
            val dueDate = taskDueDateEditText.text.toString()
            val priority = taskPriorityEditText.text.toString()
            val costAssociated = costAssociatedCheckBox.isChecked
            val cost = taskCostEditText.text.toString().toDoubleOrNull() ?: 0.0
            val currency = Currency.getInstance(currencySpinner.selectedItem.toString())
            val completed = completedCheckBox.isChecked

            // Calculate overdue
            val overdue = isOverdue(dueDate)

            val newTask = Task(
                id = taskToEdit?.id ?: (System.currentTimeMillis() / 1000).toInt(),
                name = name,
                dueDate = dueDate,
                priority = priority,
                costAssociated = costAssociated,
                currency = currency,
                cost = cost,
                completed = completed,
                overdue = overdue
            )

            //Send back via SavedStateHandle to populate list
            findNavController().previousBackStackEntry?.savedStateHandle?.set("newTask", bundleOf(
                "taskId" to newTask.id,
                "taskName" to newTask.name,
                "taskDueDate" to newTask.dueDate,
                "taskPriority" to newTask.priority,
                "costAssociated" to newTask.costAssociated,
                "currency" to newTask.currency.currencyCode,
                "cost" to newTask.cost,
                "completed" to newTask.completed,
                "overdue" to newTask.overdue
            ))

            findNavController().popBackStack()
        }

        return view
    }

    private fun isOverdue(dueDateStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dueDate = sdf.parse(dueDateStr)
            val today = Date()
            dueDate != null && dueDate.before(today)
        } catch (e: Exception) {
            false
        }
    }
}
