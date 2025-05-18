package com.equipouno.aplicaciondetareas.tasks
import android.content.Context
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.equipouno.aplicaciondetareas.Task2
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private lateinit var sharedPreferences: SharedPreferences

override fun doWork(): Result {
        Log.d("MyWorker", "Ejecutando tarea cada 15 minutos")
        val db = AppDatabase.getDatabase(applicationContext)


    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)


        val notificationsEnabled = sharedPreferences.getBoolean("notificationsEnabled", true)
        if (notificationsEnabled) {

        val reminderTime = sharedPreferences.getString("reminderTime", "15 minutos") ?: "15 minutos"
        val Tiempo = parseReminderTime(reminderTime!!)

        runBlocking {
            val pendingTasks = db.taskDao().getPendingTasks().first()
            val tasksToShow = mutableListOf<Task2>()

            for (task in pendingTasks) {
                val sdf2 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf2.timeZone = TimeZone.getTimeZone("America/Mexico_City")
                var deadlineMillis = 0L

                try {
                    val deadlineDate = sdf2.parse(task.deadline)
                    deadlineMillis = deadlineDate?.time ?: 0L
                    Log.d("MyWorker", "Fecha límite (sin hora) en milisegundos: $deadlineMillis")
                } catch (e: ParseException) {
                    Log.e("DB_TEST", "Error al convertir fecha límite", e)
                }

                val mexicoColimaTimeZone = TimeZone.getTimeZone("America/Tijuana")
                val calendar = Calendar.getInstance(mexicoColimaTimeZone)
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                sdf.timeZone = mexicoColimaTimeZone
                val currentDate = sdf.format(calendar.time)
                val currentMillis = calendar.timeInMillis

                if (currentMillis > deadlineMillis - Tiempo) {
                    Log.d("MyWorker","el current Millis es ${currentMillis} y el deadlinemillis es ${deadlineMillis} y el tiempo es ${Tiempo}")
                    //Los datos task.title y task.deadline se llevaran como notificaciones externa en la parte de la bandeja
                    val reminder = TaskReminderReceiver()
                    reminder.sendNotification(applicationContext, task.title, task.deadline)
                }
            }
        }}
        return Result.success()
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
