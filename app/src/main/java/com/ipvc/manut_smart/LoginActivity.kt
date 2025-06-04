package com.ipvc.manut_smart

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipvc.manut_smart.admin.MenuAdminActivity
import com.ipvc.manut_smart.technical.MenuTechnicalActivity
import com.ipvc.manut_smart.user.MenuUserActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role")
                        navigateBasedOnRole(role)
                    } else {
                        Toast.makeText(this, "Erro: utilizador não encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            return
        }

        setContentView(R.layout.activity_login)

        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val registerText = findViewById<TextView>(R.id.textRegister)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                            db.collection("users").document(uid).get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val role = document.getString("role")
                                        navigateBasedOnRole(role)
                                    } else {
                                        Toast.makeText(this, "Erro ao carregar dados do utilizador", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Erro no login: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        registerText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun navigateBasedOnRole(role: String?) {
        when (role) {
            "admin" -> startActivity(Intent(this, MenuAdminActivity::class.java))
            "technician" -> startActivity(Intent(this, MenuTechnicalActivity::class.java))
            else -> startActivity(Intent(this, MenuUserActivity::class.java))
        }
        finish()
    }
}
