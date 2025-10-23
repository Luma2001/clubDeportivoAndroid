package com.example.clubdeportivoprueba

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivoprueba.database.Database
import com.example.clubdeportivoprueba.database.dao.PersonaDao

class CobrarActivity2 : AppCompatActivity() {

    // database
    private lateinit var database: Database
    private lateinit var personaDao: PersonaDao

    // controles
    private lateinit var tvCobrar: TextView
    private lateinit var etMonto: EditText
    private lateinit var rbEfectivo: RadioButton
    private lateinit var rbUnaCuota: RadioButton
    private lateinit var rbDosCuotas: RadioButton
    private lateinit var rbTresCuotas: RadioButton
    private lateinit var btnCobrar: Button
    private lateinit var btnVolver: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cobrar2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val dni = intent.getStringExtra("dni")
        val esSocio = intent.getBooleanExtra("esSocio", false) // bolean para saber si es socio o no socio

        database = Database(this)
        personaDao = PersonaDao(database.readableDatabase)

        tvCobrar = findViewById<TextView>(R.id.tvCobrar)
        etMonto = findViewById<EditText>(R.id.etMonto)
        rbEfectivo = findViewById<RadioButton>(R.id.rbEfectivo)
        rbUnaCuota = findViewById<RadioButton>(R.id.rbUnaCuota)
        rbDosCuotas = findViewById<RadioButton>(R.id.rbDosCuotas)
        rbTresCuotas = findViewById<RadioButton>(R.id.rbTresCuotas)
        btnCobrar = findViewById<Button>(R.id.btnCobrar)
        btnVolver = findViewById<ImageButton>(R.id.btnVolver)

        btnVolver.setOnClickListener {
            finish()
        }

        btnCobrar.setOnClickListener {
            val intent = Intent(this, ComprobanteActivity::class.java)
            startActivity(intent)
        }

        tvCobrar.text = getString(R.string.TituloCobrar).plus(if (esSocio) " Socio" else " No socio")



    }
}