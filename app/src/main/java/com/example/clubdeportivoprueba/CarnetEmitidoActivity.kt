package com.example.clubdeportivoprueba

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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

class CarnetEmitidoActivity : AppCompatActivity() {

    // database
    private lateinit var database: Database
    private lateinit var personaDao: PersonaDao
    private lateinit var pagoDao: PagoDao

    // controles
    private lateinit var tvNombreCompleto: TextView
    private lateinit var tvTipoSocio: TextView
    private lateinit var tvDni: TextView
    private lateinit var tvNumeroSocio: TextView
    private lateinit var tvValidoHasta: TextView
    private lateinit var tvCodigoBarras: TextView
    private lateinit var carnetContainer: LinearLayout
    private lateinit var btnCompartirCarnet: Button
    private lateinit var btnVolver: ImageButton

    private var dni: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_carnet_emitido)
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

        // Obtener DNI del intent
        dni = intent.getStringExtra("dni") ?: run {
            Toast.makeText(this, "Error: No se recibió el DNI", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()

        // validar que se puede emitir el carnet
        if (!validarPuedeEmitirCarnet()) {
            return
        }

        setupCarnetData()
        setListeners()
    }

    private fun validarPuedeEmitirCarnet(): Boolean {
        val persona = personaDao.getPersonByDNI(dni)

        if (persona == null) {
            Toast.makeText(this, "Error: No se encontró la persona", Toast.LENGTH_SHORT).show()
            finish()
            return false
        }

        // verificar que sea socio
        if (!persona.esSocio) {
            Toast.makeText(this, "Error: Solo se pueden emitir carnets para socios", Toast.LENGTH_SHORT).show()
            finish()
            return false
        }

        // validar estado de membresia
        val (puedeEmitir, mensaje) = pagoDao.puedeEmitirCarnet(persona.id)
        if (!puedeEmitir) {
            Toast.makeText(this, "No se puede emitir carnet: $mensaje", Toast.LENGTH_LONG).show()
            finish()
            return false
        }

        return true
    }

    private fun initializeViews() {
        tvNombreCompleto = findViewById(R.id.tvNombreCompleto)
        tvTipoSocio = findViewById(R.id.tvTipoSocio)
        tvDni = findViewById(R.id.tvDni)
        tvNumeroSocio = findViewById(R.id.tvNumeroSocio)
        tvValidoHasta = findViewById(R.id.tvValidoHasta)
        tvCodigoBarras = findViewById(R.id.tvCodigoBarras)
        carnetContainer = findViewById(R.id.carnetContainer)
        btnCompartirCarnet = findViewById(R.id.btnCompartirCarnet)
        btnVolver = findViewById(R.id.btnVolver)
    }

    private fun setupCarnetData() {
        // Obtener información de la persona
        val persona = personaDao.getPersonByDNI(dni)!!

        // Obtener la última membresía activa
        val ultimaMembresia = pagoDao.getUltimaMembresia(persona.id)
        val fechaValidez = if (ultimaMembresia != null && ultimaMembresia.fecha_fin != null) {
            ultimaMembresia.fecha_fin.substring(0, 10) // Formato YYYY-MM-DD
        } else {
            // Si no tiene membresía activa, usar fecha por defecto (30 días desde hoy)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 30)
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        }

        // Configurar datos del carnet
        tvNombreCompleto.text = "${persona.nombre} ${persona.apellido}".uppercase()
        tvTipoSocio.text = "SOCIO ACTIVO"
        tvDni.text = persona.dni
        tvNumeroSocio.text = generarNumeroSocio(persona.id)
        tvValidoHasta.text = formatearFecha(fechaValidez)
        tvCodigoBarras.text = "CD ${String.format("%06d", persona.id)} ${persona.dni}"
    }

    private fun generarNumeroSocio(personaId: Long): String {
        return "CD-${String.format("%06d", personaId)}"
    }

    private fun formatearFecha(fecha: String): String {
        return try {
            // Convertir de YYYY-MM-DD a DD/MM/YYYY
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

    private fun setListeners() {
        btnVolver.setOnClickListener {
            finish()
        }

        btnCompartirCarnet.setOnClickListener {
            compartirCarnet()
        }
    }

    private fun compartirCarnet() {
        try {
            // Crear imagen del carnet
            val bitmap = crearBitmapDelCarnet()
            val archivo = guardarBitmapComoArchivo(bitmap, "carnet")

            // Crear intent para compartir
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                archivo
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Carnet de Socio - Club Deportivo")
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Compartir carnet"))

        } catch (e: Exception) {
            Toast.makeText(this, "Error al compartir el carnet", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun crearBitmapDelCarnet(): Bitmap {
        // Medir y layout el contenedor del carnet
        carnetContainer.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val width = carnetContainer.measuredWidth
        val height = carnetContainer.measuredHeight

        carnetContainer.layout(0, 0, width, height)

        // Crear bitmap
        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        carnetContainer.draw(canvas)

        return bitmap
    }

    private fun guardarBitmapComoArchivo(bitmap: Bitmap, prefix: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${prefix}_$timeStamp.png"

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