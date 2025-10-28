package com.example.clubdeportivoprueba

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivoprueba.database.Database
import com.example.clubdeportivoprueba.database.dao.EmpleadoDao
import com.example.clubdeportivoprueba.database.dao.UsuarioDao

class MainActivity : AppCompatActivity() {
    private lateinit var database: Database
    private lateinit var usuarioDao: UsuarioDao
    private lateinit var empleadoDao: EmpleadoDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Llamar a la base de datos para crearla si aún no existe
        database = Database(this)
        val db = database.writableDatabase
        empleadoDao = EmpleadoDao(db)
        usuarioDao = UsuarioDao(db)

        val etUsuario = findViewById<EditText>(R.id.editTextUsuario)
        val etPassword = findViewById<EditText>(R.id.editTextPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = etUsuario.text.toString()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            // buscar usuario en la DB
            val usuario = usuarioDao.getByCredentials(username, password)

            if (usuario == null) {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else {
                val intent = Intent(this, MenuActivity::class.java)
                intent.putExtra("user", usuario.username)
                intent.putExtra("rol", usuario.rol)
                startActivity(intent)

                // limpiar inputs
                etUsuario.text.clear()
                etPassword.text.clear()
            }

        }

        val btnCrearCuenta = findViewById<Button>(R.id.btnCrearCuenta)
        btnCrearCuenta.setOnClickListener {
            val intent = Intent(this, CrearCuentaActivity::class.java)
            startActivity(intent)
        }

        val btnRecuperarPass = findViewById<Button>(R.id.btnRecuperarPass)
        btnRecuperarPass.setOnClickListener {
            mostrarDialogoRecuperarPassword()
        }


    }

    private fun mostrarDialogoRecuperarPassword() {
        val input = EditText(this).apply {
            hint = "Ingrese su DNI"
        }

        AlertDialog.Builder(this)
            .setTitle("Recuperar contraseña")
            .setMessage("Ingrese su DNI para resetear su contraseña")
            .setView(input)
            .setPositiveButton("Aceptar") {dialog, which ->
                val dni = input.text.toString().trim()
                resetPassword(dni)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun resetPassword(dni: String) {
        if (dni.isEmpty()) {
            Toast.makeText(this, "Ingrese un DNI", Toast.LENGTH_SHORT).show()
            return
        }

        // verificar que el empleado existe
        val empleado = empleadoDao.getByDNI(dni)
        if(empleado == null) {
            Toast.makeText(this, "DNI no registrado como empleado", Toast.LENGTH_SHORT).show()
            return
        }

        // verificar que el empleado tiene cuenta de usuario
        val usuario = usuarioDao.getByDNI(dni)
        if (usuario == null) {
            Toast.makeText(this, "No existe una cuenta para este empleado", Toast.LENGTH_SHORT).show()
            return
        }

        // resetear contraseña. poner DNI
        val passwordReset = dni

        val updated = usuarioDao.updatePasswordByDNI(dni, passwordReset)

        if(updated) {
            AlertDialog.Builder(this)
                .setTitle("Contraseña reseteada")
                .setMessage("La contraseña ha sido reseteada exitosamente.\n\n" + "Su nueva contraseña será su DNI")
                .setPositiveButton("Aceptar", null)
                .show()
        } else {
            Toast.makeText(this, "Error al resetear la contraseña", Toast.LENGTH_SHORT).show()
        }
    }
}