package com.ipvc.manut_smart.admin.Devices

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipvc.manut_smart.LoginActivity
import com.ipvc.manut_smart.R
import com.ipvc.manut_smart.admin.Departments.DepartementData.Department

class RegisterDeviceAdminActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var departmentSpinner: Spinner
    private lateinit var deviceTypeSpinner: Spinner
    private val departments = mutableListOf<Department>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_device_admin)
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

        db = FirebaseFirestore.getInstance()
        departmentSpinner = findViewById(R.id.spinnerDepartment)
        deviceTypeSpinner = findViewById(R.id.spinnerTypeDevice)

        val editName = findViewById<EditText>(R.id.editName)
        val editSerial = findViewById<EditText>(R.id.editSN)
        val editDescription = findViewById<EditText>(R.id.editDescription)

        val btnConfirm = findViewById<Button>(R.id.btnConfirm)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        btnCancel.setOnClickListener { finish() }

        val fixedDeviceTypes = listOf(this.resources.getString(R.string.Fixed), this.resources.getString(R.string.projector), this.resources.getString(R.string.InteractiveWhiteboard))

        deviceTypeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            fixedDeviceTypes
        )
        deviceTypeSpinner.isEnabled = true

        db.collection("departments").get()
            .addOnSuccessListener { result ->
                departments.clear()
                for (doc in result) {
                    val id = doc.id
                    val name = doc.getString("name") ?: "Unnamed"
                    val location = doc.getString("location") ?: "Unnamed"
                    val isActive = doc.getBoolean("is_active") ?: true
                    if(doc.getBoolean("is_active") == true)
                        departments.add(Department(id,name,location,isActive))
                }

                departmentSpinner.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    departments.map { it.name + " - " + it.location }
                )
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.ErrorLoadingData), Toast.LENGTH_SHORT).show()
            }

        btnConfirm.setOnClickListener {
            val branch = editName.text.toString().trim()
            val serial = editSerial.text.toString().trim()
            val model = editDescription.text.toString().trim()

            if (branch.isEmpty() || serial.isEmpty() || model.isEmpty()) {
                Toast.makeText(this, getString(R.string.FieldAllFields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedDepartmentId = departments[departmentSpinner.selectedItemPosition].id
            val selectedDeviceType = deviceTypeSpinner.selectedItem.toString()

            val deviceData = hashMapOf(
                "branch" to branch,
                "serialNumber" to serial,
                "model" to model,
                "departmentId" to selectedDepartmentId,
                "deviceType" to selectedDeviceType,
                "is_active" to true
            )

            db.collection("devices").add(deviceData)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.RegisteredDeviceSuccessfully), Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.ErrorRegistering), Toast.LENGTH_SHORT).show()
                }
        }
    }
}