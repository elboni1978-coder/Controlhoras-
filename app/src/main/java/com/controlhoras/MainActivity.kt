package com.controlhoras

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val texto = TextView(this).apply {
            text = "Control Horas"
            textSize = 28f
            setPadding(40, 80, 40, 40)
        }

        setContentView(texto)
    }
}
