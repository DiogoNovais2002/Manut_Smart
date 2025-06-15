package com.ipvc.manut_smart.technical

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
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
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.ipvc.manut_smart.LoginActivity
import com.ipvc.manut_smart.R
import com.ipvc.manut_smart.technical.IssueData.Issue
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
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        val backButton = findViewById<ImageView>(R.id.returnIcon)
        backButton.setOnClickListener { finish() }

        loadInProgressIssues()
    }


    private fun loadInProgressIssues() {
        val listContainer = findViewById<LinearLayout>(R.id.listContainer)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val user = FirebaseAuth.getInstance().currentUser
        val technicalUid = user?.uid ?: return

        listContainer.removeAllViews()

        db.collection("intervention")
            .whereEqualTo("technical_uid", technicalUid)
            .get()
            .addOnSuccessListener { interventions ->
                val issueIds = interventions
                    .mapNotNull { it.getString("issue_id") }
                    .toSet()

                if (issueIds.isEmpty()) {
                    showNoIssuesMessage(listContainer)
                    return@addOnSuccessListener
                }

                db.collection("issue")
                    .whereIn(FieldPath.documentId(), issueIds.toList())
                    .whereEqualTo("state", "in_progress")
                    .get()
                    .addOnSuccessListener { documents ->
                        var count = 0
                        val added = mutableSetOf<String>()

                        for (document in documents) {
                            val issueId = document.id
                            if (added.contains(issueId)) continue
                            added.add(issueId)

                            val issue = document.toObject(Issue::class.java)

                            val itemView = LayoutInflater.from(this)
                                .inflate(R.layout.item_finish_repair, listContainer, false)

                            itemView.findViewById<TextView>(R.id.tvTitle).text = issue.title
                            itemView.findViewById<TextView>(R.id.tvUrgency).text =
                                if (issue.urgency) "Alta" else "Baixa"
                            itemView.findViewById<TextView>(R.id.tvDescription).text = issue.description

                            val dateText = issue.date_registration?.toDate()?.let { sdf.format(it) } ?: ""
                            itemView.findViewById<TextView>(R.id.tvDate).text = dateText


                            val photoView = itemView.findViewById<ImageView>(R.id.photoView)
                            val textPhoto = itemView.findViewById<TextView>(R.id.titlePhoto)
                            val photoBase64 = document.getString("photoBase64")

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

                            val btnExpand = itemView.findViewById<FrameLayout>(R.id.btnExpand)
                            val detailsLayout = itemView.findViewById<LinearLayout>(R.id.detailsLayout)
                            btnExpand.setOnClickListener {
                                detailsLayout.visibility =
                                    if (detailsLayout.visibility == View.GONE) View.VISIBLE else View.GONE
                            }

                            itemView.findViewById<Button>(R.id.btnFinish).setOnClickListener {
                                val intent = Intent(this, Finish_Repair_description::class.java)
                                intent.putExtra("ISSUE_ID", issueId)
                                startActivity(intent)
                            }

                            listContainer.addView(itemView)
                            count++
                        }

                        if (count == 0) {
                            showNoIssuesMessage(listContainer)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, getString(R.string.Load_repair_Error), Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.Load_history), Toast.LENGTH_SHORT).show()
            }
    }


    private fun showNoIssuesMessage(container: LinearLayout) {
        val noIssuesView = LayoutInflater.from(this)
            .inflate(R.layout.item_no_issues, container, false)
        container.addView(noIssuesView)
    }
}
