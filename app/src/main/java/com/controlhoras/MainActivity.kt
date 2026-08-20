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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : Activity() {

    private lateinit var imageView: ImageView
    private lateinit var resultado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val titulo = TextView(this).apply {
            text = "Control Horas"
            textSize = 28f
        }

        val botonFoto = Button(this).apply {
            text = "📷 Tomar foto"
            setOnClickListener {
                abrirCamara()
            }
        }

        val botonGaleria = Button(this).apply {
            text = "🖼️ Buscar foto"
            setOnClickListener {
                abrirGaleria()
            }
        }

        imageView = ImageView(this).apply {
            adjustViewBounds = true
        }

        resultado = TextView(this).apply {
            text = "Selecciona o toma una foto de la tarjeta."
            textSize = 18f
            setPadding(0, 20, 0, 20)
        }

        layout.addView(titulo)
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
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        startActivityForResult(intent, 200)
    }

    private fun reconocerTexto(uri: Uri) {
        resultado.text = "🔍 Leyendo la tarjeta..."

        try {
            val image = InputImage.fromFilePath(this, uri)

            val recognizer = TextRecognition.getClient(
                TextRecognizerOptions.DEFAULT_OPTIONS
            )

            recognizer.process(image)
                .addOnSuccessListener { visionText ->

                    val patronHora = Regex(
                        """\b(?:[01]?\d|2[0-3])[:.][0-5]\d(?:\s?[AaPp][Mm])?\b"""
                    )

                    val horas = patronHora
                        .findAll(visionText.text)
                        .map { it.value }
                        .toList()

                    resultado.text = if (horas.isEmpty()) {
                        "No encontré horas.\n\nTexto reconocido:\n${visionText.text}"
                    } else {
                        "Horas encontradas:\n\n${horas.joinToString("\n")}"
                    }
                }
                .addOnFailureListener { error ->
                    resultado.text =
                        "No se pudo leer la tarjeta.\n${error.message}"
                }

        } catch (e: Exception) {
            resultado.text =
                "No se pudo abrir la imagen.\n${e.message}"
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK || data == null) return

        if (requestCode == 200) {
            val uri = data.data ?: return

            imageView.setImageURI(uri)
            reconocerTexto(uri)
        }
    }
}
