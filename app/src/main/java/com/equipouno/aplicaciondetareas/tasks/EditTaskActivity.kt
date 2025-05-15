package com.equipouno.aplicaciondetareas.tasks
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.equipouno.aplicaciondetareas.R
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog

// Importaciones para la vibración
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context

// Importaciones para el sonido
import android.media.MediaPlayer
import android.widget.ArrayAdapter
import org.json.JSONArray

class EditTaskActivity : AppCompatActivity() {

    private lateinit var taskDao: TaskDao
    private var taskId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_task)

        val titleEditText = findViewById<EditText>(R.id.et_task_title)
        val descriptionEditText = findViewById<EditText>(R.id.et_task_description)
        val deadlineEditText = findViewById<EditText>(R.id.et_deadline)
        val prioritySpinner = findViewById<Spinner>(R.id.spinner_priority)
        val categorySpinner = findViewById<Spinner>(R.id.spinner_category)
        val recurrenceSpinner = findViewById<Spinner>(R.id.spinner_recurrence)
        val saveButton = findViewById<Button>(R.id.btn_save)
        val cancelButton = findViewById<Button>(R.id.btn_cancel)

        taskDao = AppDatabase.getDatabase(applicationContext).taskDao()

        // Obtener datos de la tarea desde el formulario
        taskId = intent.getStringExtra("task_id")?: ""
        val taskTitle = intent.getStringExtra("task_title") ?: ""
        val taskDescription = intent.getStringExtra("task_description") ?: ""
        val taskDeadline = intent.getStringExtra("task_deadline") ?: ""
        val taskPriority = intent.getStringExtra("task_priority") ?: ""
        val taskCategory = intent.getStringExtra("task_category") ?: ""
        val taskRecurrence = intent.getStringExtra("task_recurrence") ?: ""

        val priorityOptions = listOf("\uD83D\uDD34 Alta", "\uD83D\uDFE1 Media", "\uD83D\uDFE2 Baja")
        val recurrenceOptions = listOf("Una vez", "Diaria", "Semanal", "Mensual")

        //Spinner de prioridad
        prioritySpinner.adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, priorityOptions).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        //Spinner de categoría
        // Cargar categorías desde SharedPreferences
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val categoriesJson = prefs.getString("categorias", "[]")
        val categoriesList = JSONArray(categoriesJson).let { jsonArray ->
            mutableListOf<String>().apply {
                for (i in 0 until jsonArray.length()) {
                    add(jsonArray.getString(i))
                }
            }
        }
        // Si no se cargan categorías, usar categorías predeterminadas
        if (categoriesList.isEmpty()) {
            categoriesList.addAll(listOf("Selecionar categoría", "🧘 Personal", "💻 Trabajo", "🎓 Escuela"))
        }
        categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriesList).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        //Spinner de recurrencia
        recurrenceSpinner.adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, recurrenceOptions).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Rellenar los campos
        titleEditText.setText(taskTitle)
        descriptionEditText.setText(taskDescription)
        deadlineEditText.setText(taskDeadline)
        setSpinnerSelection(prioritySpinner, taskPriority)
        setSpinnerSelection(categorySpinner, taskCategory)
        setSpinnerSelection(recurrenceSpinner, taskRecurrence)

        // Guardar cambios
        saveButton.setOnClickListener {
            val updatedTask = Task(
                id = taskId,
                title = titleEditText.text.toString(),
                description = descriptionEditText.text.toString(),
                deadline = deadlineEditText.text.toString(),
                priority = prioritySpinner.selectedItem.toString(),
                category = categorySpinner.selectedItem.toString(),
                recurrence = recurrenceSpinner.selectedItem.toString(),
                isCompleted = false // o mantenlo como estaba originalmente
            )
            lifecycleScope.launch {
                taskDao.update(updatedTask)
                android.widget.Toast.makeText(this@EditTaskActivity, "¡Tarea actualizada \uD83E\uDE84!", android.widget.Toast.LENGTH_SHORT).show()
                vibrate()
                playCompleteTaskSound()
                finish()
            }
        }

        // Cancelar cambios
        cancelButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cancelar cambios")
                .setMessage("¿Estás seguro de que deseas cancelar los cambios?")
                .setPositiveButton("Sí") { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    // Método para establecer la selección del Spinner
    fun setSpinnerSelection(spinner: Spinner, value: String) {
        val adapter = spinner.adapter
        if (adapter != null) {
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i) == value) {
                    spinner.setSelection(i)
                    break
                }
            }
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

