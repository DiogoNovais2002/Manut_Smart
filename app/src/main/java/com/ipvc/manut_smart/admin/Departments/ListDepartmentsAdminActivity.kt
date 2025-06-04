package com.ipvc.manut_smart.admin.Departments

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.ipvc.manut_smart.R
import com.ipvc.manut_smart.admin.Departments.DepartementData.Department

class ListDepartmentsAdminActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<Department>
    private val departmentList = ArrayList<Department>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list_departments_admin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        listView = findViewById(R.id.departmentListView)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, departmentList)
        listView.adapter = adapter

        db = FirebaseFirestore.getInstance()
        fetchDepartments()
    }

    private fun fetchDepartments() {
        db.collection("departments")
            .get()
            .addOnSuccessListener { result ->
                departmentList.clear()
                for (document in result) {
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val location = document.getString("location") ?: ""
                    val isActive = document.getBoolean("is_active") ?: true
                    if(document.getBoolean("is_active") == true)
                        departmentList.add(Department(id,name,location,isActive))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching departments: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}