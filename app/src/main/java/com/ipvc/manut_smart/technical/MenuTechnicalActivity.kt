package com.ipvc.manut_smart.technical

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

class MenuTechnicalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_technical)

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

        val startRepairButton = findViewById<Button>(R.id.startRepair)
        val ongoingRepairButton = findViewById<Button>(R.id.ongoingRepair)
        val finishRepairButton = findViewById<Button>(R.id.finishRepair)
        val repairHistoryButton = findViewById<Button>(R.id.repairHistory)

        startRepairButton.setOnClickListener {
            startActivity(Intent(this, Pending_repair::class.java))
        }

        ongoingRepairButton.setOnClickListener {
            startActivity(Intent(this, Ongoing_Repair_tec::class.java))
        }

        finishRepairButton.setOnClickListener {
            startActivity(Intent(this, Finish_repair_technical::class.java))
        }

        repairHistoryButton.setOnClickListener {
            startActivity(Intent(this, RepairHistory_technical::class.java))
        }

    }
}