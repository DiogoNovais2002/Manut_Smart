package com.ipvc.manut_smart.admin

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
import com.ipvc.manut_smart.admin.Departments.SubMenuDepartmentAdminActivity
import com.ipvc.manut_smart.admin.Devices.SubMenuDeviceAdminActivity

class MenuAdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_admin)
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

        val logoutButton = findViewById<ImageView>(R.id.logout)

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            finish()
        }

        val repairButton = findViewById<Button>(R.id.repair)
        repairButton.setOnClickListener {
        }

        val departmentButton = findViewById<Button>(R.id.department)
        departmentButton.setOnClickListener {
            val intent = Intent(this, SubMenuDepartmentAdminActivity::class.java)
            startActivity(intent)
        }

        val devicesButton = findViewById<Button>(R.id.devices)
        devicesButton.setOnClickListener {
            val intent = Intent(this, SubMenuDeviceAdminActivity::class.java)
            startActivity(intent)
        }

        val techniciansButton = findViewById<Button>(R.id.technicians)
        techniciansButton.setOnClickListener {
            val intent = Intent(this, SubMenuTechnicalAdminActivity::class.java)
            startActivity(intent)
        }
    }
}
