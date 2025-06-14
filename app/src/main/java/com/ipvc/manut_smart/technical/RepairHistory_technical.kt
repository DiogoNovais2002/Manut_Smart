package com.ipvc.manut_smart.technical

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipvc.manut_smart.LoginActivity
import com.ipvc.manut_smart.R
import com.ipvc.manut_smart.technical.IssueData.Issue
import java.text.SimpleDateFormat
import java.util.Locale

class RepairHistory_technical : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_repair_history_technical)
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
        val backButton = findViewById<ImageView>(R.id.returnIcon)
        backButton.setOnClickListener { finish() }

        loadAllIssues()
    }

    private fun loadAllIssues() {
        val listContainer = findViewById<LinearLayout>(R.id.listContainer)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        db.collection("issue")
            .get()
            .addOnSuccessListener { documents ->
                listContainer.removeAllViews()
                for (document in documents) {
                    val issue = document.toObject(Issue::class.java)
                    val itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_pending_repair, listContainer, false)

                    itemView.findViewById<TextView>(R.id.tvTitle).text = issue.title
                    itemView.findViewById<TextView>(R.id.tvUrgency).text =
                        if (issue.urgency) "Alta" else "Baixa"
                    itemView.findViewById<TextView>(R.id.tvDescription).text = issue.description

                    val dateText = issue.date_registration?.toDate()?.let { sdf.format(it) } ?: ""
                    itemView.findViewById<TextView>(R.id.tvDate).text = dateText

                    itemView.findViewById<Button>(R.id.btnReparar).visibility = View.GONE

                    val btnExpand = itemView.findViewById<FrameLayout>(R.id.btnExpand)
                    val detailsLayout = itemView.findViewById<LinearLayout>(R.id.detailsLayout)
                    val tvState = itemView.findViewById<TextView>(R.id.tvState)

                    tvState.text = ""

                    btnExpand.setOnClickListener {
                        if (detailsLayout.visibility == View.GONE) {
                            val estadoVisivel = when (issue.state) {
                                "pending" -> getString(R.string.Pending)
                                "in progress" -> getString(R.string.In_progress)
                                "finished" -> getString(R.string.Finish_state)
                                else -> issue.state
                            }
                            tvState.text = "Estado: $estadoVisivel"
                            detailsLayout.visibility = View.VISIBLE
                        } else {
                            detailsLayout.visibility = View.GONE

                        }
                    }

                    listContainer.addView(itemView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.Load_history), Toast.LENGTH_SHORT).show()
            }
    }
}

