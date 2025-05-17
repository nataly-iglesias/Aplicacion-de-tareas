import com.equipouno.aplicaciondetareas.tasks.Task
import com.equipouno.aplicaciondetareas.tasks.TaskAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.equipouno.aplicaciondetareas.MainActivity
import com.equipouno.aplicaciondetareas.R
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.lifecycle.asLiveData

class PendingTasksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private val pendingTasks = mutableListOf<Task>()
    var onTaskDelete: ((Task) -> Unit)? = null

    // Método para actualizar la lista desde el exterior
    fun updateTaskList(tasks: List<Task>) {
        pendingTasks.clear()
        pendingTasks.addAll(tasks)
        adapter.notifyDataSetChanged()
    }

    companion object {
        fun newInstance(tasks: List<Task>, onTaskDelete: (Task) -> Unit): PendingTasksFragment {
            val fragment = PendingTasksFragment()
            fragment.pendingTasks.addAll(tasks) // Agrega las tareas iniciales
            fragment.onTaskDelete = onTaskDelete
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pending_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recycler_pending)

        // Inicializa el adaptador con las listas de tareas pendientes
        adapter = TaskAdapter(pendingTasks, { task ->
            (activity as? MainActivity)?.moveTaskCompleted(task)
        }, { task ->
            onTaskDelete?.invoke(task)
        }, showConfetti = true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Obtiene las tareas pendientes desde la base de datos
        (activity as? MainActivity)?.let { main ->
            main.db.taskDao().getPendingTasks().asLiveData().observe(viewLifecycleOwner) { tasks: List<Task> ->
                pendingTasks.clear()
                pendingTasks.addAll(tasks)
                adapter.notifyDataSetChanged()
            }
        }
    }
}
