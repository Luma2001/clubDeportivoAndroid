package com.example.clubdeportivoprueba.database.model

data class Usuario(
    val id: Long = 0,
    val dni: String,
    val email: String,
    val username: String,
    val pass: String,
    val rol: String
)


