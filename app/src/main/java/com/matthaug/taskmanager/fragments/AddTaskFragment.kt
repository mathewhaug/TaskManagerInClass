package com.matthaug.taskmanager.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.matthaug.taskmanager.R
import com.matthaug.taskmanager.models.Task
import java.util.Calendar
import java.util.Locale

class AddTaskFragment : Fragment() {

    private var taskToEdit: Task? = null
    private lateinit var taskDueDateEditText: EditText
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_task, container, false)

        val taskNameEditText: EditText = view.findViewById(R.id.taskName)
        taskDueDateEditText = view.findViewById(R.id.taskDueDate)
        val taskPriorityEditText: EditText = view.findViewById(R.id.taskPriority)
        val saveButton: Button = view.findViewById(R.id.saveButton)

        //making DatePicker opens on clicking the due date field - annoying
        taskDueDateEditText.apply {
            isFocusable = false
            isClickable = true //This is redundent, it just wasnt working for me so I added it, why it works idk
            setOnClickListener {
                showDatePickerDialog()
            }
        }

        // Retrieve task data if editing using built in arguments from bundle
        arguments?.let {
            val taskId = it.getInt("taskId", -1)
            val taskName = it.getString("taskName", "")
            val taskDueDate = it.getString("taskDueDate", "")
            val taskPriority = it.getString("taskPriority", "")

            if (taskId != -1) {
                taskToEdit = Task(taskId, taskName!!, taskDueDate!!, taskPriority!!)
                taskNameEditText.setText(taskName)
                taskDueDateEditText.setText(taskDueDate)
                taskPriorityEditText.setText(taskPriority)
            }
        }

        saveButton.setOnClickListener {
            val taskName = taskNameEditText.text.toString()
            val taskDueDate = taskDueDateEditText.text.toString()
            val taskPriority = taskPriorityEditText.text.toString()
            //seeding task if with time if one is not provided, to ensure it is unique
            val taskId = taskToEdit?.id ?: (System.currentTimeMillis() / 1000).toInt()
            //SavedStateHandle does not support passing custom objects, so we convert and pass a bundle instead
            val bundle = Bundle().apply {
                putInt("taskId", taskId.toInt())
                putString("taskName", taskName)
                putString("taskDueDate", taskDueDate)
                putString("taskPriority", taskPriority)
            }

            val navController = findNavController()
            // Pass the bundle to the previous fragment to add to the list
            navController.previousBackStackEntry?.savedStateHandle?.set("newTask", bundle) // Pass the bundle instead of Task
            // Navigate back to the previous fragment
            navController.popBackStack()
        }

        return view
    }

    private fun showDatePickerDialog() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format(
                    Locale.getDefault(),
                    "%02d/%02d/%04d",
                    selectedDay,
                    selectedMonth + 1,
                    selectedYear
                )
                taskDueDateEditText.setText(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }
}