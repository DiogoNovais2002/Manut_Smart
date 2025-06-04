package com.ipvc.manut_smart.admin.Departments.EnableDesableDepartmet

import android.os.Bundle
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

class EnableDisableDepartmentsAdminActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var listView: ListView
    private val departmentList = mutableListOf<Department>()
    private lateinit var adapter: DepartmentListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_enable_disable_departments_admin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        db = FirebaseFirestore.getInstance()
        listView = findViewById(R.id.enableDisableDepartmentListView)

        adapter = DepartmentListAdapter(this, departmentList) { department ->
            toggleDepartment(department)
        }

        listView.adapter = adapter
        fetchDepartments()
    }

    private fun fetchDepartments() {
        db.collection("departments")
            .get()
            .addOnSuccessListener { result ->
                departmentList.clear()
                for (doc in result) {
                    val dept = Department(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        location = doc.getString("location") ?: "",
                        is_active = doc.getBoolean("is_active") ?: true
                    )
                    departmentList.add(dept)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching departments", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleDepartment(department: Department) {
        val newState = !department.is_active

        db.collection("departments").document(department.id)
            .update("is_active", newState)
            .addOnSuccessListener {
                department.is_active = newState
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "${department.name} ${if (newState) "true" else "false"}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error toggling department", Toast.LENGTH_SHORT).show()
            }
    }
}