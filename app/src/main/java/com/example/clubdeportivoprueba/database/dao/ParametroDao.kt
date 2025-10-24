package com.example.clubdeportivoprueba.database.dao

import android.database.sqlite.SQLiteDatabase

class ParametroDao(private val db: SQLiteDatabase) {
    fun getCuotaMensualSocio(): Float {
        val cursor = db.query(
            "Parametro",
            arrayOf("valor"),
            "clave = ?",
            arrayOf("cuota_mensual"),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()){
            val valorStr = cursor.getString(0)
            cursor.close()
            valorStr.toFloatOrNull() ?: 0f
        } else {
            cursor.close()
            0f // por defecto cero si no está registrada la cuota mensual
        }
    }
}