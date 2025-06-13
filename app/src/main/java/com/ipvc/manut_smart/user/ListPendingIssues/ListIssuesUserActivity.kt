package com.ipvc.manut_smart.user.ListPendingIssues

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.util.Base64
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipvc.manut_smart.LoginActivity
import com.ipvc.manut_smart.R

class ListIssuesUserActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list_issues_user)

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
            return
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val container = findViewById<LinearLayout>(R.id.issuesContainer)
        container.removeAllViews()

        firestore.collection("issue")
            .whereEqualTo("userId", user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    val warning = findViewById<TextView>(R.id.warning)
                    warning.visibility = View.VISIBLE
                }
                for (doc in snapshot.documents) {
                    val state = doc.getString("state") ?: continue
                    if (state != "pending" && state != "in_progress") continue

                    val deviceId = doc.getString("deviceid") ?: continue

                    firestore.collection("devices")
                        .document(deviceId)
                        .get()
                        .addOnSuccessListener { deviceDoc ->
                            val serial = deviceDoc.getString("serialNumber") ?: "Sem Nº de Série"
                            val branch = deviceDoc.getString("branch") ?: "Sem Marca"
                            val model = deviceDoc.getString("model") ?: "Sem Modelo"
                            val fullName = "$branch - $model - $serial"

                            val description = doc.getString("description") ?: "Sem descrição"
                            val fault = doc.getString("title") ?: ""

                            val issueView = layoutInflater.inflate(R.layout.item_issue_card, container, false)

                            val nameText = issueView.findViewById<TextView>(R.id.deviceName)
                            val faultText = issueView.findViewById<TextView>(R.id.faultText)
                            val descText = issueView.findViewById<TextView>(R.id.descriptionText)
                            val stateText = issueView.findViewById<TextView>(R.id.stateText)
                            val statusDot = issueView.findViewById<View>(R.id.statusDot)
                            val btnExpand = issueView.findViewById<FrameLayout>(R.id.btnExpand)
                            val detailsLayout = issueView.findViewById<LinearLayout>(R.id.detailsLayout)
                            val expandText = btnExpand.findViewById<TextView>(R.id.expandText)

                            nameText.text = fullName
                            faultText.text = fault
                            descText.text = description
                            stateText.text = "Estado: " + when (state) {
                                "pending" -> getString(R.string.Pending)
                                "in_progress" -> getString(R.string.InProgress)
                                else -> state
                            }

                            val color = when (state) {
                                "pending" -> R.color.orange
                                "in_progress" -> R.color.blue
                                else -> R.color.gray
                            }
                            statusDot.backgroundTintList = ContextCompat.getColorStateList(this, color)

                            val photoView = issueView.findViewById<ImageView>(R.id.photoView)
                            val photoBase64 = doc.getString("photoBase64")
                            val textPhoto = issueView.findViewById<TextView>(R.id.titlePhoto)

                            if (!photoBase64.isNullOrEmpty()) {
                                val imageBytes = Base64.decode(photoBase64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                photoView.setImageBitmap(bitmap)
                                photoView.visibility = View.VISIBLE
                                textPhoto.visibility = View.VISIBLE
                            } else {
                                photoView.visibility = View.GONE
                                textPhoto.visibility = View.GONE
                            }

                            if (state == "in_progress") {
                                firestore.collection("intervention")
                                    .whereEqualTo("issue_id", doc.id)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener { interSnapshot ->
                                        val interDoc = interSnapshot.documents.firstOrNull()
                                        val startDate = interDoc?.getTimestamp("start_date")
                                        val startText = issueView.findViewById<TextView>(R.id.startDate)

                                        if (startDate != null) {
                                            val formatted = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", startDate.toDate())
                                            startText.text = "Iniciada em: $formatted"
                                            startText.visibility = View.VISIBLE
                                        } else {
                                            startText.visibility = View.GONE
                                        }
                                    }
                            }


                            btnExpand.setOnClickListener {
                                val isVisible = detailsLayout.visibility == View.VISIBLE
                                detailsLayout.visibility = if (isVisible) View.GONE else View.VISIBLE
                                expandText.text = if (isVisible) "+" else "-"
                            }

                            container.addView(issueView)
                        }
                }
            }
    }
}