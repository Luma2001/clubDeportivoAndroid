package com.example.clubdeportivoprueba.database.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.clubdeportivoprueba.database.model.Persona

class PersonaDao(private val db: SQLiteDatabase) {
    fun insert(persona: Persona): Long {
        val values = ContentValues().apply {
            put("nombre", persona.nombre)
            put("apellido", persona.apellido)
            put("dni", persona.dni)
            put("direccion", persona.direccion)
            put("esSocio", if (persona.esSocio) 1 else 0)

        }
        return db.insert("Persona", null, values)

    }
}