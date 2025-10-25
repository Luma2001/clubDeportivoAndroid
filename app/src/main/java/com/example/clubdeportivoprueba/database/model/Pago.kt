package com.example.clubdeportivoprueba.database.model

data class Pago (
    val id: Long = 0,
    val id_persona: Long,
    val tipo: String,
    val monto: Float,
    val fecha_pago: String,
    val fecha_inicio: String?, // para marcar el inicio del mes de membresía
    val fecha_fin: String?, // para marcar el fin del mes de membresía
    val id_actividad: Int? // solo para no socios


)