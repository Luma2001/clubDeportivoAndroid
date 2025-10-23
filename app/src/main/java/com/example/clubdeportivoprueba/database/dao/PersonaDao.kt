package com.example.clubdeportivoprueba.database.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.clubdeportivoprueba.database.model.Persona

class PersonaDao(private val db: SQLiteDatabase) {
    fun insert(persona: Persona): Long {
        if (persona.nombre.isEmpty() || persona.apellido.isEmpty() || persona.dni.isEmpty()) {
            throw IllegalArgumentException("Faltan campos obligatorios")
        }

        if (getPersonByDNI(persona.dni) != null) throw IllegalArgumentException("Ya existe una persona con ese DNI")

        val values = ContentValues().apply {
            put("nombre", persona.nombre)
            put("apellido", persona.apellido)
            put("dni", persona.dni)
            put("direccion", persona.direccion)
            put("esSocio", if (persona.esSocio) 1 else 0)
        }
        return db.insert("Persona", null, values)
    }

    fun getPersonByDNI(dni: String): Persona? {
        val cursor = db.query(
            "Persona",
            null,
            "dni = ?",
            arrayOf(dni),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            val apellido = cursor.getString(cursor.getColumnIndexOrThrow("apellido"))
            val fetchedDni = cursor.getString(cursor.getColumnIndexOrThrow("dni"))
            val direccion = cursor.getString(cursor.getColumnIndexOrThrow("direccion"))
            val esSocio = cursor.getInt(cursor.getColumnIndexOrThrow("esSocio")) == 1

            cursor.close()

            Persona(id, nombre, apellido, fetchedDni, direccion, esSocio)
        } else {
            cursor.close()
            null
        }
    }
}