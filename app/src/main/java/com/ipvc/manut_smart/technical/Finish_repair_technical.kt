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
import com.google.firebase.firestore.FirebaseFirestore
import com.ipvc.manut_smart.R
import com.ipvc.manut_smart.technical.IssueData.Issue
import com.ipvc.manut_smart.technical.IssueData.urgencyLevel
import java.text.SimpleDateFormat
import java.util.Locale

class Finish_repair_technical : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_finish_repair_technical)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val backButton = findViewById<ImageView>(R.id.returnIcon)
        backButton.setOnClickListener { finish() }

        loadInProgressIssues()
    }
    override fun onResume() {
        super.onResume()
        loadInProgressIssues()
    }

    private fun loadInProgressIssues() {
        val listContainer = findViewById<LinearLayout>(R.id.listContainer)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        db.collection("issue")
            .whereEqualTo("state", "in progress") // Só os em progresso!
            .get()
            .addOnSuccessListener { documents ->
                listContainer.removeAllViews()
                for (document in documents) {
                    val issue = document.toObject(Issue::class.java)
                    val itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_finish_repair, listContainer, false)

                    itemView.findViewById<TextView>(R.id.tvTitle).text = issue.title
                    itemView.findViewById<TextView>(R.id.tvUrgency).text =
                        when (issue.urgencyLevel()) {
                            2 -> "Alta"
                            1 -> "Média"
                            else -> "Baixa"
                        }
                    itemView.findViewById<TextView>(R.id.tvDescription).text = issue.description

                    val dateText = issue.date_registration?.toDate()?.let { sdf.format(it) } ?: ""
                    itemView.findViewById<TextView>(R.id.tvDate).text = dateText

                    val btnExpand = itemView.findViewById<FrameLayout>(R.id.btnExpand)
                    val detailsLayout = itemView.findViewById<LinearLayout>(R.id.detailsLayout)
                    btnExpand.setOnClickListener {
                        detailsLayout.visibility = if (detailsLayout.visibility == View.GONE) View.VISIBLE else View.GONE
                    }

                    itemView.findViewById<Button>(R.id.btnFinish).setOnClickListener {
                        val intent = Intent(this, Finish_Repair_description::class.java)
                        intent.putExtra("ISSUE_ID", document.id)
                        startActivity(intent)
                    }

                    listContainer.addView(itemView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar reparações em progresso", Toast.LENGTH_SHORT).show()
            }
    }
}
