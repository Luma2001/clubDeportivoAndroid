package com.example.clubdeportivoprueba.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

const val DATABASE_NAME = "Database"
const val DATABASE_VERSION = 1

class Database(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        // crear todas las tablas

        db?.execSQL(CREATE_TABLE_USUARIO)
        db?.execSQL(CREATE_TABLE_PERSONA)

        // insertar usuario administrador por defecto
        if (db != null) insertDefaultAdmin(db)
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        TODO("Not yet implemented")
    }

    // === Sentencias SQL como constantes ===
    companion object {
        private const val CREATE_TABLE_USUARIO = """
            CREATE TABLE Usuario(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                dni VARCHAR(10),
                email VARCHAR(100),
                username VARCHAR(30),
                pass VARCHAR(100),
                rol VARCHAR(10)
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
}