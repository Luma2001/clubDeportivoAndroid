package com.example.clubdeportivoprueba

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivoprueba.database.Database
import com.example.clubdeportivoprueba.database.dao.UsuarioDao

class CambiarPasswordActivity : AppCompatActivity() {

    private lateinit var usuarioDao: UsuarioDao
    private lateinit var etNuevaPassword: EditText
    private lateinit var etConfirmarPassword: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var btnVolver: ImageButton

    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cambiar_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener el username del intent
        username = intent.getStringExtra("username") ?: run {
            Toast.makeText(this, "Error: No se pudo identificar el usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Inicializar base de datos
        val database = Database(this)
        usuarioDao = UsuarioDao(database.writableDatabase)

        inicializarVistas()
        configurarListeners()
    }

    private fun inicializarVistas() {
        etNuevaPassword = findViewById(R.id.etNuevaPassword)
        etConfirmarPassword = findViewById(R.id.etConfirmarPassword)
        btnConfirmar = findViewById(R.id.btnConfirmar)
        btnVolver = findViewById(R.id.btnVolver)
    }

    private fun configurarListeners() {
        btnVolver.setOnClickListener {
            finish()
        }

        btnConfirmar.setOnClickListener {
            cambiarPassword()
        }
    }

    private fun cambiarPassword() {
        val nuevaPassword = etNuevaPassword.text.toString().trim()
        val confirmarPassword = etConfirmarPassword.text.toString().trim()

        // Validaciones
        if (nuevaPassword.isEmpty() || confirmarPassword.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (nuevaPassword != confirmarPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        if (nuevaPassword.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        val usuario = usuarioDao.getByUsername(username)
        if (usuario == null) {
            Toast.makeText(this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        val actualizado = usuarioDao.updatePasswordByDNI(usuario.dni, nuevaPassword)

        if (actualizado) {
            Toast.makeText(this, "Contraseña cambiada exitosamente", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "Error al cambiar la contraseña", Toast.LENGTH_SHORT).show()
        }
    }
}