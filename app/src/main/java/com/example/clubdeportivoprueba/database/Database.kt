package com.example.clubdeportivoprueba.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

const val DATABASE_NAME = "Database"
const val DATABASE_VERSION = 6

class Database(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        // esto activa las foreign key porque SQLite no las activa por defecto
        db?.execSQL("PRAGMA foreign_keys = ON")

        // crear todas las tablas
        db?.execSQL(CREATE_TABLE_USUARIO)
        db?.execSQL(CREATE_TABLE_EMPLEADO)
        db?.execSQL(CREATE_TABLE_PERSONA)
        db?.execSQL(CREATE_TABLE_PARAMETRO)
        db?.execSQL(CREATE_TABLE_ACTIVIDAD)
        db?.execSQL(CREATE_TABLE_PAGO)

        // insertar valores por defecto
        if (db != null) {
            insertDefaultAdmin(db)
            insertDefaultEmpleados(db)
            insertDefaultParametros(db)
            insertDefaultActividades(db)
        }

    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        // solo en desarrollo. en produccion no se deben eliminar las tablas
        db?.execSQL("DROP TABLE IF EXISTS Usuario")
        db?.execSQL("DROP TABLE IF EXISTS Persona")
        db?.execSQL("DROP TABLE IF EXISTS Parametro")
        db?.execSQL("DROP TABLE IF EXISTS Actividad")
        db?.execSQL("DROP TABLE IF EXISTS Pago")
        onCreate(db)
    }

    // === Sentencias SQL como constantes ===
    companion object {
        private const val CREATE_TABLE_USUARIO = """
            CREATE TABLE Usuario(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                dni VARCHAR(10) UNIQUE,
                email VARCHAR(100),
                username VARCHAR(30),
                pass VARCHAR(100),
                rol VARCHAR(10) DEFAULT 'empleado',
                activo INTEGER DEFAULT 1,
                FOREIGN KEY (dni) REFERENCES Empleado(dni) ON DELETE CASCADE
            )
        """

        private const val CREATE_TABLE_EMPLEADO = """
            CREATE TABLE Empleado(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre VARCHAR(100) NOT NULL,
                apellido VARCHAR(100) NOT NULL,
                dni VARCHAR(10) NOT NULL UNIQUE,
                activo INTEGER DEFAULT 1 -- 1 activo , 0 = inactivo
            )
        """

        private const val CREATE_TABLE_PERSONA = """
            CREATE TABLE Persona(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre VARCHAR(100) NOT NULL,
                apellido VARCHAR(100) NOT NULL,
                dni VARCHAR(10) NOT NULL,
                direccion VARCHAR(200),
                esSocio INTEGER NOT NULL -- 0 = false, 1 = true
            )
        """

        // Esta tabla sirve para almacenar valores de configuracion de la app, como la cuota mensual del socio
        private const val CREATE_TABLE_PARAMETRO = """
            CREATE TABLE Parametro(
                clave TEXT PRIMARY KEY,
                valor TEXT NOT NULL,
                descripcion TEXT
            )
        """

        private const val CREATE_TABLE_ACTIVIDAD = """
            CREATE TABLE Actividad(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre VARCHAR(100) NOT NULL,
                precio REAL NOT NULL
            )
        """

        private const val CREATE_TABLE_PAGO = """
            CREATE TABLE Pago(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                id_persona INTEGER NOT NULL,
                tipo TEXT NOT NULL,             -- 'cuota_mensual' o 'actividad'
                monto REAL NOT NULL,
                fecha_pago TEXT NOT NULL,       -- formato ISO: '2025-10-23'
                fecha_inicio TEXT,              -- para socios. inicio de memebresía
                fecha_fin TEXT,                 -- para socios. fin de membresía
                id_actividad INTEGER,           -- solo para actividades
                FOREIGN KEY(id_persona) REFERENCES Persona(id) ON DELETE CASCADE,
                FOREIGN KEY (id_actividad) REFERENCES Actividad(id) ON DELETE SET NULL
            )
        """
    }

    private fun insertDefaultAdmin(db: SQLiteDatabase) {
        val adminValues = ContentValues().apply {
            put("dni", "00000000")
            put("email", "admin@club.com")
            put("username", "admin")
            put("pass", "admin123")
            put("rol", "admin")
        }
        db.insert("Usuario", null, adminValues)
    }

    private fun insertDefaultEmpleados(db: SQLiteDatabase) {
        val empleados = listOf(
            Triple("Juan", "Pérez", "12345678"),
            Triple("Fabio", "Hubert", "01010101"),
            Triple("María", "Gómez", "23456789"),
            Triple("Carlos", "López", "34567890"),
            Triple("Ana", "Rodríguez", "45678901")
        )

        empleados.forEach { (nombre, apellido, dni) ->
            val values = ContentValues().apply {
                put("nombre", nombre)
                put("apellido", apellido)
                put("dni", dni)
            }
            db.insert("Empleado", null, values)
        }
    }

    private fun insertDefaultParametros(db: SQLiteDatabase) {
        val cuotaMensual = ContentValues().apply {
            put("clave", "cuota_mensual")
            put("valor", "60000.0")
            put("descripcion", "Cuota mensual para socios")
        }
        db.insert("Parametro", null, cuotaMensual)
    }

    private fun insertDefaultActividades(db: SQLiteDatabase) {
        val actividades = listOf(
            Pair("Natación", 8000.0),
            Pair("Tenis", 12000.0),
            Pair("Gimnasio", 6000.0),
            Pair("Fútbol", 6000.0),
            Pair("Quincho", 12000.0),
            Pair("Fogones", 3000.0),
        )

        actividades.forEach { (nombre, precio) ->
            val values = ContentValues().apply {
                put("nombre", nombre)
                put("precio", precio)
            }
            db.insert("Actividad", null, values)
        }
    }
}