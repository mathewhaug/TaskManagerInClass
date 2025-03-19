package com.matthaug.taskmanager

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

private const val FILE_NAME = "tasks.txt"

class MainActivity : AppCompatActivity(), TaskAdapter.TaskItemListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load saved tasks from file
        taskList.addAll(loadTasksFromFile())

        // Setup RecyclerView with GridLayoutManager for 2 columns
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns
        taskAdapter = TaskAdapter(taskList, this)
        recyclerView.adapter = taskAdapter

        // Handle FloatingActionButton Click to Add Task
        val addTaskFab: FloatingActionButton = findViewById(R.id.addTaskFab)
        addTaskFab.setOnClickListener {
            findNavController(R.id.fragmentContainer).navigate(R.id.action_mainFragment_to_addTaskFragment)
        }
    }

    // Handle task edit click event
    override fun onEditClick(task: Task) {
        val bundle = Bundle().apply {
            putInt("taskId", task.id.toInt())
            putString("taskName", task.name)
            putString("taskDueDate", task.dueDate)
            putString("taskPriority", task.priority)
        }
        findNavController(R.id.fragmentContainer).navigate(R.id.action_mainFragment_to_addTaskFragment, bundle)
    }

    override fun onDeleteClick(task: Task) {
        taskList.remove(task)
        taskAdapter.notifyDataSetChanged()
        saveTasksToFile() // Save updated task list
    }

    private fun saveTasksToFile() {
        try {
            val json = Gson().toJson(taskList)
            openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { output ->
                output.write(json.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadTasksFromFile(): MutableList<Task> {
        val loadedTasks = mutableListOf<Task>()
        try {
            val file = File(filesDir, FILE_NAME)
            if (!file.exists()) return loadedTasks

            val json = file.readText()
            val type = object : TypeToken<List<Task>>() {}.type
            val taskListFromFile: List<Task> = Gson().fromJson(json, type)
            loadedTasks.addAll(taskListFromFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return loadedTasks
    }
}
