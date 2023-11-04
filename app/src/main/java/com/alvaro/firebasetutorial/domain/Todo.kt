package com.alvaro.firebasetutorial.domain

data class Todo(
    val title: String? = "",
    val description: String? = "",
    val done: Boolean? = false
)
