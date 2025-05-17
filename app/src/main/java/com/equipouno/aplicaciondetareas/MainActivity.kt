package com.equipouno.aplicaciondetareas

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
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
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import PendingTasksFragment
import CompletedTasksFragment

// Importaciones para la vibración
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer

//Importaciones para la búsqueda
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager

// Importaciones para la animación de confetti
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
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

    // Variables para la búsqueda
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchAdapter: TaskAdapter
    private val searchResults = mutableListOf<Task>()

    // Variables para el filtro de categorías
    private lateinit var spinnerCategory: Spinner
    private val allCategories = mutableListOf<String>()

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

        // Obtiene el nombre del usuario y lo muestra en el saludo
        val tvSaludo = findViewById<TextView>(R.id.tv_saludo)
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val nombre = prefs.getString("nombre_usuario", "")
        if (!nombre.isNullOrEmpty()) {
            tvSaludo.text = "Hola $nombre ⭐!"
        } else {
            tvSaludo.text = "Hola ⭐!"
        }

        // Inicializa el botón de búsqueda y el contenedor de búsqueda
        val toggleSearchButton = findViewById<ImageButton>(R.id.btn_toggle_search)
        val searchFilterContainer = findViewById<LinearLayout>(R.id.search_filter_container)
        var isSearchVisible = false

        toggleSearchButton.setOnClickListener {
            isSearchVisible = !isSearchVisible
            searchFilterContainer.visibility = if (isSearchVisible) View.VISIBLE else View.GONE
        }

        // Inicializa el SearchView
        val searchView = findViewById<SearchView>(R.id.search_view)
        searchView.isSubmitButtonEnabled = false
        searchView.isIconified = false
        searchView.clearFocus()
        searchView.queryHint = "Buscar tarea"
        searchView.maxWidth = Integer.MAX_VALUE //

        // Configura el listener para manejar los cambios en el texto de búsqueda
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
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


        //Filtro categorias
        spinnerCategory = findViewById(R.id.spinner_category)
        allCategories.add("Todas")

        // Carga las categorías desde las tareas existentes
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val tasks = db.taskDao().getAllTasks().first()
                val categories = tasks.map { it.category }.distinct()
                allCategories.addAll(categories)
            }
            val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, allCategories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter
        }

        // Maneja el cambio de selección en el Spinner
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = parent.getItemAtPosition(position).toString()

                // Filtra las tareas según la categoría seleccionada
                lifecycleScope.launch {
                    val allPending = withContext(Dispatchers.IO) {
                        db.taskDao().getPendingTasks().first()
                    }
                    val allCompleted = withContext(Dispatchers.IO) {
                        db.taskDao().getCompletedTasks().first()
                    }

                    val filteredPending = if (selectedCategory == "Todas") {
                        allPending
                    } else {
                        allPending.filter { it.category == selectedCategory }
                    }

                    val filteredCompleted = if (selectedCategory == "Todas") {
                        allCompleted
                    } else {
                        allCompleted.filter { it.category == selectedCategory }
                    }
                    pagerAdapter.refreshFragments(filteredPending, filteredCompleted)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


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

        // Inicia la animación del gatito
        val catView = findViewById<ImageView>(R.id.catView)
        val catAnimation = catView.drawable as AnimationDrawable
        catAnimation.start()

        // Esperar a que se dibuje el layout para obtener el ancho
        catView.post {
            val parentWidth = (catView.parent as View).width
            val catWidth = catView.width

            val startX = 0f
            val endX = parentWidth - catWidth - 150f // 150 para no tapar el botón "más"

            val animator = ObjectAnimator.ofFloat(catView, "translationX", startX, endX).apply {
                duration = 6000
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE

                addUpdateListener { animation ->
                    val currentPosition = animation.animatedValue as Float
                    if (currentPosition == endX) {
                        catView.setImageResource(R.drawable.cat_walk_reverse) // Cambia la imagen
                        val catReverseAnimation = catView.drawable as AnimationDrawable
                        catReverseAnimation.start()
                    } else if (currentPosition == startX) {
                        catView.setImageResource(R.drawable.cat_walk) // Cambia la imagen
                        val catWalkAnimation = catView.drawable as AnimationDrawable
                        catWalkAnimation.start()
                    }
                }
            }
            animator.start()
        }

        // Reproducir sonido al hacer clic en el gatito
        catView.setOnClickListener {
            val mediaPlayer = MediaPlayer.create(this, R.raw.meow)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }

            val catView = findViewById<ImageView>(R.id.catView)
            catView.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(300)
                .withEndAction {
                    catView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .start()
                }
        }

        //Clasificación por prioridad - > Filtro
        val prioritySpinner = findViewById<Spinner>(R.id.spinner_priority)
        val filterContainer = findViewById<LinearLayout>(R.id.filter_container)

        filterContainer.setOnClickListener {
            prioritySpinner.performClick()
        }

        prioritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()

                lifecycleScope.launch {
                    //Obtiene todas las tareas pendientes y completadas
                    val allPendingTasks = withContext(Dispatchers.IO) {
                        db.taskDao().getPendingTasks().first()
                    }
                    val allCompletedTasks = withContext(Dispatchers.IO) {
                        db.taskDao().getCompletedTasks().first()
                    }

                    // Filtra las tareas según la prioridad seleccionada
                    val filteredPending = if (selected.lowercase().contains("todas")) {
                        allPendingTasks
                    } else {
                        allPendingTasks.filter { it.priority.equals(selected.trim(), ignoreCase = true) }
                    }

                    val filteredCompleted = if (selected.lowercase().contains("todas")) {
                        allCompletedTasks
                    } else {
                        allCompletedTasks.filter { it.priority.equals(selected.trim(), ignoreCase = true) }
                    }
                    pagerAdapter.refreshFragments(filteredPending, filteredCompleted)
                }
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
        android.widget.Toast.makeText(this, "¡Tarea eliminada 🗑️!", android.widget.Toast.LENGTH_SHORT).show()
        // Actualiza el adaptador para reflejar los cambios
        pagerAdapter.refreshFragments(pendingTasks, completedTasks)
        vibrate()
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

    // Función para vibrar el dispositivo
    fun vibrate(){
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }
}