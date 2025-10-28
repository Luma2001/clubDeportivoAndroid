package com.example.clubdeportivoprueba.database.model

data class Empleado (
    val id: Long = 0,
    val nombre: String,
    val apellido: String,
    val dni: String,
    val activo: Boolean = true
)