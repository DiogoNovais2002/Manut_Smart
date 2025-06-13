package com.ipvc.manut_smart.admin.Technician

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.ipvc.manut_smart.R
import java.text.SimpleDateFormat
import java.util.Locale

class Admin_tec_Relatory : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_tec_relatory)
        loadTechniciansReport()
        val backButton = findViewById<ImageView>(R.id.returnIcon)
        backButton.setOnClickListener { finish() }
    }

    private fun loadTechniciansReport() {
        val listContainer = findViewById<LinearLayout>(R.id.deviceListView)
        db.collection("users")
            .whereEqualTo("role", "technician")
            .get()
            .addOnSuccessListener { documents ->
                listContainer.removeAllViews()
                for (doc in documents) {
                    val name = doc.getString("name") ?: ""
                    val uid = doc.id
                    val itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_technician_report, listContainer, false)

                    itemView.findViewById<TextView>(R.id.tvName).text = name

                    val detailsLayout = itemView.findViewById<LinearLayout>(R.id.detailsLayout)
                    detailsLayout.visibility = android.view.View.GONE

                    val btnExpand = itemView.findViewById<FrameLayout>(R.id.btnExpand)
                    val expandText = btnExpand.findViewById<TextView>(R.id.expandText)

                    btnExpand.setOnClickListener {
                        val isVisible = detailsLayout.visibility == android.view.View.GONE
                        detailsLayout.visibility = if (isVisible) android.view.View.VISIBLE else android.view.View.GONE
                        expandText.text = if (isVisible) "-" else "+"
                    }

                    db.collection("intervention")
                        .whereEqualTo("technical_uid", uid)
                        .get()
                        .addOnSuccessListener { interventions ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            val total = interventions.size()
                            var lastRepairDate = "-"
                            var lastRepairIssue = "-"
                            var avgRepairTime = "-"
                            var lastRepairDevice = "-"
                            var lastRepairLocation = "-"

                            var maxEndDate: Long = 0
                            var totalDuration: Long = 0
                            var validRepairs = 0

                            for (interv in interventions) {
                                val end = interv.getTimestamp("end_date")?.toDate()
                                val start = interv.getTimestamp("start_date")?.toDate()
                                if (end != null && start != null) {
                                    val duration = end.time - start.time
                                    totalDuration += duration
                                    validRepairs++
                                    if (end.time > maxEndDate) {
                                        maxEndDate = end.time
                                        lastRepairDate = sdf.format(end)

                                        val issueId = interv.getString("issue_id")
                                        if (!issueId.isNullOrEmpty()) {
                                            db.collection("issue").document(issueId).get()
                                                .addOnSuccessListener { issueDoc ->
                                                    val deviceId = issueDoc.getString("deviceid")
                                                    if (!deviceId.isNullOrEmpty()) {
                                                        db.collection("devices").document(deviceId).get()
                                                            .addOnSuccessListener { deviceDoc ->
                                                                val deviceName = deviceDoc.getString("model") ?: "-"
                                                                val departmentId = deviceDoc.getString("departmentId")
                                                                if (!departmentId.isNullOrEmpty()) {
                                                                    db.collection("departments")
                                                                        .document(departmentId).get()
                                                                        .addOnSuccessListener { depDoc ->
                                                                            val location = depDoc.getString("location") ?: "-"
                                                                            itemView.findViewById<TextView>(R.id.tvLastRepairIssue).text =
                                                                                getString(
                                                                                    R.string.Last_Intervention_loc,
                                                                                    deviceName,
                                                                                    location
                                                                                )
                                                                        }
                                                                } else {
                                                                    itemView.findViewById<TextView>(R.id.tvLastRepairIssue).text =
                                                                        getString(
                                                                            R.string.last_intervention_device,
                                                                            deviceName
                                                                        )
                                                                }
                                                            }
                                                    } else {
                                                        itemView.findViewById<TextView>(R.id.tvLastRepairIssue).text =
                                                            getString(R.string.last_intervention_error)
                                                    }
                                                }
                                        } else {
                                            itemView.findViewById<TextView>(R.id.tvLastRepairIssue).text =
                                                getString(R.string.last_intervention_error)
                                        }
                                    }
                                }
                            }
                            if (validRepairs > 0) {
                                val avgMillis = totalDuration / validRepairs
                                val minutes = (avgMillis / 1000 / 60) % 60
                                val hours = (avgMillis / 1000 / 60 / 60)
                                avgRepairTime = "${hours}h ${minutes}m"
                            }

                            itemView.findViewById<TextView>(R.id.tvTotalRepairs).text =
                                getString(R.string.Total_repair_num, total)
                            itemView.findViewById<TextView>(R.id.tvLastRepairDate).text = getString(
                                R.string.last_repair_date, lastRepairDate
                            )
                            itemView.findViewById<TextView>(R.id.tvLastRepairIssue).text =
                                getString(
                                    R.string.last_intervation_dev_name,
                                    lastRepairDevice,
                                    lastRepairLocation
                                )
                            itemView.findViewById<TextView>(R.id.tvAvgRepairTime).text =
                                getString(R.string.Avg_repair_time, avgRepairTime)
                        }

                    listContainer.addView(itemView)
                }
            }
    }
}
