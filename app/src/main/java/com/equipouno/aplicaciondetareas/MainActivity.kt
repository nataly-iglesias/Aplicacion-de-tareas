package com.equipouno.aplicaciondetareas

import CompletedTasksFragment
import PendingTasksFragment
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.lifecycle.lifecycleScope
import com.equipouno.aplicaciondetareas.tasks.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import android.util.Log

//Importación de búsqueda
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager




// Importaciones para la animación de confetti
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.TimeUnit
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var pagerAdapter: TasksPagerAdapter
    lateinit var db: AppDatabase
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchAdapter: TaskAdapter
    private val searchResults = mutableListOf<Task>()


    // Inicializa las listas de tareas pendientes y completadas
    val pendingTasks = mutableListOf<Task>()
    val completedTasks = mutableListOf<Task>()



    private val addTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            data?.let {
                val title = it.getStringExtra("title") ?: ""
                val description = it.getStringExtra("description") ?: ""
                val deadline = it.getStringExtra("deadline") ?: ""
                val priority = it.getStringExtra("priority") ?: ""
                val category = it.getStringExtra("category") ?: ""
                val recurrence = it.getStringExtra("recurrence") ?: ""

                val newTask = Task(
                    title = title,
                    description = description,
                    deadline = deadline,
                    priority = priority,
                    category = category,
                    recurrence = recurrence,
                    isCompleted = false
                )
                pendingTasks.add(newTask)
                insertTask(newTask)
                pagerAdapter.refreshFragments(pendingTasks, completedTasks)
            }
        }
    }

    // Función para crear la actividad principal y la búsqueda
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa el SearchView
        val searchView = findViewById<SearchView>(R.id.search_view)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                // No es necesario hacer nada aqui si solo se quiere buscar en tiempo real
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText ?: ""

                if (query.isNotEmpty()) {
                    val filteredPending = pendingTasks.filter { it.title.contains(query, ignoreCase = true) }
                    val filteredCompleted = completedTasks.filter { it.title.contains(query, ignoreCase = true) }

                    searchResults.clear()
                    searchResults.addAll(filteredPending + filteredCompleted)
                    searchAdapter.notifyDataSetChanged()

                    // Mostrar solo los resultados de búsqueda
                    searchRecyclerView.visibility = View.VISIBLE
                    viewPager.visibility = View.GONE
                    tabLayout.visibility = View.GONE
                } else {
                    // Restaurar la vista normal
                    searchRecyclerView.visibility = View.GONE
                    viewPager.visibility = View.VISIBLE
                    tabLayout.visibility = View.VISIBLE
                }

                return true
            }

        })

        //Inicializa RecyclerView
        searchRecyclerView = findViewById(R.id.recycler_search_results)
        searchAdapter = TaskAdapter(searchResults, { task ->
            // Aquí decides qué hacer al marcar como completado en modo búsqueda
            if (!task.isCompleted) {
                moveTaskCompleted(task)
            }
        }, { task ->
            deleteTask(task)
        }, showConfetti = true)

        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        searchRecyclerView.adapter = searchAdapter


        // Inicializa las vistas
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
        fabAddTask = findViewById(R.id.fab_add_task)

        // Inicializa el adaptador con las listas de tareas
        pagerAdapter = TasksPagerAdapter(this, pendingTasks, completedTasks) { task ->
            deleteTask(task)
        }
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Pendientes" else "Completadas"
        }.attach()

        // Maneja el click del botón para agregar una nueva tarea
        fabAddTask.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            addTaskLauncher.launch(intent)
        }

        // Inicializa la base de datos Room
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "tasks-database"
        ).build()

        loadTasks() // Carga las tareas desde la base de datos al iniciar la actividad

        // Configura el BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    true
                }
                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        //Clasificación por Prioridad --> Flitro

        val prioritySpinner = findViewById<Spinner>(R.id.spinner_priority)
        val filterContainer = findViewById<LinearLayout>(R.id.filter_container)
        val spinnerPriority = findViewById<Spinner>(R.id.spinner_priority)

        filterContainer.setOnClickListener {
            spinnerPriority.performClick()
        }

        prioritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                val filteredPending = if (selected == "Todas") {
                    pendingTasks
                } else {
                    pendingTasks.filter { it.priority.equals(selected, ignoreCase = true) }
                }

                val filteredCompleted = if (selected == "Todas") {
                    completedTasks
                } else {
                    completedTasks.filter { it.priority.equals(selected, ignoreCase = true) }
                }

                // Actualiza fragmentos
                pagerAdapter.refreshFragments(filteredPending, filteredCompleted)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


    }

    // Carga las tareas pendientes desde la base de datos
    private fun loadTasks() {
        lifecycleScope.launch {
            val pending = withContext(Dispatchers.IO) {
                db.taskDao().getPendingTasks().first()
            }
            val completed = withContext(Dispatchers.IO) {
                db.taskDao().getCompletedTasks().first()
            }

            pendingTasks.clear()
            pendingTasks.addAll(pending)

            completedTasks.clear()
            completedTasks.addAll(completed)

            pagerAdapter.refreshFragments(pendingTasks, completedTasks)
            processRecurringTasks()
        }
    }

    // Función para procesar las tareas recurrentes
    private fun processRecurringTasks() {
        lifecycleScope.launch {
            val completed = withContext(Dispatchers.IO) {
                db.taskDao().getCompletedTasks().first()
            }

            for (task in completed) {
                if (!task.recurrence.isNullOrEmpty()) {
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy")
                    val oldDate = sdf.parse(task.deadline) ?: continue
                    val calendar = java.util.Calendar.getInstance()
                    calendar.time = oldDate

                    // Calcula la nueva fecha según la recurrencia
                    when (task.recurrence) {
                        "Diaria" -> calendar.add(java.util.Calendar.DATE, 1)
                        "Semanal" -> calendar.add(java.util.Calendar.DATE, 7)
                        "Mensual" -> calendar.add(java.util.Calendar.MONTH, 1)
                        else -> continue
                    }

                    val newDeadline = sdf.format(calendar.time)

                    // Verifica si ya existe una tarea con la misma fecha de vencimiento
                    val existingTask = withContext(Dispatchers.IO) {
                        db.taskDao().getTaskByDeadline(newDeadline)
                    }

                    if (existingTask == null) {
                        // Si no existe, crea la nueva tarea
                        val newTask = task.copy(
                            id = java.util.UUID.randomUUID().toString(),
                            isCompleted = false,
                            deadline = newDeadline
                        )
                        withContext(Dispatchers.IO) {
                            db.taskDao().insert(newTask)
                        }
                        // Actualiza la lista local
                        pendingTasks.add(newTask)

                        Log.d("TaskRecurrence", "Procesando tarea recurrente: ${task.title} con recurrencia: ${task.recurrence}")
                        Log.d("TaskRecurrence", "Nueva fecha de vencimiento: $newDeadline")
                    } else {
                        Log.d("TaskRecurrence", "La tarea ya existe para la fecha: $newDeadline")
                    }
                }
            }

            // Refresca las pestañas
            pagerAdapter.refreshFragments(pendingTasks, completedTasks)
        }
    }

    // Inserta una tarea de manera asincrónica en la base de datos
    private fun insertTask(task: Task) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.taskDao().insert(task)
            }
        }
    }

    // Mueve una tarea de pendientes a completadas
    fun moveTaskCompleted(task: Task) {
        pendingTasks.remove(task)
        completedTasks.add(task)

        task.isCompleted = true
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.taskDao().update(task) // Actualiza la tarea en la base de datos
            }
        }
        showConfetti() // Muestra el efecto de confetti
        pagerAdapter.refreshFragments(pendingTasks, completedTasks)
    }

    // Elimina una tarea de la base de datos y actualiza las listas
    fun deleteTask(task: Task) {
        pendingTasks.remove(task)
        completedTasks.remove(task)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.taskDao().delete(task)
            }
        }

        // Actualiza el adaptador para reflejar los cambios
        pagerAdapter.refreshFragments(pendingTasks, completedTasks)
    }

    // Función que filtra las tareas por título y actualiza las listas en el adaptador
    private fun filterTasks(query: String?) {
        val filteredPendingTasks = if (query.isNullOrEmpty()) {
            pendingTasks
        } else {
            pendingTasks.filter { it.title.contains(query, ignoreCase = true) }
        }

        val filteredCompletedTasks = if (query.isNullOrEmpty()) {
            completedTasks
        } else {
            completedTasks.filter { it.title.contains(query, ignoreCase = true) }
        }

        pagerAdapter.updateTasks(filteredPendingTasks, filteredCompletedTasks)
    }

    //Función para mostrar el efecto de confetti
    fun showConfetti() {
        val konfettiView = findViewById<nl.dionsegijn.konfetti.xml.KonfettiView>(R.id.konfettiView)
        konfettiView.visibility = View.VISIBLE

        konfettiView.start(
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                position = Position.Relative(0.5, 0.3),
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)
            )
        )

        Handler(Looper.getMainLooper()).postDelayed({
            konfettiView.visibility = View.GONE
        }, 3000)
    }


}


