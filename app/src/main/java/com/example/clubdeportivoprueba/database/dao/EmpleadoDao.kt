package com.example.clubdeportivoprueba.database.dao

import android.database.sqlite.SQLiteDatabase
import com.example.clubdeportivoprueba.database.model.Empleado

class EmpleadoDao(private val db: SQLiteDatabase) {
    fun getByDNI(dni: String): Empleado? {
        val cursor = db.query(
            "Empleado",
            null,
            "dni = ? AND activo = 1",
            arrayOf(dni),
            null, null, null

        )

        return if (cursor.moveToFirst()) {
            val empleado = Empleado(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                apellido = cursor.getString(cursor.getColumnIndexOrThrow("apellido")),
                dni = cursor.getString(cursor.getColumnIndexOrThrow("dni")),
                activo = cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1
            )
            cursor.close()
            empleado
        } else {
            cursor.close()
            null
        }
    }


}