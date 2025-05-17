package com.equipouno.aplicaciondetareas.tasks

import CompletedTasksFragment
import PendingTasksFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TasksPagerAdapter(
    activity: FragmentActivity,
    private var pendingTasks: List<Task>,
    private var completedTasks: List<Task>,
    private val onTaskDelete: (Task) -> Unit
) : FragmentStateAdapter(activity) {

    private var pendingFragment: PendingTasksFragment? = null
    private var completedFragment: CompletedTasksFragment? = null

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            pendingFragment = PendingTasksFragment.newInstance(pendingTasks, onTaskDelete)
            pendingFragment!!
        } else {
            completedFragment = CompletedTasksFragment.newInstance(completedTasks, onTaskDelete)
            completedFragment!!
        }
    }

    fun refreshFragments(updatedPendingTasks: List<Task>, updatedCompletedTasks: List<Task>) {
        this.pendingTasks = updatedPendingTasks
        this.completedTasks = updatedCompletedTasks
        pendingFragment?.updateTaskList(updatedPendingTasks)
        completedFragment?.updateTaskList(updatedCompletedTasks)
    }

    fun updateTasks(filteredPending: List<Task>, filteredCompleted: List<Task>) {
        pendingFragment?.updateTaskList(filteredPending)
        completedFragment?.updateTaskList(filteredCompleted)
    }
}
