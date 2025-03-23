package com.matthaug.taskmanager.fragments

import android.icu.util.Currency
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

        val name = arguments?.getString("taskName", "") ?: ""
        val dueDate = arguments?.getString("taskDueDate", "") ?: ""
        val priority = arguments?.getString("taskPriority", "") ?: ""
        val costAssociated = arguments?.getBoolean("costAssociated", false) ?: false
        val cost = arguments?.getDouble("cost", 0.0) ?: 0.0
        val currencyCode = arguments?.getString("currency", "CAD") ?: "CAD"
        val completed = arguments?.getBoolean("completed", false) ?: false
        val overdue = arguments?.getBoolean("overdue", false) ?: false

        view.findViewById<TextView>(R.id.taskNameTextView).text = name
        view.findViewById<TextView>(R.id.taskDueDateTextView).text = "Due Date: $dueDate"
        view.findViewById<TextView>(R.id.taskPriorityTextView).text = "Priority: $priority"
        view.findViewById<TextView>(R.id.taskCostAssociatedTextView).text = "Cost Associated: ${if (costAssociated) "Yes" else "No"}"
        view.findViewById<TextView>(R.id.taskCostTextView).text = "Cost: ${Currency.getInstance(currencyCode).symbol}$cost"
        view.findViewById<TextView>(R.id.taskCompletedTextView).text = "Completed: $completed"
        view.findViewById<TextView>(R.id.taskOverdueTextView).text = "Overdue: $overdue"

        return view
    }
}
