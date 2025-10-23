package com.example.clubdeportivoprueba

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivoprueba.database.Database
import com.example.clubdeportivoprueba.database.dao.PersonaDao
import com.example.clubdeportivoprueba.database.model.Persona

class RegistrarActivity : AppCompatActivity() {

    private lateinit var database: Database
    private lateinit var personaDao: PersonaDao

    private lateinit var etNombre: EditText
    private lateinit var etApellido: EditText
    private lateinit var etDNI: EditText
    private lateinit var etDireccion: EditText
    private lateinit var cbSocio: CheckBox
    private lateinit var cbAptoFisico: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnVolver = findViewById<ImageButton>(R.id.btnVolver)
        btnVolver.setOnClickListener {
            finish()
        }

        database = Database(this)
        personaDao = PersonaDao(database.writableDatabase)


        etNombre = findViewById(R.id.etNombre)
        etApellido = findViewById(R.id.etApellido)
        etDNI = findViewById(R.id.etDNI)
        etDireccion = findViewById(R.id.etDireccion)
        cbSocio = findViewById(R.id.cbSocio)
        cbAptoFisico = findViewById(R.id.cbAptoFisico)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val apellido = etApellido.text.toString()
            val dni = etDNI.text.toString()

            if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor, complete nombre, apellido y DNI",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (!cbAptoFisico.isChecked) {
                Toast.makeText(this, "Es necesario el apto físico", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!checkDNI(dni)) {
                Toast.makeText(this, "Ya existe una persona con ese DNI", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(this).apply {
                setTitle("Confirmar registro")
                setMessage("¿Está seguro de que desea registrar a esta persona?")

                setPositiveButton("Aceptar") { dialog, which ->
                    // Obtener los valores de los campos de texto y checkboxes
                    val direccion = etDireccion.text.toString()
                    val esSocio = cbSocio.isChecked

                    // Llamar a la función para registrar
                    registerPerson(nombre, apellido, dni, direccion, esSocio)
                }

                setNegativeButton("Cancelar") { dialog, which ->
                    // solo cerrar el dialog
                }

                create()
                show()
            }
        }
    }

    private fun checkDNI(dni: String): Boolean {
        // Opcional: una validación más robusta del DNI
        if (dni.length < 7 || dni.length > 8) {
            Toast.makeText(this, "El DNI debe tener entre 7 y 8 dígitos", Toast.LENGTH_SHORT).show()
            return false
        }
        val person = personaDao.getPersonByDNI(dni)
        return person == null
    }

    private fun registerPerson(
        nombre: String,
        apellido: String,
        dni: String,
        direccion: String,
        esSocio: Boolean
    ) {
        val persona = Persona(
            nombre = nombre,
            apellido = apellido,
            dni = dni,
            direccion = direccion,
            esSocio = esSocio
        )
        // Inserta la persona en la base de datos
        val newId = personaDao.insert(persona)

        // Verificar si la inserción fue exitosa
        if (newId > -1) {
            Toast.makeText(this, "Persona registrada con éxito", Toast.LENGTH_LONG).show()
            finish() // Cierra la actividad después de registrar
        } else {
            Toast.makeText(this, "Error al registrar la persona", Toast.LENGTH_LONG).show()
        }
    }
}