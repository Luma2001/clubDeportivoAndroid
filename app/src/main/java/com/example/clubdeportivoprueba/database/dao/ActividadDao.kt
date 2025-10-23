package com.example.clubdeportivoprueba.database.dao

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteDatabase
import com.example.clubdeportivoprueba.database.model.Actividad

class ActividadDao(private val db: SQLiteDatabase) {
    @SuppressLint("Range")
    fun getActivities(): List<Actividad> {
        val activities = mutableListOf<Actividad>()

        // el null significa traer todo sin filtros
        val cursor = db.query(
            "Actividad",
            null,
            null,
            null,
            null,
            null,
            "nombre ASC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val actividad = Actividad(
                        id = it.getInt(it.getColumnIndex("id")),
                        nombre = it.getString(it.getColumnIndex("nombre")),
                        precio = it.getFloat(it.getColumnIndex("precio"))
                    )
                    activities.add(actividad)
                } while (it.moveToNext())
            }
        }
        return activities
    }
}