package com.example.clubdeportivoprueba.database.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.clubdeportivoprueba.database.model.Pago
import java.time.LocalDate

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
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                id_persona = cursor.getLong(cursor.getColumnIndexOrThrow("id_persona")),
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

    fun insert(personId: Long, tipo: String, monto: Float, idActividad: Int?): Long {
        // validaciones
        if (personId.toString().isEmpty() || tipo.isEmpty() || monto.toString().isEmpty()) {
            throw IllegalArgumentException("Faltan campos obligatorios")
        }

        if (tipo != "cuota_mensual" && tipo != "actividad") {
            throw IllegalArgumentException("Tipo de pago no válido")
        }

        if (tipo == "actividad" && idActividad == null) {
            throw IllegalArgumentException("Falta el ID de la actividad")
        }

        val esCuotaMensual = tipo == "cuota_mensual"
        val today = LocalDate.now().toString()
        val endDate = LocalDate.now().plusDays(30).toString()

        val values = ContentValues().apply {
            put("id_persona", personId)
            put("tipo", tipo)
            put("monto", monto)
            put("fecha_pago", today)
            put("fecha_inicio", if (esCuotaMensual) today else null)
            put("fecha_fin", if (esCuotaMensual) endDate else null)
            put("id_actividad", if (!esCuotaMensual) idActividad else null)
        }
        return db.insert("Pago", null, values)
    }
}