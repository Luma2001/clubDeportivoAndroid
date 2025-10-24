package com.example.clubdeportivoprueba.database.dao

import android.database.sqlite.SQLiteDatabase
import com.example.clubdeportivoprueba.database.model.Pago

class PagoDao(private val db: SQLiteDatabase) {
    fun getUltimaMembresiaActiva(personaId: Long, hoy: String): Pago? {
        val cursor = db.query(
            "Pago",
            null,
            "id_persona = ? AND fecha_fin >= ? AND tipo = 'cuota_mensual'",
            arrayOf(personaId.toString(), hoy),
            null,
            null,
            "fecha_fin DESC"
        )
        return if (cursor.moveToFirst()) {
            val pago = Pago(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                id_persona = cursor.getInt(cursor.getColumnIndexOrThrow("id_persona")),
                tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo")),
                monto = cursor.getFloat(cursor.getColumnIndexOrThrow("monto")),
                fecha_pago = cursor.getString(cursor.getColumnIndexOrThrow("fecha_pago")),
                fecha_inicio = cursor.getString(cursor.getColumnIndexOrThrow("fecha_inicio")),
                fecha_fin = cursor.getString(cursor.getColumnIndexOrThrow("fecha_fin")),
                id_actividad = cursor.isNull(cursor.getColumnIndexOrThrow("id_actividad"))
                    .takeIf { it }?.let { null }
                    ?: cursor.getInt(cursor.getColumnIndexOrThrow("id_actividad"))
            )
            cursor.close()
            pago
        } else {
            cursor.close()
            null
        }
    }
}