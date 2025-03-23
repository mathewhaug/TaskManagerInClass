package com.matthaug.taskmanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matthaug.taskmanager.models.Task
import android.icu.util.Currency
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.matthaug.taskmanager.R
import com.matthaug.taskmanager.TaskAdapter
import com.matthaug.taskmanager.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragment : Fragment(), TaskAdapter.TaskItemListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<Task>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        taskAdapter = TaskAdapter(taskList, this)
        recyclerView.adapter = taskAdapter

        val addTaskButton: FloatingActionButton = view.findViewById(R.id.addTaskFab)
        addTaskButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_addTaskFragment)
        }

        //Motivate button event listener
        val motivateMeButton: Button = view.findViewById(R.id.motivateMeButton)
        motivateMeButton.setOnClickListener {
            fetchMotivationalQuote()
        }


        return view
    }

    private fun fetchMotivationalQuote() {
        lifecycleScope.launch {
            try {
                val quotes = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getQuotes()
                }

                if (quotes.isNotEmpty()) {
                    val quote = quotes[0]
                    val message = "\"${quote.q}\" - ${quote.a}"
                    Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
                } else {
                    Snackbar.make(requireView(), "No quote found", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Bundle>("newTask")
            ?.observe(viewLifecycleOwner) { bundle ->

                val newTask = Task(
                    id = bundle.getInt("taskId"),
                    name = bundle.getString("taskName") ?: "",
                    dueDate = bundle.getString("taskDueDate") ?: "",
                    priority = bundle.getString("taskPriority") ?: "",
                    costAssociated = bundle.getBoolean("costAssociated"),
                    currency = Currency.getInstance("CAD"),
                    cost = bundle.getDouble("cost", 0.0),
                    completed = bundle.getBoolean("completed"),
                    overdue = bundle.getBoolean("overdue")
                )

                val index = taskList.indexOfFirst { it.id == newTask.id }
                if (index != -1) {
                    taskList[index] = newTask
                    taskAdapter.notifyItemChanged(index)
                } else {
                    taskList.add(newTask)
                    taskAdapter.notifyItemInserted(taskList.size - 1)
                }
            }
    }
}
