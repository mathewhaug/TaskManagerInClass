package com.matthaug.taskmanager

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
//Gson Import
import com.google.gson.Gson
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

private const val FILE_NAME = "tasks.txt"

class MainFragment : Fragment(), TaskAdapter.TaskItemListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<Task>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Prevent duplicate tasks by clearing the list before loading
        taskList.clear()
        taskList.addAll(loadTasksFromFile(requireContext()))

        taskAdapter = TaskAdapter(taskList, this)
        recyclerView.adapter = taskAdapter

        val addTaskButton: FloatingActionButton = view.findViewById(R.id.addTaskFab)
        addTaskButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_addTaskFragment)
        }

        return view
    }


    override fun onEditClick(task: Task) {
        val bundle = Bundle().apply {
            putInt("taskId", task.id.toInt())
            putString("taskName", task.name)
            putString("taskDueDate", task.dueDate)
            putString("taskPriority", task.priority)
        }
        findNavController().navigate(R.id.action_mainFragment_to_addTaskFragment, bundle)
    }

    override fun onDeleteClick(task: Task) {
        taskList.remove(task)
        taskAdapter.notifyDataSetChanged()
        saveTasksToFile(requireContext(), taskList) // Save updated list
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the saved state handle for new or edited tasks
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Bundle>("newTask")
            ?.observe(viewLifecycleOwner) { bundle ->
                val updatedTask = Task(
                    bundle.getInt("taskId"),
                    bundle.getString("taskName", ""),
                    bundle.getString("taskDueDate", ""),
                    bundle.getString("taskPriority", "")
                )

                // Check if the task already exists in the list
                val index = taskList.indexOfFirst { it.id == updatedTask.id }
                if (index != -1) {
                    // If found, update it
                    taskList[index] = updatedTask
                    taskAdapter.notifyItemChanged(index)
                } else {
                    // If not found, add as a new task
                    taskList.add(updatedTask)
                    taskAdapter.notifyItemInserted(taskList.size - 1)
                }

                // Save the updated list to file
                saveTasksToFile(requireContext(), taskList)
            }
    }

    private fun saveTasksToFile(context: Context, taskList: List<Task>) {
        try {
            val json = Gson().toJson(taskList)
            context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { output ->
                output.write(json.toByteArray())
            }
            Log.d("FileStorage", "Tasks saved successfully")
        } catch (e: IOException) {
            Log.e("FileStorage", "Error saving tasks: ${e.message}")
        }
    }

    private fun loadTasksFromFile(context: Context): MutableList<Task> {
        val taskList: MutableList<Task> = mutableListOf()
        try {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) return taskList

            val json = file.readText()
            val type = object : TypeToken<List<Task>>() {}.type
            val loadedTasks: List<Task> = Gson().fromJson(json, type)
            taskList.addAll(loadedTasks)

            Log.d("FileStorage", "Tasks loaded successfully")
        } catch (e: FileNotFoundException) {
            Log.e("FileStorage", "File not found: ${e.message}")
        } catch (e: IOException) {
            Log.e("FileStorage", "Error reading file: ${e.message}")
        }
        return taskList
    }
}
