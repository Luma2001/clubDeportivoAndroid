package com.example.clubdeportivoprueba.database.model

data class Persona(
    val id: Long = 0,
    val nombre: String,
    val apellido: String,
    val dni: String,
    val direccion: String,
    val esSocio: Boolean // se guarda como INTEGER en la db porque SQLite no tiene boolean
)