package com.matthaug.taskmanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.matthaug.taskmanager.R

class TaskDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task_details, container, false)

        // Retrieve data from the arguments
        val taskName = arguments?.getString("taskName") ?: "No Name"
        val taskDueDate = arguments?.getString("taskDueDate") ?: "No Due Date"
        val taskPriority = arguments?.getString("taskPriority") ?: "No Priority"

        // Set data to the TextViews
        view.findViewById<TextView>(R.id.taskNameTextView).text = taskName
        view.findViewById<TextView>(R.id.taskDueDateTextView).text = taskDueDate
        view.findViewById<TextView>(R.id.taskPriorityTextView).text = taskPriority

        return view

    }
}