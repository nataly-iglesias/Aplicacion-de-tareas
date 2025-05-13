package com.equipouno.aplicaciondetareas.tasks

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.equipouno.aplicaciondetareas.R

class TaskReminderReceiver {

    fun sendNotification(context: Context, title: String, deadline: String) {
        val channelId = "task_notifications"
        val notificationId = System.currentTimeMillis().toInt()

        // Crear el canal de notificación (solo para Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de tareas"
            val descriptionText = "Notificaciones para tareas próximas a vencer"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Construir la notificación
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Tarea próxima: $title")
            .setContentText("Fecha límite: $deadline")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        Log.d("DB_TEST", "Notificación creada con éxito")

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}

