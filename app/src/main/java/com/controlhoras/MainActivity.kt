package com.controlhoras

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
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
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 200)
    }

    private fun reconocerTexto(bitmap: Bitmap) {
        resultado.text = "🔍 Leyendo la tarjeta..."

        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(
            TextRecognizerOptions.DEFAULT_OPTIONS
        )

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val texto = visionText.text

                if (texto.isBlank()) {
                    resultado.text = "No pude reconocer texto en la foto."
                } else {
                    resultado.text =
                        "Texto reconocido:\n\n$texto"
                }
            }
            .addOnFailureListener { error ->
                resultado.text =
                    "No se pudo leer la tarjeta: ${error.message}"
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

            val bitmap = MediaStore.Images.Media.getBitmap(
                contentResolver,
                uri
            )

            imageView.setImageBitmap(bitmap)
            reconocerTexto(bitmap)

        } else if (requestCode == 100) {
            val bitmap = data.extras?.get("data") as? Bitmap

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
                reconocerTexto(bitmap)
            }
        }
    }
}
