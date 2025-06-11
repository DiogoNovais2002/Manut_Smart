package com.ipvc.manut_smart.technical.IssueData
import com.google.firebase.Timestamp

data class Issue(
    val date_registration: Timestamp? = null,
    val description: String = "",
    val deviceid: String = "",
    val state: String = "",
    val title: String = "",
    val uid: String = "",
    val urgency: Boolean = false,
    var id: String = ""
)



