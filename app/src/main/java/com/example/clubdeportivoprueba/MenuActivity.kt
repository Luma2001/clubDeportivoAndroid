package com.example.clubdeportivoprueba

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnRegistrarPersona = findViewById<Button>(R.id.btnRegistrarPersona)
        btnRegistrarPersona.setOnClickListener{
            val intentRP = Intent(this, RegistrarActivity::class.java)
            startActivity(intentRP)
        }

        val btnListaDeudores = findViewById<Button>(R.id.btnListaDeudores)
        btnListaDeudores.setOnClickListener{
            val intentLD = Intent(this, ListaDeudoresActivity::class.java)
            startActivity(intentLD)
        }

        val btnEmitirCarnet = findViewById<Button>(R.id.btnEmitirCarnet)
        btnEmitirCarnet.setOnClickListener{
            val intentEC = Intent(this, EmitirCarnetActivity::class.java)
            startActivity(intentEC)
        }

        val btnCobrar = findViewById<Button>(R.id.btnCobrar)
        btnCobrar.setOnClickListener{
            val intentC = Intent(this, CobrarActivity::class.java)
            startActivity(intentC)
        }

        val btnCerrarSesion = findViewById<Button>(R.id.btnLogout)
        btnCerrarSesion.setOnClickListener{
            val intentCS = Intent(this, MainActivity::class.java)
            startActivity(intentCS)
        }
    }
}