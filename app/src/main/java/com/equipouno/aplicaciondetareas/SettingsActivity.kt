package com.equipouno.aplicaciondetareas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray

class SettingsActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var listView: ListView
    private lateinit var etNewCategory: EditText
    private lateinit var btnAddCategory: Button
    private lateinit var adapter: CategoryAdapter
    private val categories = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        // Referencias
        listView = findViewById(R.id.lv_categories)
        etNewCategory = findViewById(R.id.et_newCategory)
        btnAddCategory = findViewById(R.id.btn_add_category)

        val etUserName = findViewById<EditText>(R.id.et_nameUser)
        val btnSaveName = findViewById<Button>(R.id.btn_save_name)

        // Obtener SharedPreferences
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        val savedCategoriesJson = prefs.getString("categorias", null)
        if (savedCategoriesJson != null) {
            val jsonArray = JSONArray(savedCategoriesJson)
            for (i in 0 until jsonArray.length()) {
                categories.add(jsonArray.getString(i))
            }
        } else {
            // Carga inicial
            categories.addAll(listOf("🧘 Personal", "💻 Trabajo", "🎓 Escuela"))
        }

        // Configurar adaptador para el listado de categorías
        adapter = CategoryAdapter(this, categories) { categoriaAEliminar ->
            categories.remove(categoriaAEliminar)
            adapter.notifyDataSetChanged()
            val updatedJson = JSONArray(categories).toString()
            prefs.edit().putString("categorias", updatedJson).apply()
            Toast.makeText(this, "Categoría eliminada", Toast.LENGTH_SHORT).show()
        }
        listView.adapter = adapter

        // Agregar nueva categoría
        btnAddCategory.setOnClickListener {
            val newCategory = etNewCategory.text.toString().trim()
            if (newCategory.isNotEmpty() && !categories.contains(newCategory)) {
                categories.add(newCategory)
                adapter.notifyDataSetChanged()
                etNewCategory.text.clear()

                val  updatedJson = JSONArray(categories).toString()
                prefs.edit().putString("categorias",  updatedJson).apply()
                Toast.makeText(this, "Categoría agregada", Toast.LENGTH_SHORT).show()
            }
        }

        // Cargar nombre de usuario
        etUserName.setText(prefs.getString("nombre_usuario", ""))

        btnSaveName.setOnClickListener {
            val userName = etUserName.text.toString().trim()
            if (userName.isNotEmpty()) {
                prefs.edit().putString("nombre_usuario", userName).apply()
                Toast.makeText(this, "Nombre guardado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ingresa un nombre válido", Toast.LENGTH_SHORT).show()
            }
        }

        // Configura el BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_settings

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
                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    true
                }
                else -> false
            }
        }

    }
}
