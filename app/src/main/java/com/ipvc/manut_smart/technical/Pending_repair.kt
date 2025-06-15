package com.ipvc.manut_smart.technical

import android.content.Intent
import android.graphics.BitmapFactory
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
import com.ipvc.manut_smart.LoginActivity
import com.ipvc.manut_smart.R
import com.ipvc.manut_smart.technical.IssueData.Issue
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Base64


class Pending_repair : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private var selectedFilter = 0
    private var isActive: Boolean = false  // Guardar o estado do técnico

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_repair)

        val backButton = findViewById<ImageView>(R.id.returnIcon)
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Buscar estado de ativo do técnico ANTES de mostrar a lista
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                isActive = doc.getBoolean("isActive") ?: false
                setupFilterAndList()
            }
            .addOnFailureListener {
                isActive = false
                setupFilterAndList()
            }

        backButton.setOnClickListener { finish() }
    }

    private fun setupFilterAndList() {
        val spinnerFilter = findViewById<Spinner>(R.id.spinnerFilter)
        val options = listOf(
            getString(R.string.Filter_Filter), getString(R.string.Urgency_Filter),
            getString(R.string.Date_Filter)
        )
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
                val issuesList = documents.map { doc ->
                    Pair(doc, doc.toObject(Issue::class.java))
                }.toMutableList()

                when (selectedFilter) {
                    1 -> {
                        issuesList.sortByDescending { it.second.urgency }
                    }
                    2 -> {
                        issuesList.sortBy { it.second.date_registration?.toDate() }
                    }
                    else -> {  }
                }

                for ((doc, issue) in issuesList) {
                    val issueId = doc.id
                    val itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_pending_repair, listContainer, false)

                    itemView.findViewById<TextView>(R.id.tvTitle).text = issue.title
                    itemView.findViewById<TextView>(R.id.tvUrgency).text =
                        if (issue.urgency) getString(R.string.high) else getString(R.string.low)

                    val btnExpand = itemView.findViewById<FrameLayout>(R.id.btnExpand)
                    val detailsLayout = itemView.findViewById<LinearLayout>(R.id.detailsLayout)
                    detailsLayout.visibility = View.GONE

                    itemView.findViewById<TextView>(R.id.tvDescription).text = issue.description
                    val dateText = issue.date_registration?.toDate()?.let { sdf.format(it) } ?: ""
                    itemView.findViewById<TextView>(R.id.tvDate).text = dateText
                    detailsLayout.visibility = View.GONE

                    val photoView = itemView.findViewById<ImageView>(R.id.photoView)
                    val textPhoto = itemView.findViewById<TextView>(R.id.titlePhoto)
                    val photoBase64 = doc.getString("photoBase64")

                    if (!photoBase64.isNullOrEmpty()) {
                        try {
                            val imageBytes = Base64.decode(photoBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            photoView.setImageBitmap(bitmap)
                            photoView.visibility = View.VISIBLE
                            textPhoto.visibility = View.VISIBLE
                        } catch (e: Exception) {
                            photoView.visibility = View.GONE
                            textPhoto.visibility = View.GONE
                        }
                    } else {
                        photoView.visibility = View.GONE
                        textPhoto.visibility = View.GONE
                    }

                    val expandText = btnExpand.findViewById<TextView>(R.id.expandText)
                    btnExpand.setOnClickListener {
                        val isVisible = detailsLayout.visibility == View.GONE
                        detailsLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
                        expandText.text = if (isVisible) "-" else "+"
                    }

                    itemView.findViewById<Button>(R.id.btnReparar).setOnClickListener {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val technicalUid = currentUser?.uid

                        if (technicalUid == null) {
                            Toast.makeText(this, getString(R.string.Auth_tec), Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (!isActive) {
                            Toast.makeText(this, "Utilizador desativado, não pode iniciar reparação!", Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        }


                        db.collection("issue")
                            .document(issueId)
                            .update("state", "in_progress")
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
                                            getString(R.string.State_changed),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        loadPendingIssues()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            this,
                                            getString(R.string.Error_Intervention),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    getString(R.string.State_change_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }

                    listContainer.addView(itemView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.Get_repair_error), Toast.LENGTH_SHORT).show()
            }
    }
}
