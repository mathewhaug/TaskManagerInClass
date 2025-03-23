package com.matthaug.taskmanager.models

import android.icu.util.Currency

data class Task(
    val id: Number,
    val name: String,
    val dueDate: String,
    val priority: String,
    val costAssociated:Boolean,
    val currency: Currency,
    val cost: Double = 0.0,
    val completed: Boolean,
    val overdue: Boolean,


    )



