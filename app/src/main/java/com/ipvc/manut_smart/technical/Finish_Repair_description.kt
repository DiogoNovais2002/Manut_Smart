package com.ipvc.manut_smart.technical

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.ipvc.manut_smart.R

class Finish_Repair_description : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_finish_repair_description)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val backButton = findViewById<ImageView>(R.id.returnIcon)
        backButton.setOnClickListener { finish() }
        val CancelButton = findViewById<Button>(R.id.btnCancelar)
        CancelButton.setOnClickListener { finish() }

        val editDescricao = findViewById<EditText>(R.id.editDescricao)
        val ConfirmButton = findViewById<Button>(R.id.btnConfirmar)

        val issueId = intent.getStringExtra("ISSUE_ID") ?: ""

        ConfirmButton.setOnClickListener {
            val novaDescricao = editDescricao.text.toString().trim()
            if (issueId.isNotEmpty() && novaDescricao.isNotEmpty()) {

                db.collection("issue")
                    .document(issueId)
                    .update("state", "finished")
                    .addOnSuccessListener {

                        db.collection("intervention")
                            .whereEqualTo("issue_id", issueId)
                            .get()
                            .addOnSuccessListener { interventions ->
                                if (interventions.isEmpty) {
                                    Toast.makeText(this,
                                        getString(R.string.Intervencion_not_find), Toast.LENGTH_SHORT).show()
                                } else {
                                    for (intervention in interventions) {
                                        intervention.reference.update(
                                            mapOf(
                                                "end_date" to com.google.firebase.Timestamp.now(),
                                                "description" to novaDescricao
                                            )
                                        )
                                    }
                                    Toast.makeText(this,
                                        getString(R.string.Repair_done), Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this,
                                    getString(R.string.update_intervention_error), Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this,
                            getString(R.string.Finish_Repair_Error), Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, getString(R.string.Write_description), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
