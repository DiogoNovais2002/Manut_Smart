package com.ipvc.manut_smart.admin.Technician

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipvc.manut_smart.LoginActivity
import com.ipvc.manut_smart.R

class RegisterTechnicianAdminActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPhone: EditText
    private lateinit var editPassword: EditText
    private lateinit var editConfirmPassword: EditText
    private lateinit var btnConfirm: Button
    private lateinit var btnCancel: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_technician_admin)

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // UI References
        editName = findViewById(R.id.editNome)
        editEmail = findViewById(R.id.editTextEmail)
        editPhone = findViewById(R.id.editTextMobilePhone)
        editPassword = findViewById(R.id.editTextPassword)
        editConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        btnConfirm = findViewById(R.id.btnConfirm)
        btnCancel = findViewById(R.id.buttonCancel)

        btnConfirm.setOnClickListener {
            registerTechnician()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun registerTechnician() {
        val name = editName.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val phone = editPhone.text.toString().trim()
        val password = editPassword.text.toString()
        val confirmPassword = editConfirmPassword.text.toString()

        // Use string resource: FieldAllFields
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, getString(R.string.FieldAllFields), Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val hasUppercase = password.any { it.isUpperCase() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }

        if (!hasUppercase || !hasSymbol) {
            Toast.makeText(
                this,
                "Password must contain at least one uppercase letter and one symbol.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = it.user?.uid ?: return@addOnSuccessListener
                val technician = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "role" to "technician",
                    "uid" to uid,
                    "isActive" to true
                )

                firestore.collection("users").document(uid).set(technician)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Technician registered successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, getString(R.string.ErrorUpdateData), Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to create user: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
