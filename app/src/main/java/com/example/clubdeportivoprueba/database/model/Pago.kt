package com.example.clubdeportivoprueba.database.model

data class Pago (
    val id: Int,
    val id_persona: Int,
    val tipo: String,
    val monto: Float,
    val fecha_pago: String,
    val periodo: String?,       // Nulable, porque solo aplica a cuotas
    val id_actividad: Int?
)