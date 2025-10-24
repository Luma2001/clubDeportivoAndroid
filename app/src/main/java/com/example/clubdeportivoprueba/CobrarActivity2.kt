package com.example.clubdeportivoprueba

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivoprueba.database.Database
import com.example.clubdeportivoprueba.database.dao.ActividadDao
import com.example.clubdeportivoprueba.database.dao.ParametroDao
import com.example.clubdeportivoprueba.database.dao.PersonaDao
import com.example.clubdeportivoprueba.database.model.Actividad

class CobrarActivity2 : AppCompatActivity() {

    // database
    private lateinit var database: Database
    private lateinit var personaDao: PersonaDao
    private lateinit var actividadDao: ActividadDao
    private lateinit var parametroDao: ParametroDao


    // controles
    private lateinit var tvCobrar: TextView
    private lateinit var llActividades: LinearLayout
    private lateinit var spnActividad: Spinner
    private lateinit var etMonto: EditText
    private lateinit var rbEfectivo: RadioButton
    private lateinit var rbUnaCuota: RadioButton
    private lateinit var rbDosCuotas: RadioButton
    private lateinit var rbTresCuotas: RadioButton
    private lateinit var btnCobrar: Button
    private lateinit var btnVolver: ImageButton

    // lista de actividades
    private var actividades: List<Actividad> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cobrar2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // inicializar base de datos y DAOs
        database = Database(this)
        val db = database.readableDatabase
        personaDao = PersonaDao(db)
        actividadDao = ActividadDao(db)
        parametroDao = ParametroDao(db)

        // enlazar vistas
        tvCobrar = findViewById<TextView>(R.id.tvCobrar)
        llActividades = findViewById<LinearLayout>(R.id.llActividades)
        spnActividad = findViewById<Spinner>(R.id.spnActividad)
        etMonto = findViewById<EditText>(R.id.etMonto)
        rbEfectivo = findViewById<RadioButton>(R.id.rbEfectivo)
        rbUnaCuota = findViewById<RadioButton>(R.id.rbUnaCuota)
        rbDosCuotas = findViewById<RadioButton>(R.id.rbDosCuotas)
        rbTresCuotas = findViewById<RadioButton>(R.id.rbTresCuotas)
        btnCobrar = findViewById<Button>(R.id.btnCobrar)
        btnVolver = findViewById<ImageButton>(R.id.btnVolver)

        val dni = intent.getStringExtra("dni") ?: run {
            Toast.makeText(this, "DNI no recibido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val persona = personaDao.getPersonByDNI(dni) ?: run {
            Toast.makeText(this, "Persona no encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val esSocio = persona.esSocio

        // configurar titulo
        tvCobrar.text =
            getString(R.string.TituloCobrar).plus(if (esSocio) " Socio" else " No socio")

        // mostrar u ocultar actividades
        setupActivities(esSocio)

        // listener botones
        btnVolver.setOnClickListener {
            finish()
        }

        btnCobrar.setOnClickListener {
            val intent = Intent(this, ComprobanteActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupActivities(esSocio: Boolean) {
        if (esSocio) {
            // ocultar spinner de actividades
            llActividades.visibility = View.GONE
            val cuotaSocio = parametroDao.getCuotaMensualSocio()

            etMonto.isEnabled = false
            etMonto.setText(cuotaSocio.toString())
        } else {
            // mostrar spiner y cargar actividades
            llActividades.visibility = View.VISIBLE
            etMonto.isEnabled = false // el monto lo define la actividad

            actividades = actividadDao.getActivities()

            if (actividades.isEmpty()) {
                Toast.makeText(this, "No hay actividades disponibles", Toast.LENGTH_SHORT).show()
                return
            }

            // adaptador para el spinner
            val activitieNames = actividades.map { it.nombre }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activitieNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spnActividad.adapter = adapter

            //Escuchar selección
            spnActividad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val actividadSeleccionada = actividades[position]
                    etMonto.setText(actividadSeleccionada.precio.toString())
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    etMonto.setText("")
                }
            }

            // seleccionar el primero por defecto
            spnActividad.setSelection(0)
        }
    }
}