package com.example.clubdeportivoprueba

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivoprueba.database.Database
import com.example.clubdeportivoprueba.database.dao.ActividadDao
import com.example.clubdeportivoprueba.database.dao.PagoDao
import com.example.clubdeportivoprueba.database.dao.ParametroDao
import com.example.clubdeportivoprueba.database.dao.PersonaDao
import com.example.clubdeportivoprueba.database.model.Actividad
import java.time.LocalDate

class CobrarActivity2 : AppCompatActivity() {

    // database
    private lateinit var database: Database
    private lateinit var personaDao: PersonaDao
    private lateinit var actividadDao: ActividadDao
    private lateinit var parametroDao: ParametroDao
    private lateinit var pagoDao: PagoDao


    // controles
    private lateinit var tvCobrar: TextView
    private lateinit var llActividades: LinearLayout
    private lateinit var spnActividad: Spinner
    private lateinit var rbEfectivo: RadioButton
    private lateinit var rbUnaCuota: RadioButton
    private lateinit var rbTresCuotas: RadioButton
    private lateinit var rbSeisCuotas: RadioButton
    private lateinit var btnCobrar: Button
    private lateinit var btnVolver: ImageButton

    private lateinit var tvConceptoTitulo: TextView
    private lateinit var tvConceptoValor: TextView
    private lateinit var tvActividadLabel: TextView
    private lateinit var tvActividadValor: TextView
    private lateinit var tvMontoLabel: TextView
    private lateinit var tvMontoValor: TextView
    private lateinit var tvPeriodoLabel: TextView
    private lateinit var tvPeriodoValor: TextView
    private lateinit var tvEstadoMembresiaLabel: TextView
    private lateinit var tvEstadoMembresiaValor: TextView
    private var currentPersonaId: Long = -1
    private var montoBase: Float = 0f

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
        pagoDao = PagoDao(db)

        // enlazar vistas
        tvCobrar = findViewById<TextView>(R.id.tvCobrar)
        llActividades = findViewById<LinearLayout>(R.id.llActividades)
        spnActividad = findViewById<Spinner>(R.id.spnActividad)

        tvConceptoTitulo = findViewById(R.id.tvConceptoTitulo)
        tvConceptoValor = findViewById(R.id.tvConceptoValor)
        tvActividadLabel = findViewById(R.id.tvActividadLabel)
        tvActividadValor = findViewById(R.id.tvActividadValor)
        tvMontoLabel = findViewById(R.id.tvMontoLabel)
        tvMontoValor = findViewById(R.id.tvMontoValor)
        tvPeriodoLabel = findViewById(R.id.tvPeriodoLabel)
        tvPeriodoValor = findViewById(R.id.tvPeriodoValor)
        tvEstadoMembresiaLabel = findViewById(R.id.tvEstadoMembresiaLabel)
        tvEstadoMembresiaValor = findViewById(R.id.tvEstadoMembresiaValor)

        rbEfectivo = findViewById<RadioButton>(R.id.rbEfectivo)
        rbUnaCuota = findViewById<RadioButton>(R.id.rbUnaCuota)
        rbTresCuotas = findViewById<RadioButton>(R.id.rbTresCuotas)
        rbSeisCuotas = findViewById<RadioButton>(R.id.rbSeisCuotas)
        btnCobrar = findViewById<Button>(R.id.btnCobrar)
        btnVolver = findViewById<ImageButton>(R.id.btnVolver)

        setupMetodoPagoListener()

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

        currentPersonaId = persona.id

        val esSocio = persona.esSocio

        // configurar titulo
        tvCobrar.text =
            getString(R.string.TituloCobrar).plus(if (esSocio) " Socio" else " No socio")

        // mostrar u ocultar actividades
        setupActivities(esSocio)
        actualizarDetallePago(esSocio, currentPersonaId)

        // listener botones
        btnVolver.setOnClickListener {
            finish()
        }

