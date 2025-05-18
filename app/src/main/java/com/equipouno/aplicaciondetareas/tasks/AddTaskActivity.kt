package com.equipouno.aplicaciondetareas.tasks

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.*
import android.widget.Spinner
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.equipouno.aplicaciondetareas.R
import java.util.*
import android.view.ContextThemeWrapper
import org.json.JSONArray

// Importaciones para el sonido
import android.media.MediaPlayer
import androidx.appcompat.app.AlertDialog

class AddTaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_task)

        val etTitle: EditText = findViewById(R.id.et_task_title)
        val etDescription: EditText = findViewById(R.id.et_task_description)
        val etDeadline: EditText = findViewById(R.id.et_deadline)
        val ivCalendar: ImageView = findViewById(R.id.iv_calendar)
        val spinnerPriority: Spinner = findViewById(R.id.spinner_priority)
        val spinnerCategory: Spinner = findViewById(R.id.spinner_category)
        val spinnerRecurrence: Spinner = findViewById(R.id.spinner_recurrence)

        val btnCancel: Button = findViewById(R.id.btn_cancel)
        val btnSave: Button = findViewById(R.id.btn_save)

        // Configurar el spinner con las prioridades
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.priority_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = adapter
        // Cargar categorías desde SharedPreferences
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val categoriasJson = prefs.getString("categorias", null)
        val categoriasList = mutableListOf<String>()
        // Si hay categorías guardadas, cargarlas en la lista
        if (categoriasJson != null) {
            val jsonArray = JSONArray(categoriasJson)
            for (i in 0 until jsonArray.length()) {
                categoriasList.add(jsonArray.getString(i))
            }
        } else {
            // Valor por defecto si no hay categorías guardadas
            categoriasList.addAll(listOf("🧘 Personal", "💻 Trabajo", "🎓 Escuela"))
        }
        // Asignar al Spinner
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriasList)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        // Configurar el spinner con las frecuencias
        val recurrenceAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.recurrence_array,
            android.R.layout.simple_spinner_item
        )
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRecurrence.adapter = recurrenceAdapter

        // Configurar el DatePickerDialog al hacer clic en el ImageView
        ivCalendar.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                ContextThemeWrapper(this, R.style.CustomDatePickerDialog),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                    etDeadline.setText(formattedDate)
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        // Botón cancelar
        btnCancel.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cancelar cambios")
                .setMessage("¿Estás seguro de que deseas cancelar la tarea?")
                .setPositiveButton("Sí") { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }

        // Botón guardar
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString()
            val deadline = etDeadline.text.toString().trim()
            val priority = spinnerPriority.selectedItem.toString()
            val category = spinnerCategory.selectedItem.toString()
            val recurrence = spinnerRecurrence.selectedItem.toString()

            var isValid = true

            if (title.isEmpty()) {
                etTitle.error = "Este campo es obligatorio"
                isValid = false
            } else {
                etTitle.error = null
            }

            if (deadline.isEmpty()) {
                etDeadline.error = "Este campo es obligatorio"
                isValid = false
            } else {
                etDeadline.error = null
            }

            if (priority == "Seleccionar prioridad") {
                isValid = false
            }

            if (!isValid) return@setOnClickListener
            val data = Intent().apply {
                putExtra("title", title)
                putExtra("description", description)
                putExtra("deadline", deadline)
                putExtra("priority", priority)
                putExtra("category", category)
                putExtra("recurrence", recurrence)
            }

            setResult(RESULT_OK, data)
            android.widget.Toast.makeText(this, "!Tarea guardada ✨!", android.widget.Toast.LENGTH_SHORT).show()
            vibrate()
            playCompleteTaskSound()
            finish()
        }
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