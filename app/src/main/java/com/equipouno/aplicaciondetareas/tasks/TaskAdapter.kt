package com.equipouno.aplicaciondetareas.tasks

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import com.equipouno.aplicaciondetareas.R
import androidx.appcompat.app.AlertDialog
import android.widget.Button
import android.widget.Toast
import com.equipouno.aplicaciondetareas.MainActivity

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskToggle: (Task) -> Unit,
    private val onTaskDelete: (Task) -> Unit,
    private val showConfetti: Boolean
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivComplete: ImageView = itemView.findViewById(R.id.iv_task_complete)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_task_title)
        val tvDeadline: TextView = itemView.findViewById(R.id.tv_task_deadline)
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete)


        fun bind(task: Task) {
            tvTitle.text = task.title
            tvDeadline.text = task.deadline

            // Mostrar detalles de la tarea al hacer click
            itemView.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle(task.title)
                    .setMessage("Descripción: ${task.description}\n\nFecha límite: ${task.deadline}\n\nPrioridad: ${task.priority}\n\nCategoría: ${task.category}")
                    .setPositiveButton("Cerrar", null)
                    .show()
            }

            // Cambiar el icono de completado de la tarea
            ivComplete.setImageResource(
                if (task.isCompleted) R.drawable.circle_filled else R.drawable.circle_empty
            )

            // Cambiar al estado de completado
            ivComplete.setOnClickListener {
                val wasCompleted = task.isCompleted
                task.isCompleted = !task.isCompleted
                notifyItemChanged(adapterPosition)
                onTaskToggle(task)

                // Mostrar mensaje y confetti si la tarea se completa
                if (!wasCompleted && task.isCompleted) {
                    val messages = listOf(
                        "¡Buen trabajo! Otra tarea terminada ✅",
                        "¡Vas increíble! Nueva tarea completada 💪",
                        "¡Excelente! Tarea completada \uD83C\uDF1F",
                        "¡Felicidades! Completaste una tarea 🎉"
                    )
                    val randomMessage = messages.random() // Selecciona un mensaje al azar
                    val activity = itemView.context as? MainActivity
                    activity?.showConfetti()

                    Toast.makeText(itemView.context, randomMessage, Toast.LENGTH_SHORT).show()
                }
            }

            // Botón de eliminar tarea
            btnDelete.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Eliminar tarea")
                    .setMessage("¿Estás seguro de que deseas eliminar esta tarea?")
                    .setPositiveButton("Confirmar") { dialog, _ ->
                        onTaskDelete(task)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            //Boton de editar tarea
            btnEdit.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, EditTaskActivity::class.java).apply {
                    putExtra("task_id", task.id)
                    putExtra("task_title", task.title)
                    putExtra("task_description", task.description)
                    putExtra("task_deadline", task.deadline)
                    putExtra("task_priority", task.priority)
                    putExtra("task_category", task.category)
                    putExtra("task_recurrence", task.recurrence)
                }
                context.startActivity(intent)
            }

            // Deshabilitar los botones si la tarea está completada
            if (task.isCompleted) {
                btnEdit.visibility = View.GONE
                btnDelete.visibility = View.GONE
            } else {
                btnEdit.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    // Devolver el número de elementos en la lista de tareas
    override fun getItemCount(): Int = tasks.size
}
