package com.equipouno.aplicaciondetareas

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import android.app.AlarmManager
import android.provider.Settings



// Importaciones para la animación de confetti
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
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

    // Función para crear la actividad principal
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            try {
                val canSchedule = alarmManager.canScheduleExactAlarms()
                if (!canSchedule) {
                    Toast.makeText(this, "La app no puede programar alarmas exactas", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                    return
                }
            } catch (e: SecurityException) {
                Log.e("AlarmPermission", "Error al verificar permiso de alarmas exactas", e)
                Toast.makeText(this, "Error al verificar permiso de alarmas", Toast.LENGTH_SHORT).show()
            }
        }


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


