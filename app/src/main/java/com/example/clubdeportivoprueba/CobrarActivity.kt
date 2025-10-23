package com.example.clubdeportivoprueba

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivoprueba.database.Database
import com.example.clubdeportivoprueba.database.dao.PersonaDao

class CobrarActivity : AppCompatActivity() {

    // database
    private lateinit var database: Database
    private lateinit var personaDao: PersonaDao

    // controles
    private lateinit var btnSiguiente: Button
    private lateinit var btnVolver: ImageButton
    private lateinit var btnVerificar: Button
    private lateinit var etDNI: EditText
    private lateinit var tvDNI: TextView
    private lateinit var tvApellido: TextView
    private lateinit var tvNombre: TextView
    private lateinit var tvTipo: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cobrar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = Database(this)
        personaDao = PersonaDao(database.readableDatabase)

        btnSiguiente = findViewById<Button>(R.id.btnSiguiente)
        btnVolver = findViewById<ImageButton>(R.id.btnVolver)
        btnVerificar = findViewById<Button>(R.id.btnVerificar)
        etDNI = findViewById<EditText>(R.id.etDNI)
        tvDNI = findViewById<TextView>(R.id.tvDNI)
        tvApellido = findViewById<TextView>(R.id.tvApellido)
        tvNombre = findViewById<TextView>(R.id.tvNombre)
        tvTipo = findViewById<TextView>(R.id.tvTipo)

        btnVolver.setOnClickListener {
            finish()
        }

        btnVerificar.setOnClickListener {
            val dni = etDNI.text.toString().trim()

            if (dni.isEmpty()) {
                cleanFields()
                return@setOnClickListener
            }

            // buscar persona por dni
            val person = personaDao.getPersonByDNI(dni)

            if (person == null) {
                Toast.makeText(this, "No existe una persona con ese DNI", Toast.LENGTH_SHORT).show()
                cleanFields()
            } else {
                tvDNI.text = getString(R.string.datoDNI).plus(" ${person.dni}")
                tvApellido.text = getString(R.string.datoApellido).plus(" ${person.apellido}")
                tvNombre.text = getString(R.string.datoNombre).plus(" ${person.nombre}")
                tvTipo.text =
                    getString(R.string.datoTipo).plus(" ${if (person.esSocio) "Socio" else "No Socio"}")
            }
        }

        btnSiguiente.setOnClickListener {
            // verificar que esté cargado el dni antes de continuar
            val tvDNIvalue = tvDNI.text.toString().trim()
            val dniNumber = tvDNIvalue.replace("DNI:", "").trim()

            if (dniNumber != "") {
                val intent = Intent(this, CobrarActivity2::class.java)
                // enviar los datos a la siguiente pantalla
                val tvTipoValue = tvTipo.text.toString().trim()
                val tipo = tvTipoValue.replace("TIPO:", "").trim()

                intent.putExtra("esSocio", if (tipo == "Socio") true else false)
                intent.putExtra("dni", dniNumber)
                startActivity(intent)
                cleanFields()
            } else {
                Toast.makeText(this, "Verifique el DNI antes de continuar", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun cleanFields() {
        tvDNI.text = getString(R.string.datoDNI)
        tvApellido.text = getString(R.string.datoApellido)
        tvNombre.text = getString(R.string.datoNombre)
        tvTipo.text = getString(R.string.datoTipo)
        etDNI.text?.clear()
    }
}