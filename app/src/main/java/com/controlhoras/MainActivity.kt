package com.controlhoras

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity

class MainActivity : Activity() {

    private lateinit var imageView: ImageView
    private lateinit var resultado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
        }

        val titulo = TextView(this).apply {
            text = "Control Horas"
            textSize = 28f
            gravity = Gravity.CENTER
        }

        val instrucciones = TextView(this).apply {
            text = "Toma una foto de la tarjeta de horas para comenzar."
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 30, 0, 30)
        }

        val botonFoto = Button(this).apply {
            text = "📷 Tomar foto"
            setOnClickListener {
                abrirCamara()
            }
        }

        val botonGaleria = Button(this).apply {
            text = "🖼️ Seleccionar foto"
            setOnClickListener {
                abrirGaleria()
            }
        }

        imageView = ImageView(this).apply {
            adjustViewBounds = true
        }

        resultado = TextView(this).apply {
            text = ""
            textSize = 18f
            setPadding(0, 30, 0, 0)
        }

        layout.addView(titulo)
        layout.addView(instrucciones)
        layout.addView(botonFoto)
        layout.addView(botonGaleria)
        layout.addView(imageView)
        layout.addView(resultado)

        setContentView(layout)
    }

    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 100)
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 200)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 200) {
                val uri: Uri? = data.data
                imageView.setImageURI(uri)
                resultado.text = "Foto seleccionada. Próximamente leeremos las horas."
            } else if (requestCode == 100) {
                val foto = data.extras?.get("data")
                imageView.setImageBitmap(foto as android.graphics.Bitmap)
                resultado.text = "Foto tomada. Próximamente leeremos las horas."
            }
        }
    }
}
