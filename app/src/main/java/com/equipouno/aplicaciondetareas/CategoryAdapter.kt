package com.equipouno.aplicaciondetareas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class CategoryAdapter(
    private val context:Context,
    private val categories: MutableList<String>,
    private val onDeleteCategory: (String) -> Unit
    ) : BaseAdapter(){

    // Lista de categorías predeterminadas que no se pueden eliminar
    private val categoriasPredeterminadas = listOf(
        "🧘 Personal",
        "💻 Trabajo",
        "🎓 Escuela"
    )

    override fun getCount(): Int = categories.size
    override fun getItem(position: Int): Any = categories[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView:View?, parent: ViewGroup?): View{
        val view: View = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_category, parent, false)

        val tvCategory = view.findViewById<TextView>(R.id.tv_name_category)
        val btnDelete = view.findViewById<Button>(R.id.btn_delete_category)

        val category = categories[position]
        tvCategory.text = category

        if (category in categoriasPredeterminadas) {
            btnDelete.isEnabled = false
            btnDelete.alpha = 0.4f // Se ve deshabilitado visualmente
        } else {
            btnDelete.isEnabled = true
            btnDelete.alpha = 1f
            btnDelete.setOnClickListener {
                onDeleteCategory(category)
            }
        }
        return view
    }
}