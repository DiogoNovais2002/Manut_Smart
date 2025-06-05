package com.ipvc.manut_smart.admin.Devices.EnableDisableDevices

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipvc.manut_smart.LoginActivity
import com.ipvc.manut_smart.R
import com.ipvc.manut_smart.admin.Departments.DepartementData.Department
import com.ipvc.manut_smart.admin.Devices.DeviceData.Device
import com.ipvc.manut_smart.admin.Devices.DeviceData.DeviceListAdapter

class EnableDisableDevicesAdminActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var departmentSpinner: Spinner
    private lateinit var deviceListView: ListView
    private lateinit var emptyTextView: TextView
    private val departments = mutableListOf<Department>()
    private val devices = mutableListOf<Device>()
    private lateinit var deviceAdapter: DeviceListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enable_disable_devices_admin)
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

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        emptyTextView = findViewById(R.id.emptyTextView)
        db = FirebaseFirestore.getInstance()
        departmentSpinner = findViewById(R.id.spinnerDepartment)
        deviceListView = findViewById(R.id.deviceListView)

        loadDepartments()

        departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedDeptId = departments[position].id
                loadDevicesForDepartment(selectedDeptId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val device = devices[position]
            toggleDeviceStatus(device)
        }
    }

    private fun loadDepartments() {
        db.collection("departments").get()
            .addOnSuccessListener { result ->
                departments.clear()
                for (doc in result) {
                    val department = Department(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unnamed",
                        location = doc.getString("location") ?: "Unknown",
                        is_active = doc.getBoolean("is_active") ?: true
                    )
                    if (department.is_active)
                        departments.add(department)
                }

                val names = departments.map { "${it.name} - ${it.location}" }
                departmentSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.ErrorLoadingData), Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadDevicesForDepartment(departmentId: String) {
        db.collection("devices")
            .whereEqualTo("departmentId", departmentId)
            .get()
            .addOnSuccessListener { result ->
                devices.clear()
                for (doc in result) {
                    val device = Device(
                        id = doc.id,
                        branch = doc.getString("branch") ?: "Unnamed",
                        serialNumber = doc.getString("serialNumber") ?: "",
                        model = doc.getString("model") ?: "",
                        departmentId = doc.getString("departmentId") ?: "",
                        deviceType = doc.getString("deviceType") ?: "",
                        isActive = doc.getBoolean("is_active") ?: true
                    )
                    devices.add(device)
                }

                if (devices.isEmpty()) {
                    emptyTextView.visibility = View.VISIBLE
                    deviceListView.visibility = View.GONE
                } else {
                    emptyTextView.visibility = View.GONE
                    deviceListView.visibility = View.VISIBLE
                }

                deviceAdapter = DeviceListAdapter(this, devices) { device ->
                    toggleDeviceStatus(device)
                }

                deviceListView.adapter = deviceAdapter
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.ErrorLoadingData), Toast.LENGTH_SHORT).show()
            }
    }



    private fun toggleDeviceStatus(device: Device) {
        val newStatus = !device.isActive
        db.collection("devices").document(device.id)
            .update("is_active", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "${device.branch} ${if (newStatus) getString(R.string.Enabled) else getString(R.string.Disabled)}", Toast.LENGTH_SHORT).show()
                loadDevicesForDepartment(device.departmentId)
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.ErrorUpdateData), Toast.LENGTH_SHORT).show()
            }
    }
}
