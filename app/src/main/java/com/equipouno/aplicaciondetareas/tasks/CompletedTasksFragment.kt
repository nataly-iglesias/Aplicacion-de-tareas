import com.equipouno.aplicaciondetareas.tasks.Task
import com.equipouno.aplicaciondetareas.tasks.TaskAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.equipouno.aplicaciondetareas.R
import com.equipouno.aplicaciondetareas.MainActivity
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class CompletedTasksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private val completedTasks = mutableListOf<Task>()
    var onTaskDelete: ((Task) -> Unit)? = null

    // Método para actualizar la lista desde el exterior
    fun updateTaskList(tasks: List<Task>) {
        completedTasks.clear()
        completedTasks.addAll(tasks)
        adapter.notifyDataSetChanged()
    }

    // Método para crear una nueva instancia del fragmento
    companion object {
        fun newInstance(tasks: List<Task>, onTaskDelete: (Task) -> Unit): CompletedTasksFragment {
            val fragment = CompletedTasksFragment()
            fragment.completedTasks.addAll(tasks)
            fragment.onTaskDelete = onTaskDelete
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_completed_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recycler_completed)

        // Inicializa el adaptador con la lógica para desmarcar tareas
        adapter = TaskAdapter(completedTasks, { taskToUncomplete ->
            taskToUncomplete.isCompleted = false

            // Actualiza la tarea en la base de datos
            (activity as? MainActivity)?.let { main ->
                main.lifecycleScope.launch {
                    main.db.taskDao().update(taskToUncomplete)
                }
            }
        }, { task ->
            onTaskDelete?.invoke(task)
        }, showConfetti = false)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Observar tareas completadas desde la base de datos
        (activity as? MainActivity)?.let { main ->
            main.db.taskDao().getCompletedTasks().asLiveData().observe(viewLifecycleOwner) { tasks ->
                completedTasks.clear()
                completedTasks.addAll(tasks)
                adapter.notifyDataSetChanged()
            }
        }
    }
}
