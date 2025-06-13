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

class List_Tec_Admin : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_tec_admin)
        loadTechnicians()

        val backButton = findViewById<ImageView>(R.id.returnIcon)
        backButton.setOnClickListener { finish() }
    }

    private fun loadTechnicians() {
        val listContainer = findViewById<LinearLayout>(R.id.deviceListView)
        db.collection("users")
            .whereEqualTo("role", "technician")
            .get()
            .addOnSuccessListener { documents ->
                listContainer.removeAllViews()
                for (doc in documents) {
                    val name = doc.getString("name") ?: ""
                    val email = doc.getString("email") ?: ""
                    val phone = doc.getString("phone") ?: ""
                    val role = doc.getString("role") ?: ""
                    val isActive = doc.getBoolean("isActive") ?: false

                    val itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_technician_admin, listContainer, false)

                    itemView.findViewById<TextView>(R.id.tvName).text = name

                    val detailsLayout = itemView.findViewById<LinearLayout>(R.id.detailsLayout)
                    detailsLayout.visibility = android.view.View.GONE

                    itemView.findViewById<TextView>(R.id.tvEmail).text = "Email: $email"
                    itemView.findViewById<TextView>(R.id.tvPhone).text = "Telefone: $phone"
                    itemView.findViewById<TextView>(R.id.tvRole).text = "Função: $role"
                    itemView.findViewById<TextView>(R.id.tvActive).text = "Ativo: ${if (isActive) "Sim" else "Não"}"

                    val btnExpand = itemView.findViewById<FrameLayout>(R.id.btnExpand)
                    val expandText = btnExpand.findViewById<TextView>(R.id.expandText)
                    btnExpand.setOnClickListener {
                        val isVisible = detailsLayout.visibility == android.view.View.GONE
                        detailsLayout.visibility = if (isVisible) android.view.View.VISIBLE else android.view.View.GONE
                        expandText.text = if (isVisible) "-" else "+"
                    }

                    listContainer.addView(itemView)
                }
            }
    }
}
