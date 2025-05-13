package com.equipouno.aplicaciondetareas.tasks
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.equipouno.aplicaciondetareas.Task2
import com.equipouno.aplicaciondetareas.R
import android.util.Log



class TaskAdapterday(private val tasks: List<Task2>) : RecyclerView.Adapter<TaskAdapterday.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task2, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        Log.d("ADAPTER_TEST", "onBindViewHolder: Mostrando tarea ${task.title} con fecha ${task.deadline}")
        holder.titleTextView.text = task.title
        holder.deadlineTextView.text = task.deadline
    }

    override fun getItemCount(): Int {
        Log.d("ADAPTER_TEST", "getItemCount: ${tasks.size} elementos")
        return tasks.size
    }

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.task_title)
        val deadlineTextView: TextView = view.findViewById(R.id.task_deadline)
    }
}
