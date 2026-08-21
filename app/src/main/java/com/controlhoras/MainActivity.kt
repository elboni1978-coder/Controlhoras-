package com.controlhoras

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
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
            text = "Selecciona una foto de la tarjeta."
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

    private fun procesarFoto(uri: Uri) {
        try {
            val original = MediaStore.Images.Media.getBitmap(
                contentResolver,
                uri
            )

            imageView.setImageBitmap(original)
            resultado.text = "🔍 Preparando la tarjeta..."

            // Giramos la imagen 90 grados para ayudar
            // al reconocimiento de las horas verticales.
            val matrix = Matrix()
            matrix.postRotate(90f)

            val girada = Bitmap.createBitmap(
                original,
                0,
                0,
                original.width,
                original.height,
                matrix,
                true
            )

            reconocerTexto(girada)

        } catch (e: Exception) {
            resultado.text =
                "No se pudo procesar la foto.\n${e.message}"
        }
    }

    private fun reconocerTexto(bitmap: Bitmap) {

        resultado.text = "🔍 Leyendo las horas..."

        val image = InputImage.fromBitmap(bitmap, 0)

        val recognizer = TextRecognition.getClient(
            TextRecognizerOptions.DEFAULT_OPTIONS
        )

        recognizer.process(image)
            .addOnSuccessListener { visionText ->

                val patronHora = Regex(
                    """\b(?:[01]?\d|2[0-3])[:.][0-5]\d\b"""
                )

                val horas = patronHora
                    .findAll(visionText.text)
                    .map { it.value }
                    .toList()

                if (horas.isEmpty()) {
                    resultado.text =
                        "No encontré horas todavía.\n\n" +
                        "OCR después de girar la tarjeta:\n\n" +
                        visionText.text
                } else {
                    resultado.text =
                        "Horas detectadas:\n\n" +
                        horas.joinToString("\n")
                }
            }
            .addOnFailureListener { error ->
                resultado.text =
                    "Error al leer la tarjeta:\n${error.message}"
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (resultCode != RESULT_OK || data == null) {
            return
        }

        if (requestCode == 200) {

            val uri = data.data ?: return

            procesarFoto(uri)

        } else if (requestCode == 100) {

            val bitmap =
                data.extras?.get("data") as? Bitmap

            if (bitmap != null) {

                imageView.setImageBitmap(bitmap)

                reconocerTexto(bitmap)
            }
        }
    }
}