        btnCobrar.setOnClickListener {
            mostrarDialogoConfirmarPago(esSocio, montoBase)
        }
    }

    private fun setupActivities(esSocio: Boolean) {
        if (esSocio) {
            // ocultar spinner de actividades
            llActividades.visibility = View.GONE
            val cuotaSocio = parametroDao.getCuotaMensualSocio()


        } else {
            // mostrar spiner y cargar actividades
            llActividades.visibility = View.VISIBLE


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
                    actualizarDetallePago(esSocio = false, currentPersonaId)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }

            // seleccionar el primero por defecto
            spnActividad.setSelection(0)
        }
    }

    private fun actualizarDetallePago(esSocio: Boolean, personaId: Long) {
        if (esSocio) {
            // Socio: Pago de cuota mensual
            val cuota = parametroDao.getCuotaMensualSocio()
            val hoy = LocalDate.now().toString()

            // Obtener última membresía activa (si existe)
            val ultimaMembresia = pagoDao.getUltimaMembresiaActiva(personaId, hoy)

            // Calcular período
            val fechaInicio = hoy
            val fechaFin = LocalDate.parse(hoy).plusDays(30).toString()

            // Mostrar datos
            tvConceptoValor.text = "Cuota Mensual"
            tvMontoValor.text = String.format("%.0f", cuota)
            tvPeriodoLabel.visibility = View.VISIBLE
            tvPeriodoValor.visibility = View.VISIBLE
            tvPeriodoValor.text = "$fechaInicio al $fechaFin"

            // Estado de membresía
            if (ultimaMembresia != null) {
                tvEstadoMembresiaLabel.visibility = View.VISIBLE
                tvEstadoMembresiaValor.visibility = View.VISIBLE
                tvEstadoMembresiaValor.text = "Vencida. Renovando desde $fechaInicio."
            } else {
                tvEstadoMembresiaLabel.visibility = View.VISIBLE
                tvEstadoMembresiaValor.visibility = View.VISIBLE
                tvEstadoMembresiaValor.text = "Nueva membresía."
            }

            // Ocultar actividad
            tvActividadLabel.visibility = View.GONE
            tvActividadValor.visibility = View.GONE

            montoBase = cuota

        } else {
            // No socio: Pago por actividad
            val actividadSeleccionada = actividades[spnActividad.selectedItemPosition]
            val monto = actividadSeleccionada.precio

            tvConceptoValor.text = "Pago por Actividad"
            tvActividadLabel.visibility = View.VISIBLE
            tvActividadValor.visibility = View.VISIBLE
            tvActividadValor.text = actividadSeleccionada.nombre
            tvMontoValor.text = String.format("%.0f", monto)

            // Ocultar período y estado
            tvPeriodoLabel.visibility = View.GONE
            tvPeriodoValor.visibility = View.GONE
            tvEstadoMembresiaLabel.visibility = View.GONE
            tvEstadoMembresiaValor.visibility = View.GONE

            montoBase = actividadSeleccionada.precio
        }
    }

    private fun calcularMontoFinal(montoBase: Float): Float {
        return when {
            rbUnaCuota.isChecked || rbEfectivo.isChecked -> montoBase
            rbTresCuotas.isChecked -> montoBase * 0.9f // 10% de descuento
            rbSeisCuotas.isChecked -> montoBase * 0.8f // 20% de descuento
            else -> montoBase
        }
    }

    private fun setupMetodoPagoListener() {
        val listener = View.OnClickListener {
            actualizarMontoConDescuento()
        }
        rbEfectivo.setOnClickListener(listener)
        rbUnaCuota.setOnClickListener(listener)
        rbTresCuotas.setOnClickListener(listener)
        rbSeisCuotas.setOnClickListener(listener)
    }

    private fun actualizarMontoConDescuento() {
        val montoFinal = calcularMontoFinal(montoBase)
        tvMontoValor.text = String.format("%.0f", montoFinal)
    }

    private fun mostrarDialogoConfirmarPago(esSocio: Boolean, montoBase: Float) {
        val montoFinal = calcularMontoFinal(montoBase)

        val concepto = if (esSocio) {
            "Cuota mensual de Socio"
        } else {
            val actividad = actividades.getOrNull(spnActividad.selectedItemPosition)
            "Actividad: ${actividad?.nombre ?: "N/A"}"
        }

        val metodoPago = when {
            rbEfectivo.isChecked -> "Efectivo"
            rbUnaCuota.isChecked -> "1 cuota"
            rbTresCuotas.isChecked -> "3 cuotas (10% desc.)"
            rbSeisCuotas.isChecked -> "6 cuotas (20% desc.)"
            else -> "No especificado"
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar Pago")
            .setMessage(
                "¿Desea confirmar el siguiente pago?\n"
                        + "\nConcepto: $concepto"
                        + "\nMonto: $${String.format("%.0f", montoFinal)}"
                        + "\nMétodo de pago: $metodoPago"
            )
            .setPositiveButton("Aceptar") { dialog, which ->
                registrarPago(montoFinal, esSocio)
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }
            .setCancelable(false) // evitar que se cierre tocando fuera del dialog

        val dialog = builder.create()
        dialog.show()
    }

    private fun registrarPago(montoFinal: Float, esSocio: Boolean) {
        val pagoId: Long
        if (esSocio) {
            pagoId = pagoDao.insert(
                currentPersonaId, "cuota_mensual", montoFinal, null
            )
        } else {
            val actividad = actividades[spnActividad.selectedItemPosition]
            pagoId = pagoDao.insert(
                currentPersonaId, "actividad", montoFinal, actividad.id.toInt()
            )
        }

        if (pagoId == -1L) {
            Toast.makeText(this, "Error al registrar el pago", Toast.LENGTH_SHORT).show()
            return
        }

        // Navegar al comprobante
        val intent = Intent(this, ComprobanteActivity::class.java).apply {
            putExtra("pagoId", pagoId)
            putExtra("monto", montoFinal)
            putExtra("esSocio", esSocio)
            putExtra(
                "actividad",
                if (!esSocio) actividades[spnActividad.selectedItemPosition].nombre else null
            )
        }
        startActivity(intent)

        // finalizar esta actividad para que no quede en el stack
        finish()
    }
}