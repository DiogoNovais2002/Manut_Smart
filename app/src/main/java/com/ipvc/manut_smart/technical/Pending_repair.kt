package com.ipvc.manut_smart.technical

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ipvc.manut_smart.R

class Pending_repair : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_repair)

        val btnFiltrar = findViewById<Button>(R.id.btnFiltrar)
        val filterOptions = findViewById<LinearLayout>(R.id.filterOptions)
        val btnExpand = findViewById<View>(R.id.btnExpand)
        val detailsLayout = findViewById<LinearLayout>(R.id.detailsLayout)

        btnFiltrar.setOnClickListener {
            filterOptions.visibility =
                if (filterOptions.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        btnExpand.setOnClickListener {
            detailsLayout.visibility =
                if (detailsLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        findViewById<Button>(R.id.btnReparar).setOnClickListener {
            Toast.makeText(this, "Reparação iniciada", Toast.LENGTH_SHORT).show()
        }
    }
}