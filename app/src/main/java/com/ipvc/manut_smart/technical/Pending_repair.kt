package com.ipvc.manut_smart.technical

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipvc.manut_smart.R
import com.ipvc.manut_smart.technical.IssueData.Issue
import java.text.SimpleDateFormat
import java.util.Locale

class Pending_repair : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private var selectedFilter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_repair)

        val backButton = findViewById<ImageView>(R.id.returnIcon)

        val spinnerFilter = findViewById<Spinner>(R.id.spinnerFilter)
        val options = listOf("Filtrar", "Urgência", "Data")
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            options
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                val textView = v as TextView
                textView.text = options[position]
                if (position == 0) {
                    textView.setTextColor(Color.GRAY)
                } else {
                    textView.setTextColor(Color.GRAY)
                }
                return v
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getDropDownView(position, convertView, parent)
                val textView = v as TextView
                if (position == 0) textView.setTextColor(Color.GRAY)
                else textView.setTextColor(Color.BLACK)
                return v
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = adapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedFilter = position
                loadPendingIssues()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        backButton.setOnClickListener { finish() }

        loadPendingIssues()
    }

    private fun loadPendingIssues() {
        val listContainer = findViewById<LinearLayout>(R.id.listContainer)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        db.collection("issue")
            .whereEqualTo("state", "pending")
            .get()
            .addOnSuccessListener { documents ->
                listContainer.removeAllViews()
                val issues = documents.map { doc ->
                    doc.toObject(Issue::class.java)
                }.toMutableList()
                when (selectedFilter) {
                    1 -> {
                        issues.sortByDescending { it.urgency }
                    }
                    2 -> {
                        issues.sortBy { it.date_registration?.toDate() }
                    }
                    else -> {
                    }
                }

                for (issue in issues) {
                    val itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_pending_repair, listContainer, false)

                    itemView.findViewById<TextView>(R.id.tvTitle).text = issue.title
                    itemView.findViewById<TextView>(R.id.tvUrgency).text =
                        if (issue.urgency) "Alta" else "Baixa"


            val btnExpand = itemView.findViewById<FrameLayout>(R.id.btnExpand)
                    val detailsLayout = itemView.findViewById<LinearLayout>(R.id.detailsLayout)
                    detailsLayout.visibility = View.GONE

                    itemView.findViewById<TextView>(R.id.tvDescription).text = issue.description
                    val dateText = issue.date_registration?.toDate()?.let { sdf.format(it) } ?: ""
                    itemView.findViewById<TextView>(R.id.tvDate).text = dateText

                    btnExpand.setOnClickListener {
                        detailsLayout.visibility =
                            if (detailsLayout.visibility == View.GONE) View.VISIBLE else View.GONE
                    }

                    itemView.findViewById<Button>(R.id.btnReparar).setOnClickListener {
                        val issueId = issue.id
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val technicalUid = currentUser?.uid

                        if (technicalUid == null) {
                            Toast.makeText(this, "Técnico não autenticado!", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        db.collection("issue")
                            .document(issueId)
                            .update("state", "in progress")
                            .addOnSuccessListener {
                                val interventionData = hashMapOf(
                                    "start_date" to Timestamp.now(),
                                    "end_date" to null,
                                    "description" to "",
                                    "technical_uid" to technicalUid,
                                    "issue_id" to issueId
                                )
                                db.collection("intervention")
                                    .add(interventionData)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "Estado alterado e intervenção criada!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        loadPendingIssues()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            this,
                                            "Erro ao criar intervenção!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Erro ao atualizar estado", Toast.LENGTH_SHORT)
                                    .show()
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
