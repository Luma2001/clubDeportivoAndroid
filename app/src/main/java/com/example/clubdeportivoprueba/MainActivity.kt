package com.example.clubdeportivoprueba

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivoprueba.database.Database
import com.example.clubdeportivoprueba.database.dao.UsuarioDao

class MainActivity : AppCompatActivity() {
    private lateinit var database: Database

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
        val usuarioDao = UsuarioDao(db)

        val etUsuario = findViewById<EditText>(R.id.editTextUsuario)
        val etPassword = findViewById<EditText>(R.id.editTextPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = etUsuario.text.toString()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_LONG)
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
            val intent = Intent(this, RecuperarPassActivity::class.java)
            startActivity(intent)
        }


    }
}