package com.example.clubdeportivoprueba

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivoprueba.database.Database
import com.example.clubdeportivoprueba.database.dao.PagoDao
import com.example.clubdeportivoprueba.database.dao.PersonaDao
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ComprobanteActivity : AppCompatActivity() {

    private lateinit var database: Database
    private lateinit var personaDao: PersonaDao
    private lateinit var pagoDao: PagoDao

    private lateinit var tvNumeroComprobante: TextView
    private lateinit var tvConcepto: TextView
    private lateinit var tvMonto: TextView
    private lateinit var tvFecha: TextView
    private lateinit var tvMetodoPago: TextView
    private lateinit var tvPeriodo: TextView
    private lateinit var tvNombreSocio: TextView
    private lateinit var tvDniSocio: TextView
    private lateinit var comprobanteContainer: LinearLayout
    private lateinit var llPeriodo: LinearLayout
    private lateinit var btnCompartir: Button
    private lateinit var btnVolver: ImageButton

    private var pagoId: Long = -1
    private var monto: Float = 0f
    private var esSocio: Boolean = true
    private var actividadNombre: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comprobante)
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

        // Obtener datos del intent
        pagoId = intent.getLongExtra("pagoId", -1)
        monto = intent.getFloatExtra("monto", 0f)
        esSocio = intent.getBooleanExtra("esSocio", true)
        actividadNombre = intent.getStringExtra("actividad")

        // Enlazar vistas
        inicializarVistas()
        cargarDatosComprobante()
        configurarListeners()
    }

    private fun inicializarVistas() {
        tvNumeroComprobante = findViewById(R.id.tvNumeroComprobante)
        tvConcepto = findViewById(R.id.tvConcepto)
        tvMonto = findViewById(R.id.tvMonto)
        tvFecha = findViewById(R.id.tvFecha)
        tvMetodoPago = findViewById(R.id.tvMetodoPago)
        tvPeriodo = findViewById(R.id.tvPeriodo)
        tvNombreSocio = findViewById(R.id.tvNombreSocio)
        tvDniSocio = findViewById(R.id.tvDniSocio)
        comprobanteContainer = findViewById(R.id.comprobanteContainer)
        llPeriodo = findViewById<LinearLayout>(R.id.llPeriodo)
        btnCompartir = findViewById(R.id.btnCompartir)
        btnVolver = findViewById(R.id.btnVolver)
    }

    private fun cargarDatosComprobante() {
        if (pagoId == -1L) {
            Toast.makeText(this, "Error: No se recibió información del pago", Toast.LENGTH_SHORT)
                .show()
            finish()
            return
        }

        // Obtener información del pago
        val pago = pagoDao.getPagoById(pagoId)

        if (pago == null) {
            Toast.makeText(this, "Error: No se encontró el pago", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val persona = personaDao.getPersonById(pago.id_persona)

        if (persona == null) {
            Toast.makeText(
                this,
                "Error: No se encontró la información del pago",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        // Formatear fecha actual
        val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        // Configurar datos del comprobante
        tvNumeroComprobante.text = "#${String.format("%06d", pagoId)}"
        tvConcepto.text = if (esSocio) "Cuota Mensual de Socio" else "Actividad: $actividadNombre"
        tvMonto.text = "$${String.format("%.0f", monto)}"
        tvFecha.text = fechaActual
        tvMetodoPago.text = "Efectivo"

        // Configurar período
        if (!esSocio) {
            llPeriodo.visibility = View.GONE
        } else if (pago.fecha_inicio != null && pago.fecha_fin != null) {
            val fechaInicio = pago.fecha_inicio.substring(0, 10)
            val fechaFin = pago.fecha_fin.substring(0, 10)
            tvPeriodo.text = "$fechaInicio al $fechaFin"
        } else {
            findViewById<LinearLayout>(R.id.comprobanteContainer)
                .findViewWithTag<LinearLayout>("periodoLayout")?.visibility = View.GONE
        }

        // Información del socio/persona
        tvNombreSocio.text = "${persona.nombre} ${persona.apellido}"
        tvDniSocio.text = persona.dni
    }

    private fun configurarListeners() {
        btnVolver.setOnClickListener {
            finish()
        }

        btnCompartir.setOnClickListener {
            compartirComprobante()
        }
    }

    private fun compartirComprobante() {
        try {
            // Crear imagen del comprobante
            val bitmap = crearBitmapDelComprobante()
            val archivo = guardarBitmapComoArchivo(bitmap)

            // Crear intent para compartir
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                archivo
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Compartir comprobante"))

        } catch (e: Exception) {
            Toast.makeText(this, "Error al compartir el comprobante", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun crearBitmapDelComprobante(): Bitmap {
        // Medir y layout el contenedor
        comprobanteContainer.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        comprobanteContainer.layout(
            0,
            0,
            comprobanteContainer.measuredWidth,
            comprobanteContainer.measuredHeight
        )

        // Crear bitmap
        val bitmap = Bitmap.createBitmap(
            comprobanteContainer.width,
            comprobanteContainer.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        comprobanteContainer.draw(canvas)

        return bitmap
    }

    private fun guardarBitmapComoArchivo(bitmap: Bitmap): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "comprobante_$timeStamp.png"

        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(storageDir, fileName)

        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file
    }
}