package com.example.clubdeportivoprueba

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MenuActivity : AppCompatActivity() {
    private var username: String = ""
    private var rol: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setMenuOptions()

        username = intent.getStringExtra("user") ?: "Usuario"
        rol = intent.getStringExtra("rol") ?: "empleado"

        val tvBienvenida = findViewById<TextView>(R.id.tvBienvenida)
        tvBienvenida.text = "¡Bienvenido, $username!"


        setButtons()
    }

    private fun setMenuOptions() {
        val btnMenuOpciones = findViewById<ImageButton>(R.id.btnMenuOpciones)

        btnMenuOpciones.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_cambiar_password -> {
                        val intent = Intent(this, CambiarPasswordActivity::class.java)
                        intent.putExtra("username", username)
                        startActivity(intent)
                        true
                    }

                    R.id.menu_cerrar_sesion -> {
                        AlertDialog.Builder(this)
                            .setTitle("Cerrar sesión")
                            .setMessage("¿Desea cerrar sesión?")
                            .setPositiveButton("Sí") { _, _ -> finish() }
                            .setNegativeButton("No", null)
                            .show()
                        true
                    }

                    else -> false
                }
            }

            popupMenu.show()
        }
    }

    private fun setButtons() {
        val btnRegistrarPersona = findViewById<Button>(R.id.btnRegistrarPersona)
        val btnCobrar = findViewById<Button>(R.id.btnCobrar)
        val btnEmitirCarnet = findViewById<Button>(R.id.btnEmitirCarnet)
        val btnListaDeudores = findViewById<Button>(R.id.btnListaDeudores)

        btnRegistrarPersona.setOnClickListener {
            val intent = Intent(this, RegistrarActivity::class.java)
            startActivity(intent)
        }

        btnCobrar.setOnClickListener {
            val intent = Intent(this, CobrarActivity::class.java)
            startActivity(intent)
        }

        btnEmitirCarnet.setOnClickListener {
            val intent = Intent(this, EmitirCarnetActivity::class.java)
            startActivity(intent)
        }

        btnListaDeudores.setOnClickListener {
            val intent = Intent(this, ListaDeudoresActivity::class.java)
            startActivity(intent)
        }
    }
}