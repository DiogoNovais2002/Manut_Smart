package com.ipvc.manut_smart.admin.Departments

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ipvc.manut_smart.LoginActivity
import com.ipvc.manut_smart.R

class CreateDepartmentAdminActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_department_admin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        db = FirebaseFirestore.getInstance()

        val nameEditText = findViewById<EditText>(R.id.editName)
        val locationEditText = findViewById<EditText>(R.id.editLocation)
        val btnConfirm = findViewById<Button>(R.id.btnConfirm)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        btnConfirm.setOnClickListener {
            val nome = nameEditText.text.toString().trim()
            val localizacao = locationEditText.text.toString().trim()

            if (nome.isEmpty() || localizacao.isEmpty()) {
                Toast.makeText(this, getString(R.string.FieldAllFields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val department = hashMapOf(
                "name" to nome,
                "location" to localizacao,
                "is_active" to true,
                "created" to FieldValue.serverTimestamp()
            )

            db.collection("departments")
                .add(department)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.CreatedDepartmentSuccessfully), Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }
}