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
import com.example.clubdeportivoprueba.database.dao.PagoDao
import com.example.clubdeportivoprueba.database.dao.PersonaDao

class EmitirCarnetActivity : AppCompatActivity() {

    // database
    private lateinit var database: Database
    private lateinit var personaDao: PersonaDao
    private lateinit var pagoDao: PagoDao

    // controles
    private lateinit var btnVolver: ImageButton
    private lateinit var btnVerificar: Button
    private lateinit var btnEmitir: Button

    private lateinit var etDNI: EditText
    private lateinit var tvDNI: TextView
    private lateinit var tvApellido: TextView
    private lateinit var tvNombre: TextView
    private lateinit var tvTipo: TextView
    private lateinit var tvEstadoMembresia: TextView

    private var esSocio: Boolean = false
    private var puedeEmitirCarnet : Boolean = false
    private var currentPersonaId: Long = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_emitir_carnet)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        initializeDatabase()
        initializeViews()
        setListeners()
    }

    fun emitCarnet() {
        if (!esSocio) {
            Toast.makeText(this, "El carnet es solo para socios", Toast.LENGTH_SHORT).show()
            cleanFields()
            return
        }

        if (!puedeEmitirCarnet) {
            Toast.makeText(this, "No se puede emitir carnet. Estado de membresía no válido", Toast.LENGTH_LONG).show()
            cleanFields()
            return
        }

        val dni = etDNI.text.toString().trim()

        if (dni.isEmpty()) {
            Toast.makeText(this, "Ingrese un DNI para continuar", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, CarnetEmitidoActivity::class.java)
        intent.putExtra("dni", dni)
        startActivity(intent)
        cleanFields()
        finish()
    }

    fun verifyDni() {
        val dni = etDNI.text.toString().trim()

        if (dni.isEmpty()) {
            cleanFields()
            Toast.makeText(this, "Ingrese un DNI", Toast.LENGTH_SHORT).show()
            return
        }

        // buscar persona por dni
        val person = personaDao.getPersonByDNI(dni)

        if (person == null) {
            Toast.makeText(this, "No existe una persona con ese DNI", Toast.LENGTH_SHORT).show()
            cleanFields()
            return
        }

        currentPersonaId = person.id
        esSocio = person.esSocio

        tvDNI.text = person.dni
        tvApellido.text = person.apellido
        tvNombre.text = person.nombre
        tvTipo.text = if (esSocio) "Socio" else "No socio"
        tvTipo.setTextColor(
            if (esSocio) getColor(android.R.color.holo_green_dark) else getColor(
                android.R.color.holo_red_dark
            )
        )

        // verificar estado de memebresia si es socio
        if (esSocio) {
            val (puedeEmitir, mensaje) = pagoDao.puedeEmitirCarnet(currentPersonaId)
            this.puedeEmitirCarnet = puedeEmitir

            tvEstadoMembresia.visibility = TextView.VISIBLE
            tvEstadoMembresia.text = mensaje

            when {
                puedeEmitir -> {
                    tvEstadoMembresia.setTextColor(getColor(android.R.color.holo_green_dark))
//                    tvEstadoMembresia.setBackgroundColor(getColor(android.R.color.holo_green_light))
                }
                else -> {
                    tvEstadoMembresia.setTextColor(getColor(android.R.color.holo_red_dark))
//                    tvEstadoMembresia.setBackgroundColor(getColor(android.R.color.holo_red_light))
                }
            }
        } else {
            tvEstadoMembresia.visibility = TextView.GONE
            this.puedeEmitirCarnet = false
        }
    }

    private fun cleanFields() {
        etDNI.text.clear()
        tvDNI.text = ""
        tvApellido.text = ""
        tvNombre.text = ""
        tvTipo.text = ""
        tvEstadoMembresia.visibility = TextView.GONE
        esSocio = false
        puedeEmitirCarnet = false
        currentPersonaId = -1
    }

    fun initializeDatabase() {
        database = Database(this)
        val db = database.readableDatabase
        personaDao = PersonaDao(db)
        pagoDao = PagoDao(db)
    }

    fun initializeViews() {
        btnVolver = findViewById<ImageButton>(R.id.btnVolver)
        btnVerificar = findViewById<Button>(R.id.btnVerificar)
        btnEmitir = findViewById<Button>(R.id.btnEmitir)

        etDNI = findViewById<EditText>(R.id.etDNI)
        tvDNI = findViewById<TextView>(R.id.tvDNI)
        tvApellido = findViewById<TextView>(R.id.tvApellido)
        tvNombre = findViewById<TextView>(R.id.tvNombre)
        tvTipo = findViewById<TextView>(R.id.tvTipo)
        tvEstadoMembresia = findViewById<TextView>(R.id.tvEstadoMembresia)
    }

    fun setListeners() {
        btnVolver.setOnClickListener {
            finish()
        }

        btnVerificar.setOnClickListener {
            verifyDni()
        }

        btnEmitir.setOnClickListener {
            emitCarnet()
        }
    }
}