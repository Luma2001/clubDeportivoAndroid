package com.example.clubdeportivoprueba.database.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.clubdeportivoprueba.database.model.Usuario

class UsuarioDao(private val db: SQLiteDatabase) {
    fun insert(dni: String, email: String, username: String, pass: String, rol: String): Long {
        val values = ContentValues().apply {
            put("dni", dni)
            put("email", email)
            put("username", username)
            put("pass", pass)
            put("rol", rol)
        }
        return db.insert("Usuario", null, values)
    }

    fun update(usuario: Usuario): Int {
        val values = ContentValues().apply {
            put("dni", usuario.dni)
            put("email", usuario.email)
            put("username", usuario.username)
            put("pass", usuario.pass)
            put("rol", usuario.rol)
        }
        return db.update("Usuario", values, "id = ?", arrayOf(usuario.id.toString()))

    }

    fun delete(id: Long): Int {
        return db.delete("Usuario", "id = ?", arrayOf(id.toString()))
    }

    fun getAll(): List<Usuario> {
        val cursor = db.query("Usuario", null, null, null, null, null, null)
        val usuarios = mutableListOf<Usuario>()

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val dni = getString(getColumnIndexOrThrow("dni"))
                val email = getString(getColumnIndexOrThrow("email"))
                val username = getString(getColumnIndexOrThrow("username"))
                val pass = getString(getColumnIndexOrThrow("pass"))
                val rol = getString(getColumnIndexOrThrow("rol"))
                usuarios.add(Usuario(id, dni, email, username, pass, rol))
            }
            close()
        }
        return usuarios
    }

    // metodo para obtener un usuario por sus credenciales
    fun getByCredentials(username: String, pass: String): Usuario? {
        val cursor = db.query(
            "Usuario",
            null,
            "username = ? AND pass = ?",
            arrayOf(username, pass),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val dni = cursor.getString(cursor.getColumnIndexOrThrow("dni"))
            val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            val fetchedUsername = cursor.getString(cursor.getColumnIndexOrThrow("username"))
            val fetchedPass = cursor.getString(cursor.getColumnIndexOrThrow("pass"))
            val rol = cursor.getString(cursor.getColumnIndexOrThrow("rol"))
            cursor.close()
            Usuario(id, dni, email, fetchedUsername, fetchedPass, rol)
        } else {
            cursor.close()
            null
        }
    }

    fun getByDNI(dni: String): Usuario? {
        val cursor = db.query(
            "Usuario",
            null,
            "dni = ?",
            arrayOf(dni),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val fetchedDni = cursor.getString(cursor.getColumnIndexOrThrow("dni"))
            val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
            val fetchedPass = cursor.getString(cursor.getColumnIndexOrThrow("pass"))
            val rol = cursor.getString(cursor.getColumnIndexOrThrow("rol"))
            cursor.close()
            Usuario(id, fetchedDni, email, username, fetchedPass, rol)
        } else {
            cursor.close()
            null
        }
    }

    fun getByUsername(username: String): Usuario? {
        val cursor = db.query(
            "Usuario",
            null,
            "username = ?",
            arrayOf(username),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val dni = cursor.getString(cursor.getColumnIndexOrThrow("dni"))
            val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            val fetchedUsername = cursor.getString(cursor.getColumnIndexOrThrow("username"))
            val fetchedPass = cursor.getString(cursor.getColumnIndexOrThrow("pass"))
            val rol = cursor.getString(cursor.getColumnIndexOrThrow("rol"))
            cursor.close()
            Usuario(id, dni, email, fetchedUsername, fetchedPass, rol)
        } else {
            cursor.close()
            null
        }
    }
}