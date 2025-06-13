package com.ipvc.manut_smart.admin.Technician

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.ipvc.manut_smart.R

class Enable_disable_tec : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enable_disable_tec)

        val listTecContainer = findViewById<LinearLayout>(R.id.listTecContainer)
        val backButton = findViewById<ImageView>(R.id.returnIcon)
        backButton.setOnClickListener { finish() }

        db.collection("users")
            .whereEqualTo("role", "technician")
            .get()
            .addOnSuccessListener { documents ->
                listTecContainer.removeAllViews()
                for (doc in documents) {
                    val techId = doc.id
                    val name = doc.getString("name") ?: "-"
                    val isActive = doc.getBoolean("isActive") ?: false

                    val itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_enable_disable_tec, listTecContainer, false)

                    val tvTecName = itemView.findViewById<TextView>(R.id.tvTecName)
                    val btnEnableDisable = itemView.findViewById<Button>(R.id.btnEnableDisable)

                    tvTecName.text = name
                    btnEnableDisable.text = if (isActive) "Desativar" else "Ativar"

                    btnEnableDisable.setOnClickListener {
                        if (isActive) {
                            db.collection("intervention")
                                .whereEqualTo("technical_uid", techId)
                                .whereEqualTo("state", "in_progress")
                                .get()
                                .addOnSuccessListener { interventions ->
                                    if (!interventions.isEmpty) {
                                        Toast.makeText(
                                            this,
                                            "O técnico não pode ser desativado enquanto tem intervenções em curso.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        updateActiveState(techId, false, btnEnableDisable)
                                    }
                                }
                        } else {
                            updateActiveState(techId, true, btnEnableDisable)
                        }
                    }

                    listTecContainer.addView(itemView)
                }
            }
    }

    private fun updateActiveState(
        techId: String,
        newState: Boolean,
        btnEnableDisable: Button
    ) {
        db.collection("users").document(techId)
            .update("isActive", newState)
            .addOnSuccessListener {
                btnEnableDisable.text = if (newState) "Desativar" else "Ativar"
                Toast.makeText(
                    this,
                    if (newState) "Técnico ativado!" else "Técnico desativado!",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
