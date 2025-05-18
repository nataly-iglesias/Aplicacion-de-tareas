package com.equipouno.aplicaciondetareas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray
import android.content.SharedPreferences
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Switch
import android.widget.Spinner
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat

// Importaciones para el sonido
import android.media.MediaPlayer

class SettingsActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var listView: ListView
    private lateinit var etNewCategory: EditText
    private lateinit var btnAddCategory: Button
    private lateinit var adapter: CategoryAdapter
    private val categories = mutableListOf<String>()

    private lateinit var switchNotificaciones: Switch
    private lateinit var spinnerTiempoRecordatorio: Spinner
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        // Referencias
        listView = findViewById(R.id.lv_categories)
        etNewCategory = findViewById(R.id.et_newCategory)
        btnAddCategory = findViewById(R.id.btn_add_category)

        val etUserName = findViewById<EditText>(R.id.et_nameUser)
        val btnSaveName = findViewById<Button>(R.id.btn_save_name)

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        val savedCategoriesJson = prefs.getString("categorias", null)
        if (savedCategoriesJson != null) {
            val jsonArray = JSONArray(savedCategoriesJson)
            for (i in 0 until jsonArray.length()) {
                categories.add(jsonArray.getString(i))
            }
        } else {
            // Carga inicial
            categories.addAll(listOf("🧘 Personal", "💻 Trabajo", "🎓 Escuela"))
        }

        // Configurar adaptador para el listado de categorías
        adapter = CategoryAdapter(this, categories) { categoriaAEliminar ->
            categories.remove(categoriaAEliminar)
            adapter.notifyDataSetChanged()
            val updatedJson = JSONArray(categories).toString()
            prefs.edit().putString("categorias", updatedJson).apply()
            Toast.makeText(this, "Categoría eliminada", Toast.LENGTH_SHORT).show()
        }
        listView.adapter = adapter

        // Agregar nueva categoría
        btnAddCategory.setOnClickListener {
            val newCategory = etNewCategory.text.toString().trim()
            if (newCategory.isNotEmpty() && !categories.contains(newCategory)) {
                categories.add(newCategory)
                adapter.notifyDataSetChanged()
                etNewCategory.text.clear()

                // Ocultar el teclado
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(etNewCategory.windowToken, 0)

                val  updatedJson = JSONArray(categories).toString()
                prefs.edit().putString("categorias",  updatedJson).apply()
                Toast.makeText(this, "Categoría agregada ✨!", Toast.LENGTH_SHORT).show()
            }
            vibrate()
            playCompleteTaskSound()
        }

        // Cargar nombre de usuario
        etUserName.setText(prefs.getString("nombre_usuario", ""))

        // Guardar nombre de usuario
        btnSaveName.setOnClickListener {
            val userName = etUserName.text.toString().trim()
            if (userName.isNotEmpty()) {
                prefs.edit().putString("nombre_usuario", userName).apply()
                Toast.makeText(this, "Nombre guardado ✨!", Toast.LENGTH_SHORT).show()

                // Ocultar el teclado
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(etUserName.windowToken, 0)
            } else {
                Toast.makeText(this, "Ingresa un nombre válido", Toast.LENGTH_SHORT).show()
            }
            vibrate()
            playCompleteTaskSound()
        }

        // Configura el BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_settings

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    true
                }
                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    true
                }
                else -> false
            }
        }

        //Inicializamos las vistas
        switchNotificaciones = findViewById(R.id.switchNotificaciones)
        spinnerTiempoRecordatorio = findViewById(R.id.spinnerTiempoRecordatorio)

        val notificationsEnabled = sharedPreferences.getBoolean("notificationsEnabled", false) // Si las notificaciones están habilitadas
        val reminderTime = sharedPreferences.getString("reminderTime", "15 minutos") // Tiempo de recordatorio guardado (por defecto "15 minutos")


        switchNotificaciones.isChecked = notificationsEnabled // Si las notificaciones están habilitadas, el switch estará encendido
        val reminderPosition = resources.getStringArray(R.array.tiempos_recordatorio).indexOf(reminderTime) // Obtenemos la posición del tiempo de recordatorio
        spinnerTiempoRecordatorio.setSelection(reminderPosition) // Establecemos el tiempo de recordatorio en el spinner

        switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            // Guardamos el estado del switch (habilitado/deshabilitado) en las preferencias
            sharedPreferences.edit().putBoolean("notificationsEnabled", isChecked).apply()
        }

        var isFirstSelection = true
        spinnerTiempoRecordatorio.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedTime = parent.getItemAtPosition(position).toString()
                sharedPreferences.edit().putString("reminderTime", selectedTime).apply()

                if (!isFirstSelection) {
                    Toast.makeText(this@SettingsActivity, "Recordatorio ajustado a $selectedTime", Toast.LENGTH_SHORT).show()
                }
                isFirstSelection = false
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                // No hacer nada
            }
        })
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

    //Función para reproducir sonido al completar una tarea
    private fun playCompleteTaskSound() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.complete_task_sound)
        mediaPlayer.start()
    }
}