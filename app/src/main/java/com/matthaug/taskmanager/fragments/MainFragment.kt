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
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.matthaug.taskmanager.R
import com.matthaug.taskmanager.TaskAdapter
import com.matthaug.taskmanager.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay

// Add this to the top of MainFragment
private val fragmentJob = SupervisorJob()
private val fragmentScope = CoroutineScope(Dispatchers.Main + fragmentJob)

// file name where we are saving tasks
private const val FILE_NAME = "tasks.json"
class MainFragment : Fragment(), TaskAdapter.TaskItemListener {

    //Needed for updating file io to be able to cancel coroutines
    private val fragmentJob = SupervisorJob()
    private val fragmentScope = CoroutineScope(Dispatchers.Main + fragmentJob)

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

        /*
        // OLD File io
        taskList.clear()
        //load files from task list
        taskList.addAll(loadTasksFromFile())
         */
        //New File IO Logic
        fragmentScope.launch {
            //Off load file io to background thread
            val loadedTasks = withContext(Dispatchers.IO) {
                loadTasksFromFile()
            }
            taskList.clear()
            taskList.addAll(loadedTasks)
            taskAdapter.notifyDataSetChanged()
            updatePlaceholderVisibility()
        }

        val addTaskButton: FloatingActionButton = view.findViewById(R.id.addTaskFab)
        addTaskButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_addTaskFragment)
        }

        //Motivate button event listener
        val motivateMeButton: Button = view.findViewById(R.id.motivateMeButton)
        motivateMeButton.setOnClickListener {
            fetchMotivationalQuote()
        }

        //Sync to cloud event listener
        val syncToCloudButton: Button = view.findViewById(R.id.syncToCloudButton)
        syncToCloudButton.setOnClickListener {
            simulateCloudSync()
        }


        //Calc cost button
        val calculateCostButton: Button = view.findViewById(R.id.calculateCostButton)
        calculateCostButton.setOnClickListener {
            calculateTotalCost()
        }
        //default tasks button
        val loadDefaultsButton: Button = view.findViewById(R.id.loadDefaultsButton)
        loadDefaultsButton.setOnClickListener {
            loadDefaultTasks()
        }



        //check if task list is empty
        updatePlaceholderVisibility()

        return view
    }
    //Function to fetch motivational quote from API
    private fun fetchMotivationalQuote() {
        //Coroutine to fetch quote
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

    //Details view function
    override fun onItemClick(task: Task) {
        val bundle = Bundle().apply {
            putString("taskName", task.name)
            putString("taskDueDate", task.dueDate)
            putString("taskPriority", task.priority)
            putBoolean("costAssociated", task.costAssociated)
            putString("currency", task.currency.toString())
            putDouble("cost", task.cost)
            putBoolean("completed", task.completed)
            putBoolean("overdue", task.overdue)
        }
        findNavController().navigate(R.id.action_mainFragment_to_taskDetailsFragment, bundle)
    }



    override fun onEditClick(task: Task) {
        val bundle = Bundle().apply {
            putInt("taskId", task.id.toInt())
            putString("taskName", task.name)
            putString("taskDueDate", task.dueDate)
            putString("taskPriority", task.priority)
            putBoolean("costAssociated", task.costAssociated)
            putString("currency", task.currency.currencyCode)
            putDouble("cost", task.cost)
            putBoolean("completed", task.completed)
            putBoolean("overdue", task.overdue)
        }
        findNavController().navigate(R.id.action_mainFragment_to_addTaskFragment, bundle)
    }


    override fun onDeleteClick(task: Task) {
        taskList.remove(task)
        taskAdapter.notifyDataSetChanged()
        saveTasksToFile()
        updatePlaceholderVisibility()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Bundle>("newTask")
            ?.observe(viewLifecycleOwner) { bundle ->

                //Remove the bundle off the backstack so we dont get dup data
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Bundle>("newTask")
                //Create a new task object from the bundle or update existing
                val newTask = Task(
                    id = bundle.getInt("taskId"),
                    name = bundle.getString("taskName") ?: "",
                    dueDate = bundle.getString("taskDueDate") ?: "",
                    priority = bundle.getString("taskPriority") ?: "",
                    costAssociated = bundle.getBoolean("costAssociated"),
                    currency = Currency.getInstance(bundle.getString("currency") ?: "CAD"),
                    cost = bundle.getDouble("cost", 0.0),
                    completed = bundle.getBoolean("completed"),
                    overdue = bundle.getBoolean("overdue")
                )

                val index = taskList.indexOfFirst { it.id.toInt() == newTask.id.toInt() }
                if (index != -1) {
                    taskList[index] = newTask
                    taskAdapter.notifyItemChanged(index)
                } else {
                    taskList.add(newTask)
                    taskAdapter.notifyItemInserted(taskList.size - 1)
                }

                saveTasksToFile()
            }
    }
    //for the you have no tasks message
    private fun updatePlaceholderVisibility() {
        val noTasksTextView = view?.findViewById<TextView>(R.id.noTasksTextView)
        noTasksTextView?.visibility = if (taskList.isEmpty()) View.VISIBLE else View.GONE
    }
    //file IO OLD
    /*
    private fun saveTasksToFile() {
        val json = Gson().toJson(taskList)
        val file = File(requireContext().filesDir, FILE_NAME)
        file.writeText(json)
    }*/
    //New File IO
    private fun saveTasksToFile() {
        fragmentScope.launch(Dispatchers.IO) {
            val json = Gson().toJson(taskList)
            val file = File(requireContext().filesDir, FILE_NAME)
            file.writeText(json)
        }
    }


    private fun loadTasksFromFile(): MutableList<Task> {
        val file = File(requireContext().filesDir, FILE_NAME)
        if (!file.exists()) return mutableListOf()

        val json = file.readText()
        val type = object : TypeToken<MutableList<Task>>() {}.type
        return Gson().fromJson(json, type)
    }
    //Calc cost function
    private fun calculateTotalCost() {
        lifecycleScope.launch {
            try { //Pass it to background thread
                val total = withContext(Dispatchers.Default) {
                    taskList
                        .filter { it.costAssociated }
                        .sumOf { it.cost }
                }

                withContext(Dispatchers.Main) {
                    if (taskList.isEmpty()) {
                        Snackbar.make(requireView(), "You have no tasks to calculate.", Snackbar.LENGTH_SHORT).show()
                    } else if (total == 0.0) {
                        Snackbar.make(requireView(), "No costs associated with your tasks.", Snackbar.LENGTH_SHORT).show()
                    } else {
                        val currencyCode = taskList.firstOrNull { it.costAssociated }?.currency?.currencyCode ?: "CAD"
                        Snackbar.make(
                            requireView(),
                            "Total cost of tasks: $currencyCode %.2f".format(total),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(requireView(), "Error calculating total cost: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }


    //Simulate cloud sync function
    private fun simulateCloudSync() {
        lifecycleScope.launch {
            Snackbar.make(requireView(), "Syncing to cloud...", Snackbar.LENGTH_SHORT).show()

            delay(2000) //Simulate network delay

            Snackbar.make(requireView(), "Tasks synced successfully!", Snackbar.LENGTH_LONG).show()
        }
    }


    //Cancel coroutines on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        fragmentJob.cancel()
    }

    //Loading default tasks
    private fun loadDefaultTasks() {
        val defaultTasks = listOf(
            Task(
                id = System.currentTimeMillis(),
                name = "Plan Groceries",
                dueDate = "26/03/2025",
                priority = "Medium",
                costAssociated = true,
                currency = Currency.getInstance("CAD"),
                cost = 50.0,
                completed = false,
                overdue = false
            ),
            Task(
                id = System.currentTimeMillis() + 1,
                name = "Pay Rent",
                dueDate = "01/04/2025",
                priority = "High",
                costAssociated = true,
                currency = Currency.getInstance("CAD"),
                cost = 1200.0,
                completed = false,
                overdue = false
            ),
            Task(
                id = System.currentTimeMillis() + 2,
                name = "Read Book",
                dueDate = "30/03/2025",
                priority = "Low",
                costAssociated = false,
                currency = Currency.getInstance("CAD"),
                cost = 0.0,
                completed = false,
                overdue = false
            )
        )

        taskList.clear()
        taskList.addAll(defaultTasks)
        taskAdapter.notifyDataSetChanged()
        saveTasksToFile()
        updatePlaceholderVisibility()
    }


}
