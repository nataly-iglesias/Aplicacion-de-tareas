package com.equipouno.aplicaciondetareas.tasks

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import android.widget.Spinner
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.equipouno.aplicaciondetareas.R
import java.util.*
import android.view.MotionEvent
import android.view.ContextThemeWrapper

class AddTaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_task)

        val etTitle: EditText = findViewById(R.id.et_task_title)
        val etDescription: EditText = findViewById(R.id.et_task_description)
        val etDeadline: EditText = findViewById(R.id.et_deadline)
        val spinnerPriority: Spinner = findViewById(R.id.spinner_priority)
        val spinnerCategory: Spinner = findViewById(R.id.spinner_category)
        val spinnerRecurrence: Spinner = findViewById(R.id.spinner_recurrence)

        // Configurar el spinner con las categorías
        val categoryAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.category_array,
            android.R.layout.simple_spinner_item
        )
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

        val btnCancel: Button = findViewById(R.id.btn_cancel)
        val btnSave: Button = findViewById(R.id.btn_save)

        etDeadline.showSoftInputOnFocus = true // Permite entrada manual
        etDeadline.inputType = android.text.InputType.TYPE_CLASS_DATETIME or android.text.InputType.TYPE_DATETIME_VARIATION_DATE

        // Configurar el touch listener para detectar click en el icono del calendario
        etDeadline.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = etDeadline.compoundDrawablesRelative[2] // drawableEnd (no drawableRight)
                if (drawableEnd != null && event.rawX >= (etDeadline.right - drawableEnd.bounds.width())) {
                    // Abrir el calendario
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
                        year,
                        month,
                        day
                    )
                    datePickerDialog.show()
                    return@setOnTouchListener true
                }
            }
            false
        }

        // Configurar el spinner con las prioridades
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.priority_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = adapter

        // Botón cancelar
        btnCancel.setOnClickListener {
            finish()
        }

        // Botón guardar
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
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

            if (description.isEmpty()) {
                etDescription.error = "Este campo es obligatorio"
                isValid = false
            } else {
                etDescription.error = null
            }

            if (deadline.isEmpty()) {
                etDeadline.error = "Este campo es obligatorio"
                isValid = false
            } else {
                etDeadline.error = null
            }

            if (priority == "Seleccionar prioridad") {
                Toast.makeText(this, "Por favor, selecciona un nivel de prioridad", Toast.LENGTH_SHORT).show()
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
            finish()
        }
    }
}
