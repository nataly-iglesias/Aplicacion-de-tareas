package com.equipouno.aplicaciondetareas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import android.content.SharedPreferences
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.TextView
import java.util.Locale
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import com.equipouno.aplicaciondetareas.tasks.AppDatabase
import androidx.lifecycle.lifecycleScope
import java.util.TimeZone
import java.util.*
import java.text.SimpleDateFormat
import java.text.ParseException
import androidx.recyclerview.widget.RecyclerView
import com.equipouno.aplicaciondetareas.tasks.TaskAdapterday
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import android.os.Build
import android.app.AlarmManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.app.NotificationManager
import android.app.NotificationChannel
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.equipouno.aplicaciondetareas.R
import android.app.PendingIntent
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import com.equipouno.aplicaciondetareas.tasks.TaskReminderReceiver
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit
import com.equipouno.aplicaciondetareas.tasks.MyWorker

// Importaciones para el sonido
import android.media.MediaPlayer

data class Task2(
    val title: String,
    val deadline: String
)

class NotificationsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notifications)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            } else {
                Log.d("DB_TEST","Ya tienes permiso, puedes mantener tus notificaciones")
            }
        } else {
            Log.d("DB_TEST", "No necesitas permiso, para verSiones menores que android 13")
        }

        val workRequest = PeriodicWorkRequestBuilder<MyWorker>(15, TimeUnit.MINUTES).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MyPeriodicTask",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

    val db = AppDatabase.getDatabase(applicationContext)

    val statusTextView = findViewById<TextView>(R.id.notifications_status_text)
    //var pendingTask = mutableListOf<Task>()

    // Obtenemos las preferencias compartidas
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

    val notificationsEnabled = sharedPreferences.getBoolean("notificationsEnabled", true)
    if (notificationsEnabled) {

        val reminderTime = sharedPreferences.getString("reminderTime", "15 minutos") ?: "15 minutos"
        val Tiempo = parseReminderTime(reminderTime!!)

        lifecycleScope.launch {
            val pendingTasks = withContext(Dispatchers.IO) {
                db.taskDao().getPendingTasks().first()
            }

            val tasksToShow = mutableListOf<Task2>()

            for (task in pendingTasks) {
                Log.d("DB_TEST", "Tarea pendiente: ${task.title} - ${task.deadline}")
                var fecha_limite = task.deadline


                val sdf2 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf2.timeZone = TimeZone.getTimeZone("America/Mexico_City")

                var deadlineMillis = 0L

                try {
                    val deadlineDate = sdf2.parse(task.deadline)
                    deadlineMillis = deadlineDate?.time ?: 0L
                    Log.d("DB_TEST", "Fecha límite (sin hora) en milisegundos: $deadlineMillis")
                } catch (e: ParseException) {
                    Log.e("DB_TEST", "Error al convertir fecha límite", e)
                }

                val mexicoColimaTimeZone = TimeZone.getTimeZone("America/Tijuana")

                val calendar = Calendar.getInstance(mexicoColimaTimeZone)
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                sdf.timeZone = mexicoColimaTimeZone
                val currentDate = sdf.format(calendar.time)
                val currentMillis = calendar.timeInMillis
                Log.d("DB_TEST", "Hora actual en Colima: $currentDate")
                Log.d("DB_TEST", "Hora actual en milisegundos: $currentMillis")

                if (currentMillis > deadlineMillis - Tiempo) {
                    Log.d(
                        "DB_TEST",
                        "Tarea pendiente esta proxima a entregarse: ${task.title} - ${task.deadline}"
                    )
                    Log.d("DB_TEST", "H")
                    tasksToShow.add(Task2(task.title, task.deadline))
                    //Llevar los datos de task.title y task.deadline a la clase TaskReminderreceiver, esta se encarga de mandar notificaciones
                } else {
                    Log.d("DB_TEST", "No se entrega")

                }
            }
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewNotifications)
            val adapter = TaskAdapterday(tasksToShow)
            recyclerView.layoutManager = LinearLayoutManager(this@NotificationsActivity)
            recyclerView.adapter = adapter
        }
    } else {
        statusTextView.text = "Notificaciones inactivas"
    }

        // Referencia a BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Establecer el ítem seleccionado como Notificaciones
        bottomNavigationView.selectedItemId = R.id.nav_notifications

        // Configura el listener para cambiar de pantalla
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_notifications -> true // Ya estás en esta
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

        // Animación del gato: lamerse
        val catImageView = findViewById<ImageView>(R.id.catView)
        catImageView.setBackgroundResource(R.drawable.cat_sitting)

        val catAnimation = catImageView.background as AnimationDrawable
        catAnimation.start()

        // Mensaje del gato
        // Lista de mensajes aleatorios del gato
        val mensajesGato = listOf(
            "¡Miau! Revisa tus pendientes. 🐾",
            "¡Hola! ¿Ya terminaste tus tareas?",
            "Estoy vigilando tus pendientes... 🐱",
            "¡Vamos! ¡A trabajar se ha dicho!",
        )

        // Seleccionar un mensaje aleatorio
        val mensajeAleatorio = mensajesGato.random()

        // Mostrarlo en el TextView
        val catMessage = findViewById<TextView>(R.id.catMessage)
        catMessage.text = mensajeAleatorio
        catMessage.visibility = View.VISIBLE
        catMessage.setBackgroundResource(R.drawable.bubble_background)
        catMessage.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        catMeow()

        // Ocultar el mensaje después de 3 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            catMessage.visibility = View.GONE
        }, 3000)
    }

    //Función para reproducir sonido del gato
    private fun catMeow() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.meow)
        mediaPlayer.start()
    }

    private fun parseReminderTime(reminderTime: String): Long {
        return when (reminderTime.lowercase(Locale.getDefault())) {
            "15 minutos" -> 15 * 60 * 1000
            "1 hora antes" -> 60 * 60 * 1000
            "6 horas antes" -> 60 * 60 * 6 * 1000
            "12 horas antes" -> 60 * 60 * 12 * 1000
            "2 días antes" -> 60 * 60 * 48 * 1000
            "1 día antes" -> 24 * 60 * 60 * 1000
            "1 semana antes" -> 7 * 24 * 60 * 60 * 1000
            else -> 15 * 60 * 1000
        }
    }
}