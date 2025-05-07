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

        val priorityOptions = listOf("Alta", "Media", "Baja")
        val categoryOptions = listOf("Trabajo", "Personal", "Escuela")
        val recurrenceOptions = listOf("Ninguna", "Diaria", "Semanal", "Mensual")

        prioritySpinner.adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, priorityOptions).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        categorySpinner.adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryOptions).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
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
}

