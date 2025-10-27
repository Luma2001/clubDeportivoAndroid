package com.example.clubdeportivoprueba.database.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.clubdeportivoprueba.database.model.Pago
import com.example.clubdeportivoprueba.database.model.Persona
import java.time.LocalDate

class PagoDao(private val db: SQLiteDatabase) {
    fun getUltimaMembresia(personaId: Long): Pago? {
        val cursor = db.query(
            "Pago",
            null,
            "id_persona = ? AND tipo = 'cuota_mensual'",
            arrayOf(personaId.toString()),
            null,
            null,
            "fecha_fin DESC",
            "1"
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

    fun puedeEmitirCarnet(personaId: Long): Pair<Boolean, String> {
        val ultimaMembresia = getUltimaMembresia(personaId)
        val hoy = LocalDate.now()

        // si no tiene membresías previas no puede emitir carnet
        if (ultimaMembresia == null) {
            return Pair(false, "Es necesario abonar la primer cuota para emitir carnet")
        }

        val fechaFinUltimaMembresia = LocalDate.parse(ultimaMembresia.fecha_fin)
        val diasDesdeVencimiento = hoy.toEpochDay() - fechaFinUltimaMembresia.toEpochDay()

        return when {
            // si está dentro del periodo activo (30 días) , puede emitir carnet
            hoy.isBefore(fechaFinUltimaMembresia) || hoy.isEqual(fechaFinUltimaMembresia) -> {
                Pair(true, "Membresía activa hasta ${ultimaMembresia.fecha_fin}")
            }
            // si está en periodo de gracia (primeros 10 días desde el vencimiento) no puede emitir carnet
            diasDesdeVencimiento <= 10 -> {
                Pair(false, "Periodo de gracia. Regularice antes de emitir carnet")
            }
            // si pasaron más de 10 días desde vencimiento no puede emitir
            else -> {
                Pair(
                    false,
                    "Membresía vencida. Es necesario abonar una cuota antes de emitir el carnet"
                )
            }
        }
    }

    fun puedePagarCuota(personaId: Long): Pair<Boolean, String> {
        val ultimaMembresia = getUltimaMembresia(personaId)
        val hoy = LocalDate.now()

        // si no hay membresias previas puede pagar
        if (ultimaMembresia == null) {
            return Pair(true, "Sin membresías previas")
        }

        val fechaFinUltimaMembresia = LocalDate.parse(ultimaMembresia.fecha_fin)
        val diasDesdeVencimiento = hoy.toEpochDay() - fechaFinUltimaMembresia.toEpochDay()

        return when {
            // Si está dentro del período activo (30 días)
            hoy.isBefore(fechaFinUltimaMembresia) || hoy.isEqual(fechaFinUltimaMembresia) -> {
                Pair(false, "Socio al día. Membresía activa hasta ${ultimaMembresia.fecha_fin}")
            }
            // Si está en período de gracia (primeros 10 días después del vencimiento)
            diasDesdeVencimiento <= 10 -> {
                Pair(true, "En período de gracia")
            }
            // Si pasaron más de 10 días desde el vencimiento
            else -> {
                Pair(true, "Nueva membresía requerida")
            }
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
        val today = LocalDate.now()

        // calculo de fechas para cuotas de socios
        var fechaInicio = today.toString()
        var fechaFin = today.plusDays(30).toString()

        if (esCuotaMensual) {
            val ultimaMembresia = getUltimaMembresia(personId)
            if (ultimaMembresia != null) {
                val fechaFinUltima = LocalDate.parse(ultimaMembresia.fecha_fin)
                val diasDesdeVencimiento = today.toEpochDay() - fechaFinUltima.toEpochDay()

                // si paga dentro del periodo de gracia (10 días) continua desde donde terminó
                if (diasDesdeVencimiento <= 10 && diasDesdeVencimiento > 0) {
                    fechaInicio = fechaFinUltima.plusDays(1).toString()
                    fechaFin = fechaFinUltima.plusDays(31)
                        .toString() // 30 días desde la nueva fecha de inicio
                }

                // si está pagando antes del vencimiento no se permite ya que el socio está al día
            }
        }

        val values = ContentValues().apply {
            put("id_persona", personId)
            put("tipo", tipo)
            put("monto", monto)
            put("fecha_pago", today.toString())
            put("fecha_inicio", if (esCuotaMensual) fechaInicio else null)
            put("fecha_fin", if (esCuotaMensual) fechaFin else null)
            put("id_actividad", if (!esCuotaMensual) idActividad else null)
        }
        return db.insert("Pago", null, values)
    }

    fun getPagoById(id: Long): Pago? {
        val cursor = db.query(
            "Pago",
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null,
            null
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
                id_actividad = cursor.getInt(cursor.getColumnIndexOrThrow("id_actividad"))
            )
            cursor.close()
            pago
        } else {
            cursor.close()
            null
        }
    }

    fun getSociosConMembresiaVencidaOEnGracia(): List<Pair<Persona, Pago?>> {
        val hoy = LocalDate.now()
        val sociosConProblemas = mutableListOf<Pair<Persona, Pago?>>()

        // obtener todos los socios
        val cursorSocios = db.query(
            "Persona", null, "esSocio = 1", null, null, null, null
        )

        while (cursorSocios.moveToNext()) {
            val persona = Persona(
                id = cursorSocios.getLong(cursorSocios.getColumnIndexOrThrow("id")),
                nombre = cursorSocios.getString(cursorSocios.getColumnIndexOrThrow("nombre")),
                apellido = cursorSocios.getString(cursorSocios.getColumnIndexOrThrow("apellido")),
                dni = cursorSocios.getString(cursorSocios.getColumnIndexOrThrow("dni")),
                direccion = cursorSocios.getString(cursorSocios.getColumnIndexOrThrow("direccion")),
                esSocio = cursorSocios.getInt(cursorSocios.getColumnIndexOrThrow("esSocio")) == 1
            )

            // obtener ultima meembresia para este socio
            val ultimaMembresia = getUltimaMembresia(persona.id)

            if (ultimaMembresia == null) {
                // nunca pagó, se considera vencido
                sociosConProblemas.add(Pair(persona, null))
            } else {
                val fechaFin = LocalDate.parse(ultimaMembresia.fecha_fin)
                val diasDesdeVencimiento = hoy.toEpochDay() - fechaFin.toEpochDay()

                // está vencida (más de 0 días) O en período de gracia (hasta 10 días)
                if (diasDesdeVencimiento >= 0) {
                    sociosConProblemas.add(Pair(persona, ultimaMembresia))
                }
                // NOTA: Si diasDesdeVencimiento < 0, significa que la membresía aún está activa
            }
        }

        cursorSocios.close()
        return sociosConProblemas
    }
}