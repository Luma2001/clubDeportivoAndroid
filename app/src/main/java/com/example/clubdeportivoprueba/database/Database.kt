package com.example.clubdeportivoprueba.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

const val DATABASE_NAME = "Database"
const val DATABASE_VERSION = 1

class Database(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        // crear tabla usuario
        val sql = "CREATE TABLE Usuario(id INTEGER PRIMARY KEY AUTOINCREMENT, dni VARCHAR(10), email VARCHAR(100), username VARCHAR(30), pass VARCHAR(100), rol VARCHAR(10))"
        db?.execSQL(sql)

        // insertar usuario administrador por defecto
        val adminValues = ContentValues().apply {
            put("dni", "00000000")
            put("email", "admin@club.com")
            put("username", "admin")
            put("pass", "admin123")
            put("rol", "admin")
        }
        db?.insert("Usuario", null, adminValues)
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        TODO("Not yet implemented")
    }
}