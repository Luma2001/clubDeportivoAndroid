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
import com.example.clubdeportivoprueba.database.dao.EmpleadoDao
import com.example.clubdeportivoprueba.database.dao.UsuarioDao

class CrearCuentaActivity : AppCompatActivity() {

    private lateinit var database: Database
    private lateinit var empleadoDao: EmpleadoDao
    private lateinit var usuarioDao: UsuarioDao

    private lateinit var etDNI: EditText
    private lateinit var etEmail: EditText
    private lateinit var etUsuario: EditText
    private lateinit var etPass: EditText
    private lateinit var etRepeatPass: EditText
    private lateinit var btnCrearCuenta: Button
    private lateinit var btnVolver: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_cuenta)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar base de datos
        database = Database(this)
        val db = database.readableDatabase
        empleadoDao = EmpleadoDao(db)
        usuarioDao = UsuarioDao(db)

        inicializarVistas()
        configurarListeners()
    }

    private fun inicializarVistas() {
        etDNI = findViewById(R.id.etDNI)
        etEmail = findViewById(R.id.etEmail)
        etUsuario = findViewById(R.id.etUsuario)
        etPass = findViewById(R.id.etPass)
        etRepeatPass = findViewById(R.id.etRepeatPass)
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta)
        btnVolver = findViewById(R.id.btnVolver)
    }

    private fun configurarListeners() {
        btnVolver.setOnClickListener {
            finish()
        }

        btnCrearCuenta.setOnClickListener {
            crearCuenta()
        }
    }

    private fun crearCuenta() {
        val dni = etDNI.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val username = etUsuario.text.toString().trim()
        val pass = etPass.text.toString().trim()
        val repeatPass = etRepeatPass.text.toString().trim()

        // Validaciones básicas
        if (dni.isEmpty() || email.isEmpty() || username.isEmpty() || pass.isEmpty() || repeatPass.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        6
        if (pass != repeatPass) {
            Toast.makeText(this, "Las contraseñas deben coincidir", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass.length < 6) {
            Toast.makeText(
                this,
                "La contraseña debe tener al menos 6 caracteres",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Verificar si el empleado existe
        val empleado = empleadoDao.getByDNI(dni)
        if (empleado == null) {
            Toast.makeText(this, "DNI no registrado como empleado", Toast.LENGTH_LONG).show()
            return
        }

        // Verificar si ya existe un usuario con ese DNI o username
        if (usuarioDao.getByDNI(dni) != null) {
            Toast.makeText(this, "El empleado ya tiene una cuenta", Toast.LENGTH_LONG).show()
            return
        }

        if (usuarioDao.getByUsername(username) != null) {
            Toast.makeText(this, "El nombre de usuario ya está en uso", Toast.LENGTH_LONG).show()
            return
        }

        // Crear la cuenta
        try {
            val usuarioId = usuarioDao.insert(dni, email, username, pass, "empleado")
            if (usuarioId != -1L) {
                Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Error al crear la cuenta", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}