package com.ulisesdiaz.integracionapifoursquare

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    var fsq: Foursquare? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fsq = Foursquare(this)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        if (fsq?.hayToken()!!){
            startActivity(Intent(this, SecondActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            // Se ejeuta el codigo de autenticacion
            fsq?.iniciarSesion()

        }
    }

    // Validar de acuerdo a la respues del codigo que se ingreso
    // Se mapaeara el codigo que se mando de conexion
    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        fsq?.validarActivityResult(requestCode, resultCode, data)
    }
}