package com.ipvc.manut_smart.admin.Departments

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
import com.ipvc.manut_smart.admin.Departments.EnableDesableDepartmet.EnableDisableDepartmentsAdminActivity

class SubMenuDepartmentAdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sub_menu_department_admin)
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

        val btnCreateDepartment = findViewById<Button>(R.id.btnCreateDepartment)
        btnCreateDepartment.setOnClickListener {
            val intent = Intent(this, CreateDepartmentAdminActivity::class.java)
            startActivity(intent)
        }

        val btnEnableDisableDepartment = findViewById<Button>(R.id.btnEnableDisableDepartment)
        btnEnableDisableDepartment.setOnClickListener {
            val intent = Intent(this, EnableDisableDepartmentsAdminActivity::class.java)
            startActivity(intent)
        }
    }
}