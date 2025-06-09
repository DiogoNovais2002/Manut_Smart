package com.ipvc.manut_smart.technical

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.ipvc.manut_smart.R
import com.ipvc.manut_smart.technical.IssueData.Issue
import java.text.SimpleDateFormat
import java.util.Locale

class Pending_repair : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_repair)

        val btnFiltrar = findViewById<Button>(R.id.btnFiltrar)
        val filterOptions = findViewById<LinearLayout>(R.id.filterOptions)
        val backButton = findViewById<ImageView>(R.id.returnIcon)

        btnFiltrar.setOnClickListener {
            filterOptions.visibility =
                if (filterOptions.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        backButton.setOnClickListener { finish() }

        // Chama a função para carregar a lista ao iniciar
        loadPendingIssues()
    }

    // FUNÇÃO para carregar e atualizar a lista
    private fun loadPendingIssues() {
        val listContainer = findViewById<LinearLayout>(R.id.listContainer)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        db.collection("issue")
            .whereEqualTo("state", "pending")
            .get()
            .addOnSuccessListener { documents ->
                listContainer.removeAllViews()
                for (document in documents) {
                    val issue = document.toObject(Issue::class.java)
                    val itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_pending_repair, listContainer, false)

                    itemView.findViewById<TextView>(R.id.tvTitle).text = issue.title
                    itemView.findViewById<TextView>(R.id.tvUrgency).text =
                        if (issue.urgency) "Alta" else "Normal"
                    itemView.findViewById<TextView>(R.id.tvDescription).text = issue.description

                    val dateText = issue.date_registration?.toDate()?.let { sdf.format(it) } ?: ""
                    itemView.findViewById<TextView>(R.id.tvDate).text = dateText

                    val btnExpand = itemView.findViewById<FrameLayout>(R.id.btnExpand)
                    val detailsLayout = itemView.findViewById<LinearLayout>(R.id.detailsLayout)
                    btnExpand.setOnClickListener {
                        detailsLayout.visibility = if (detailsLayout.visibility == View.GONE) View.VISIBLE else View.GONE
                    }

                    itemView.findViewById<Button>(R.id.btnReparar).setOnClickListener {
                        val issueId = document.id
                        db.collection("issue")
                            .document(issueId)
                            .update("state", "in progress")
                            .addOnSuccessListener {
                                Toast.makeText(this, "Estado alterado para 'in progress' para ${issue.title}", Toast.LENGTH_SHORT).show()
                                loadPendingIssues()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Erro ao atualizar estado", Toast.LENGTH_SHORT).show()
                            }
                    }
                    listContainer.addView(itemView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar reparações", Toast.LENGTH_SHORT).show()
            }
    }
}
