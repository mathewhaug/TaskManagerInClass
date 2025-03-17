import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.matthaug.taskmanager.R
import com.matthaug.taskmanager.Task
import com.matthaug.taskmanager.TaskDetailsActivity

class TaskAdapter(
    private val taskList: List<Task>,
    private val listener: TaskItemListener
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    interface TaskItemListener {
        fun onEditClick(task: Task)
        fun onDeleteClick(task: Task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = taskList.size

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskName: TextView = itemView.findViewById(R.id.taskName)
        private val taskDueDate: TextView = itemView.findViewById(R.id.taskDueDate)
        private val taskPriority: TextView = itemView.findViewById(R.id.taskPriority)
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

            // Handle task click to open TaskDetailsActivity
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, TaskDetailsActivity::class.java).apply {
                    putExtra("taskName", task.name)
                    putExtra("taskDueDate", task.dueDate)
                    putExtra("taskPriority", task.priority)
                }
                context.startActivity(intent)
            }
        }
    }
}