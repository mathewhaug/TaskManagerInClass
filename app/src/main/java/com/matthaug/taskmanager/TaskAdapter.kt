package com.matthaug.taskmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.matthaug.taskmanager.models.Task

class TaskAdapter(
    private val listener: TaskItemListener
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) { //inherit from ListAdapter


    interface TaskItemListener {
        fun onEditClick(task: Task)
        fun onDeleteClick(task: Task)
        fun onItemClick(task: Task) //details functionality
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        //No longer need list
        holder.bind(getItem(position))
    }

//    override fun getItemCount(): Int = taskList.size

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskName: TextView = itemView.findViewById(R.id.taskNameTextView)
        private val taskDueDate: TextView = itemView.findViewById(R.id.taskDueDateTextView)
        private val taskPriority: TextView = itemView.findViewById(R.id.taskPriorityTextView)
        private val editButton: Button = itemView.findViewById(R.id.editButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(task: Task) {
            taskName.text = task.name
            taskDueDate.text = task.dueDate
            taskPriority.text = task.priority

            // Handle Edit button click
            editButton.setOnClickListener {
                listener.onEditClick(task)
            }

            // Handle Delete button click
            deleteButton.setOnClickListener {
                listener.onDeleteClick(task)
            }

            itemView.setOnClickListener {
                listener.onItemClick(task)
            }

        }
    }
    //Check item contents
    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem == newItem
    }
}
