package com.equipouno.aplicaciondetareas.tasks

import CompletedTasksFragment
import PendingTasksFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TasksPagerAdapter(
    activity: FragmentActivity,
    private var pendingTasks: List<Task>,  // Lista de tareas pendientes
    private var completedTasks: List<Task>,  // Lista de tareas completadas
    private val onTaskDelete: (Task) -> Unit
) : FragmentStateAdapter(activity) {

    private var pendingFragment: PendingTasksFragment? = null
    private var completedFragment: CompletedTasksFragment? = null

    override fun getItemCount(): Int = 2

    // Función para crear los fragmentos de tareas pendientes y completadas
    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            val fragment = PendingTasksFragment()
            pendingFragment = fragment
            fragment.onTaskDelete = onTaskDelete
            fragment
        } else {
            val fragment = CompletedTasksFragment()
            completedFragment = fragment
            fragment.onTaskDelete = onTaskDelete
            fragment
        }
    }

    // Función para actualizar los fragmentos con las listas de tareas actualizadas
    fun refreshFragments(updatedPendingTasks: List<Task>, updatedCompletedTasks: List<Task>) {
        pendingTasks = updatedPendingTasks
        completedTasks = updatedCompletedTasks
    }

}