package com.ipvc.manut_smart.user

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
import com.ipvc.manut_smart.user.HistoryList.IssuesHistoryUserActivity
import com.ipvc.manut_smart.user.ListPendingIssues.ListIssuesUserActivity
import com.ipvc.manut_smart.user.NewIssue.NewIssueUserActivity

class MenuUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_user)

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

        val btnNewIssue = findViewById<Button>(R.id.btnNewIssue)
        btnNewIssue.setOnClickListener {
            val intent = Intent(this, NewIssueUserActivity::class.java)
            startActivity(intent)
        }

        val btnOngoingIssues = findViewById<Button>(R.id.btnOngoingIssues)
        btnOngoingIssues.setOnClickListener {
            val intent = Intent(this, ListIssuesUserActivity::class.java)
            startActivity(intent)
        }

        val btnFaultHistory = findViewById<Button>(R.id.btnFaultHistory)
        btnFaultHistory.setOnClickListener {
            val intent = Intent(this, IssuesHistoryUserActivity::class.java)
            startActivity(intent)
        }
    }
}