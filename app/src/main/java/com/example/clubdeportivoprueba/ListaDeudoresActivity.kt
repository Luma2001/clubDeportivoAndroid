package com.example.clubdeportivoprueba

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivoprueba.database.Database
import com.example.clubdeportivoprueba.database.dao.PagoDao
import com.example.clubdeportivoprueba.database.dao.PersonaDao
import com.example.clubdeportivoprueba.database.model.Persona
import com.example.clubdeportivoprueba.database.model.Pago
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ListaDeudoresActivity : AppCompatActivity() {

    private lateinit var database: Database
    private lateinit var personaDao: PersonaDao
    private lateinit var pagoDao: PagoDao

    private lateinit var containerDeudores: LinearLayout
    private lateinit var etFiltrar: EditText
    private lateinit var btnCobrar: Button
    private lateinit var btnVolver: ImageButton

    private var deudores: List<Pair<Persona, Pago?>> = listOf()
    private var deudoresFiltrados: List<Pair<Persona, Pago?>> = listOf()
    private var personaSeleccionada: Persona? = null
    private var radioButtons: MutableList<RadioButton> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lista_deudores)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar base de datos
        database = Database(this)
        val db = database.readableDatabase
        personaDao = PersonaDao(db)
        pagoDao = PagoDao(db)

        inicializarVistas()
        cargarDeudores()
        configurarListeners()
    }

    override fun onResume() {
        super.onResume()
        // recargar la lista cuando la actividad se reanude
        cargarDeudores()
    }

    private fun inicializarVistas() {
        containerDeudores = findViewById(R.id.containerDeudores)
        etFiltrar = findViewById(R.id.etFiltrar)
        btnCobrar = findViewById(R.id.btnCobrar)
        btnVolver = findViewById(R.id.btnVolver)
    }

    private fun cargarDeudores() {
        deudores = pagoDao.getSociosConMembresiaVencidaOEnGracia()
        deudoresFiltrados = deudores
        actualizarLista()

        if (deudores.isEmpty()) {
            mostrarMensajeNoHayDeudores()
        }
    }

    private fun mostrarMensajeNoHayDeudores() {
        containerDeudores.removeAllViews()

        val textView = TextView(this).apply {
            text = "No hay socios con membresía vencida o en período de gracia"
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 50, 0, 0)
            textSize = 16f
        }

        containerDeudores.addView(textView)
        btnCobrar.visibility = Button.GONE
    }

    private fun actualizarLista() {
        containerDeudores.removeAllViews()
        radioButtons.clear()

        if (deudoresFiltrados.isEmpty()) {
            val textView = TextView(this).apply {
                text = "No se encontraron resultados"
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setPadding(0, 50, 0, 0)
                textSize = 16f
            }
            containerDeudores.addView(textView)
            btnCobrar.visibility = Button.GONE
            return
        }

        deudoresFiltrados.forEachIndexed { index, (persona, pago) ->
            val itemView =
                LayoutInflater.from(this).inflate(R.layout.item_deudor, containerDeudores, false)

            val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
            val tvDni: TextView = itemView.findViewById(R.id.tvDni)
            val tvVencimiento: TextView = itemView.findViewById(R.id.tvVencimiento)
            val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
            val tvDiasVencidos: TextView = itemView.findViewById(R.id.tvDiasVencidos)
            val rbSeleccionar: RadioButton = itemView.findViewById(R.id.rbSeleccionar)

            // Configurar datos
            tvNombre.text = "${persona.nombre} ${persona.apellido}"
            tvDni.text = "DNI: ${persona.dni}"

            if (pago == null) {
                // socio que nunca pagó
                tvVencimiento.text = "Sin membresías"
                tvEstado.text = "NUNCA PAGÓ"
                tvEstado.setTextColor(getColor(android.R.color.holo_red_dark))
                tvDiasVencidos.text = "Sin historial de pagos"
            } else {
                // membresía vencida o en gracia
                tvVencimiento.text =
                    "Vence: ${if (pago.fecha_fin != null) formatearFecha(pago.fecha_fin) else null}"

                // Calcular estado y días vencidos
                val fechaVencimiento = LocalDate.parse(pago.fecha_fin)
                val hoy = LocalDate.now()
                val diasVencidos = ChronoUnit.DAYS.between(fechaVencimiento, hoy)

                when {
                    diasVencidos == 0L -> {
                        tvEstado.text = "Vence hoy"
                        tvEstado.setTextColor(getColor(android.R.color.holo_orange_dark))
                        tvDiasVencidos.text = "Último día"
                    }

                    diasVencidos > 0 && diasVencidos <= 10 -> {
                        tvEstado.text = "En período de gracia"
                        tvEstado.setTextColor(getColor(android.R.color.holo_orange_dark))
                        tvDiasVencidos.text = "$diasVencidos días vencido"
                    }

                    diasVencidos > 10 -> {
                        tvEstado.text = "VENCIDO"
                        tvEstado.setTextColor(getColor(android.R.color.holo_red_dark))
                        tvDiasVencidos.text = "$diasVencidos días vencido"
                    }

                    else -> {
                        // Esto no debería ocurrir pero por seguridad
                        tvEstado.text = "Activo"
                        tvEstado.setTextColor(getColor(android.R.color.holo_green_dark))
                        tvDiasVencidos.text = "${-diasVencidos} días restantes"
                    }
                }
            }

            rbSeleccionar.tag = index
            radioButtons.add(rbSeleccionar)

            rbSeleccionar.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // deseleccionar otros radio buttons
                    radioButtons.forEach { rb ->
                        if (rb != rbSeleccionar) {
                            rb.isChecked = false
                        }
                    }
                    personaSeleccionada = persona
                    btnCobrar.visibility = Button.VISIBLE
                } else if (personaSeleccionada?.id == persona.id) {
                    personaSeleccionada = null
                    btnCobrar.visibility = Button.GONE
                }
            }

            itemView.setOnClickListener {
                rbSeleccionar.isChecked = true
            }

            containerDeudores.addView(itemView)
        }
    }

    private fun formatearFecha(fecha: String): String {
        return try {
            val partes = fecha.split("-")
            if (partes.size == 3) {
                "${partes[2]}/${partes[1]}/${partes[0]}"
            } else {
                fecha
            }
        } catch (e: Exception) {
            fecha
        }
    }

    private fun configurarListeners() {
        btnVolver.setOnClickListener {
            finish()
        }

        btnCobrar.setOnClickListener {
            if (personaSeleccionada == null) {
                Toast.makeText(this, "Seleccione un socio para cobrar", Toast.LENGTH_SHORT).show()
            }

            val intent = Intent(this, CobrarActivity2::class.java).apply {
                putExtra("dni", personaSeleccionada!!.dni)
            }
            startActivity(intent)
        }

        etFiltrar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filtrarDeudores(s.toString())
            }
        })
    }

    private fun filtrarDeudores(texto: String) {
        deudoresFiltrados = if (texto.isEmpty()) {
            deudores
        } else {
            deudores.filter { (persona, _) ->
                persona.nombre.contains(texto, true) ||
                        persona.apellido.contains(texto, true) ||
                        persona.dni.contains(texto, true)
            }
        }
        personaSeleccionada = null
        btnCobrar.visibility = Button.GONE
        actualizarLista()
    }
}