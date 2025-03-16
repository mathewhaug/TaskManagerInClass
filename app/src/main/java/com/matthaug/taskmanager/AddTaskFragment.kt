package com.matthaug.taskmanager

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText

class AddTaskFragment : Fragment(), View.OnClickListener {

    private var listener: AddTaskListener? = null
    private var taskToEdit: Task? = null

    private lateinit var taskNameEditText: EditText
    private lateinit var taskDueDateEditText: EditText
    private lateinit var taskPriorityEditText: EditText

    interface AddTaskListener {
        fun onTaskAdded(task: Task)
        fun onTaskUpdated(task: Task)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskNameEditText = view.findViewById(R.id.taskName)
        taskDueDateEditText = view.findViewById(R.id.taskDueDate)
        taskPriorityEditText = view.findViewById(R.id.taskPriority)

        // Set click listener on Due Date EditText to show DatePickerDialog
        taskDueDateEditText.setOnClickListener(this)

        val saveButton: Button = view.findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            val taskName = taskNameEditText.text.toString()
            val taskDueDate = taskDueDateEditText.text.toString()
            val taskPriority = taskPriorityEditText.text.toString()

            val task = Task(
                id = taskToEdit?.id ?: (System.currentTimeMillis() / 1000),
                name = taskName,
                dueDate = taskDueDate,
                priority = taskPriority
            )

            if (taskToEdit != null) {
                listener?.onTaskUpdated(task)
            } else {
                listener?.onTaskAdded(task)
            }
            parentFragmentManager.popBackStack()
        }

        taskToEdit?.let {
            taskNameEditText.setText(it.name)
            taskDueDateEditText.setText(it.dueDate)
            taskPriorityEditText.setText(it.priority)
        }
    }

    override fun onClick(view: View?) {
        if (view == taskDueDateEditText) {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "${selectedDay}/${selectedMonth + 1}/$selectedYear"
                taskDueDateEditText.setText(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    fun setListener(listener: AddTaskListener) {
        this.listener = listener
    }

    fun setTaskToEdit(task: Task) {
        this.taskToEdit = task
    }
}

