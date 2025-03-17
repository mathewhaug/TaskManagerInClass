package com.matthaug.taskmanager

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TaskDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)

        // Get the task details from the intent
        val taskName = intent.getStringExtra("taskName")
        val taskDueDate = intent.getStringExtra("taskDueDate")
        val taskPriority = intent.getStringExtra("taskPriority")

        // Find the TextViews to display the task details
        val nameTextView: TextView = findViewById(R.id.taskNameTextView)
        val dueDateTextView: TextView = findViewById(R.id.taskDueDateTextView)
        val priorityTextView: TextView = findViewById(R.id.taskPriorityTextView)

        // Set the values to the TextViews
        nameTextView.text = taskName
        dueDateTextView.text = taskDueDate
        priorityTextView.text = taskPriority
    }
}
