package com.equipouno.aplicaciondetareas

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Switch
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchNotificaciones: Switch
    private lateinit var spinnerTiempoRecordatorio: Spinner
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)


        //Inicializamos las vistas
        switchNotificaciones = findViewById(R.id.switchNotificaciones)
        spinnerTiempoRecordatorio = findViewById(R.id.spinnerTiempoRecordatorio)

        // Obtenemos las preferencias compartidas
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)


        val notificationsEnabled = sharedPreferences.getBoolean("notificationsEnabled", false) // Si las notificaciones están habilitadas
        val reminderTime = sharedPreferences.getString("reminderTime", "15 minutos") // Tiempo de recordatorio guardado (por defecto "15 minutos")


        switchNotificaciones.isChecked = notificationsEnabled // Si las notificaciones están habilitadas, el switch estará encendido
        val reminderPosition = resources.getStringArray(R.array.tiempos_recordatorio).indexOf(reminderTime) // Obtenemos la posición del tiempo de recordatorio
        spinnerTiempoRecordatorio.setSelection(reminderPosition) // Establecemos el tiempo de recordatorio en el spinner

        switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            // Guardamos el estado del switch (habilitado/deshabilitado) en las preferencias
            sharedPreferences.edit().putBoolean("notificationsEnabled", isChecked).apply()
        }


        spinnerTiempoRecordatorio.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedTime = parent.getItemAtPosition(position).toString()
                sharedPreferences.edit().putString("reminderTime", selectedTime).apply()

                // (Opcional) Mostrar al usuario que se guardó
                Toast.makeText(this@SettingsActivity, "Recordatorio ajustado a $selectedTime", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                // No hacer nada
            }
        })





    }
}
