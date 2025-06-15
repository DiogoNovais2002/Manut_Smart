package com.ipvc.manut_smart.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.ipvc.manut_smart.R
import java.text.SimpleDateFormat
import java.util.Locale

class List_all_repair_admin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list_all_repair_admin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val backButton = findViewById<ImageView>(R.id.returnIcon)
        backButton.setOnClickListener { finish() }
        listarTodasReparacoes()
    }

    private fun listarTodasReparacoes() {
        val db = FirebaseFirestore.getInstance()
        val container = findViewById<LinearLayout>(R.id.listRepairsadmin)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        db.collection("issue").get().addOnSuccessListener { issues ->
            for (issueDoc in issues) {
                val title = issueDoc.getString("title") ?: "Sem título"
                val description = issueDoc.getString("description") ?: ""
                val deviceId = issueDoc.getString("deviceid") ?: ""
                val userId = issueDoc.getString("userId")

                if (deviceId.isBlank() || userId.isNullOrBlank()) continue

                val state = issueDoc.getString("state") ?: "Sem estado"
                val dateRegistration = issueDoc.getTimestamp("date_registration")?.toDate()?.let { sdf.format(it) } ?: "Sem data"

                db.collection("devices").document(deviceId).get()
                    .addOnSuccessListener { deviceDoc ->
                        val deviceName = deviceDoc.getString("model") ?: "Desconhecido"

                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { userDoc ->
                                val userName = userDoc.getString("name") ?: "Utilizador desconhecido"

                                val itemView = LayoutInflater.from(this)
                                    .inflate(R.layout.item_admin_repair, container, false)

                                itemView.findViewById<TextView>(R.id.tvTitle).text = title
                                itemView.findViewById<TextView>(R.id.tvDevice).text = "Dispositivo: $deviceName"
                                itemView.findViewById<TextView>(R.id.tvTechnician).text = "Utilizador: $userName"
                                itemView.findViewById<TextView>(R.id.tvDescription).text = "Descrição: $description\nEstado: $state\nData: $dateRegistration"
                                itemView.findViewById<TextView>(R.id.tvDepartment).text = ""

                                val detailsLayout = itemView.findViewById<LinearLayout>(R.id.detailsLayout)
                                itemView.findViewById<FrameLayout>(R.id.btnExpand).setOnClickListener {
                                    detailsLayout.visibility =
                                        if (detailsLayout.visibility == View.GONE) View.VISIBLE else View.GONE
                                }

                                container.addView(itemView)
                            }
                            .addOnFailureListener { it.printStackTrace() }
                    }
                    .addOnFailureListener { it.printStackTrace() }
            }
        }
    }
}
