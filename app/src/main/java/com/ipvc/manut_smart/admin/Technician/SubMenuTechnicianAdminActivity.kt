package com.ipvc.manut_smart.admin.Technician

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.ipvc.manut_smart.LoginActivity
import com.ipvc.manut_smart.R

class SubMenuTechnicianAdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sub_menu_technician_admin)
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

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterTechnicianAdminActivity::class.java)
            startActivity(intent)
        }

        val btnListTec = findViewById<Button>(R.id.btnListTec)
        btnListTec.setOnClickListener{
            val intent = Intent(this, List_Tec_Admin::class.java)
            startActivity(intent)
        }

        val btnAdmin_tec_Relatory = findViewById<Button>(R.id.btnAdmin_tec_Relatory)
        btnAdmin_tec_Relatory.setOnClickListener{
            val intent = Intent(this, Admin_tec_Relatory::class.java)
            startActivity(intent)
        }
        val btnEnableDisableTec = findViewById<Button>(R.id.btnEnableDisableTec)
        btnEnableDisableTec.setOnClickListener{
            val intent = Intent(this, Enable_disable_tec::class.java)
            startActivity(intent)
        }
    }
}