package com.ipvc.manut_smart.user.NewIssue

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ipvc.manut_smart.LoginActivity
import com.ipvc.manut_smart.R
import java.io.ByteArrayOutputStream
import java.util.*

class NewIssueUserActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1001
    private var photoUri: Uri? = null
    private val firestore = FirebaseFirestore.getInstance()
    private var deptListener: ListenerRegistration? = null
    private var deviceListener: ListenerRegistration? = null
    private var deviceDataList: MutableList<com.google.firebase.firestore.DocumentSnapshot> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_issue_user)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_scroll)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java)
                .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
            finish()
            return
        }

        val rgUrgency     = findViewById<RadioGroup>(R.id.urgency_group)
        val spinnerDept   = findViewById<Spinner>(R.id.spinnerDepartment)
        val spinnerDevice = findViewById<Spinner>(R.id.spinnerDevice)
        val etFault       = findViewById<EditText>(R.id.editTextFault)
        val etDescription = findViewById<EditText>(R.id.editTextDescription)
        val imgPhoto      = findViewById<ImageView>(R.id.imageViewPhoto)
        val btnCancel     = findViewById<Button>(R.id.btnCancel)
        val btnConfirm    = findViewById<Button>(R.id.btnConfirm)

        val deptAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mutableListOf())
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDept.adapter = deptAdapter

        deptListener = firestore.collection("departments")
            .whereEqualTo("is_active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, getString(R.string.ErrorLoadingData) + " ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { it.getString("location") ?: "" } ?: emptyList()
                deptAdapter.clear()
                deptAdapter.addAll(list)
                deptAdapter.notifyDataSetChanged()
            }

        spinnerDept.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedDept = deptAdapter.getItem(position) ?: return
                loadDevicesForDepartment(selectedDept, spinnerDevice)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        imgPhoto.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(it, PICK_IMAGE_REQUEST)
            }
        }

        btnCancel.setOnClickListener { finish() }

        btnConfirm.setOnClickListener {
            val urgency = when (rgUrgency.checkedRadioButtonId) {
                R.id.urgency_low    -> false
                R.id.urgency_medium -> true
                else                -> false
            }

            val selectedDevicePosition = spinnerDevice.selectedItemPosition
            val deviceId = if (selectedDevicePosition >= 0 && selectedDevicePosition < deviceDataList.size)
                deviceDataList[selectedDevicePosition].id else ""

            val description = etDescription.text.toString().trim()
            val title       = etFault.text.toString().trim()
            val uid         = user.uid
            val timestamp   = Date()

            val photoBase64 = photoUri?.let { uriToBase64(it, 600, 600) }

            val issue = hashMapOf(
                "date_registration"   to timestamp,
                "description"         to description,
                "deviceid"            to deviceId,
                "state"               to "pending",
                "title"               to title,
                "userId"              to uid,
                "urgency"             to urgency,
                "photoBase64"         to photoBase64
            )

            firestore.collection("issue")
                .add(issue)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.IssueRegistered), Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun loadDevicesForDepartment(deptLocation: String, spinnerDevice: Spinner) {
        deviceListener?.remove()
        val deviceAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mutableListOf())
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDevice.adapter = deviceAdapter
        deviceDataList.clear()

        firestore.collection("departments")
            .whereEqualTo("location", deptLocation)
            .get()
            .addOnSuccessListener { snapshot ->
                val deptId = snapshot.documents.firstOrNull()?.id ?: return@addOnSuccessListener

                deviceListener = firestore.collection("devices")
                    .whereEqualTo("departmentId", deptId)
                    .addSnapshotListener { deviceSnapshot, error ->
                        if (error != null) {
                            Toast.makeText(this, getString(R.string.ErrorLoadingData) + "${ error.message}", Toast.LENGTH_LONG).show()
                            return@addSnapshotListener
                        }
                        val list = deviceSnapshot?.documents?.map { doc ->
                            val serial = doc.getString("serialNumber") ?: ""
                            val branch = doc.getString("branch") ?: ""
                            val model = doc.getString("model") ?: ""
                            deviceDataList.add(doc)
                            "$serial - $branch - $model"
                        } ?: emptyList()

                        deviceAdapter.clear()
                        deviceAdapter.addAll(list)
                        deviceAdapter.notifyDataSetChanged()

                        spinnerDevice.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            photoUri = data?.data
            findViewById<ImageView>(R.id.imageViewPhoto).setImageURI(photoUri)
        }
    }

    private fun uriToBase64(uri: Uri, maxWidth: Int, maxHeight: Int): String? {
        val input = contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(input)
        input.close()

        val scaledBitmap = Bitmap.createScaledBitmap(
            originalBitmap,
            maxWidth,
            maxHeight,
            true
        )

        val baos = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val bytes = baos.toByteArray()
        baos.close()

        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    override fun onDestroy() {
        super.onDestroy()
        deptListener?.remove()
        deviceListener?.remove()
    }
}