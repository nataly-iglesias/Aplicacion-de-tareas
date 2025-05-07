package com.equipouno.aplicaciondetareas

import android.app.Application
import androidx.room.Room
import com.equipouno.aplicaciondetareas.tasks.AppDatabase

class MyApplication : Application() {

    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()

        // Inicializa la base de datos
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "tasks_database"  // Nombre del archivo de la base de datos
        ).build()
    }
}
