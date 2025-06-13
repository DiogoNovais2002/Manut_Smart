package com.ipvc.manut_smart.technical.IssueData
import com.google.firebase.Timestamp

data class Issue(
    val date_registration: Timestamp? = null,
    val description: String = "",
    val deviceid: String = "",
    val photoBase64: String = "",
    val state: String = "",
    val title: String = "",
    val urgency: Boolean = false,
    val uid: String = "",
    var id: String = ""
)



