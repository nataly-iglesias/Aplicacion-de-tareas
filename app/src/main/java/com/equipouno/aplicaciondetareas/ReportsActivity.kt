package com.equipouno.aplicaciondetareas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.equipouno.aplicaciondetareas.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import android.view.MenuItem
import androidx.core.content.ContextCompat
import com.equipouno.aplicaciondetareas.tasks.AppDatabase
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.formatter.ValueFormatter

class ReportsActivity  : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var db: AppDatabase
    private var isWeekly = true // Por defecto: reporte semanal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reports)

        pieChart = findViewById(R.id.pieChart)

        // Estilo
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f); // agujero central
        pieChart.setTransparentCircleRadius(45f); // círculo transparente alrededor
        pieChart.setCenterText("Tareas");
        pieChart.setCenterTextSize(16f);
        pieChart.setEntryLabelTextSize(12f);

        // Animaciones
        pieChart.animateY(1400, Easing.EaseInOutQuad);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setDragDecelerationFrictionCoef(0.9f);

        // Inicializa la base de datos
        db = AppDatabase.getDatabase(this)
        loadChartData()

        findViewById<Button>(R.id.btnWeekly).setOnClickListener {
            isWeekly = true
            loadChartData()
        }

        findViewById<Button>(R.id.btnMonthly).setOnClickListener {
            isWeekly = false
            loadChartData()
        }

        // Configuración de la navegación inferior
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_reports

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
                R.id.nav_reports -> true // Ya estás aquí
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

    }

    private fun loadChartData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val allTasks = db.taskDao().getAllTasksOnce()

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val today = Calendar.getInstance()

            val startDate: Calendar
            val endDate: Calendar

            if (isWeekly) {
                startDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                }
                endDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 7)
                }
            } else {
                startDate = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                endDate = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                }
            }

            val filteredTasks = allTasks.filter {
                try {
                    val taskDate = dateFormat.parse(it.deadline)
                    taskDate != null && taskDate >= startDate.time && taskDate <= endDate.time
                } catch (e: Exception) {
                    false
                }
            }


            // Obtener fechas únicas de tareas completadas
            val completedDates = filteredTasks
                .filter { it.isCompleted }
                .mapNotNull { task ->
                    try {
                        dateFormat.parse(task.deadline)
                    } catch (e: Exception) {
                        null
                    }
                }

            val daysWithCompletedTasks = completedDates.map { date ->
                val cal = Calendar.getInstance().apply { time = date }
                // Eliminar hora, minuto, segundo para comparar solo fechas
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.time
            }.toSet()

            val completedTaskDaysCount = daysWithCompletedTasks.size

            val completed = filteredTasks.count { it.isCompleted }
            val pending = filteredTasks.count { !it.isCompleted }

            val entries = ArrayList<BarEntry>().apply {
                add(BarEntry(0f, pending.toFloat()))
                add(BarEntry(1f, completed.toFloat()))
            }

            val dataSet = BarDataSet(entries, "Tareas").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextSize = 16f
            }

            val barData = BarData(dataSet)

            launch(Dispatchers.Main) {
                val pieEntries = ArrayList<PieEntry>().apply {
                    add(PieEntry(pending.toFloat(), "Pendientes"))
                    add(PieEntry(completed.toFloat(), "Completadas"))
                }

                val pieDataSet = PieDataSet(pieEntries, "").apply {
                    colors = listOf(
                        ContextCompat.getColor(this@ReportsActivity, R.color.taskPendingColor),
                        ContextCompat.getColor(this@ReportsActivity, R.color.taskCompletedColor)
                    )
                    valueTextSize = 24f
                    sliceSpace = 3f

                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "${value.toInt()}%"
                        }
                    }
                }

                val pieData = PieData(pieDataSet)

                pieChart.data = pieData
                pieChart.description.isEnabled = false
                pieChart.setUsePercentValues(true)
                pieChart.setDrawEntryLabels(false)
                pieChart.animateY(1000) // animación simple de entrada

                pieChart.legend.isEnabled = false
                pieChart.invalidate()

                findViewById<TextView>(R.id.tvCompletedTasks).text =
                    "✔ Tareas completadas: $completed (${formatPercentage(completed, completed + pending)}%)"

                findViewById<TextView>(R.id.tvPendingTasks).text =
                    "⏳ Tareas pendientes: $pending (${formatPercentage(pending, completed + pending)}%)"

                findViewById<TextView>(R.id.tvDaysWithCompletedTasks).text = "📅 Días con tareas completadas: $completedTaskDaysCount"
            }
        }
    }
    private fun formatPercentage(part: Int, total: Int): String {
        return if (total == 0) "0" else String.format("%.1f", (part * 100f / total))
    }
}