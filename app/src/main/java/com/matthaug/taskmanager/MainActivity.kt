package com.matthaug.taskmanager

import TaskAdapter
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity(), TaskAdapter.TaskItemListener, AddTaskFragment.AddTaskListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Add some dummy tasks
        addDummyTasks()

        // Setup RecyclerView with GridLayoutManager for 2 columns
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns
        taskAdapter = TaskAdapter(taskList, this)
        recyclerView.adapter = taskAdapter

        // Add Task Button click listener
        val addTaskButton: Button = findViewById(R.id.addTaskButton)
        addTaskButton.setOnClickListener {
            showAddTaskFragment()
        }
    }

    // Method to add dummy tasks
    private fun addDummyTasks() {
        taskList.add(Task(1, "Complete Homework", "12/05/2025", "High"))
        taskList.add(Task(2, "Buy Groceries", "15/05/2025", "Medium"))
        taskList.add(Task(3, "Call Mom", "13/05/2025", "Low"))
        taskList.add(Task(4, "Pay Bills", "14/05/2025", "High"))
        taskList.add(Task(5, "Plan Vacation", "20/05/2025", "Low"))
        taskList.add(Task(6, "Cry", "16/05/2025", "High"))
    }

    override fun onEditClick(task: Task) {
        showAddTaskFragment(task)
    }

    override fun onDeleteClick(task: Task) {
        taskList.remove(task)
        taskAdapter.notifyDataSetChanged()
    }

    private fun showAddTaskFragment(task: Task? = null) {
        val addTaskFragment = AddTaskFragment()
        addTaskFragment.setListener(this)
        task?.let {
            addTaskFragment.setTaskToEdit(it)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, addTaskFragment)
            .addToBackStack(null)
            .commit()
    }

    // AddTaskListener implementation to handle task added or updated
    override fun onTaskAdded(task: Task) {
        taskList.add(task)
        taskAdapter.notifyItemInserted(taskList.size - 1)
    }

    override fun onTaskUpdated(task: Task) {
        val index = taskList.indexOfFirst { it.id == task.id }
        if (index != -1) {
            taskList[index] = task
            taskAdapter.notifyItemChanged(index)
        }
    }
}