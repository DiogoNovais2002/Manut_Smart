package com.ipvc.manut_smart.technical.IssueData
import com.google.firebase.Timestamp

data class Issue(
    val date_registration: Timestamp? = null,
    val description: String = "",
    val deviceid: String = "",
    val state: String = "",
    val title: String = "",
    val uid: String = "",
    val urgency: String = "",
    var id: String = ""
)


fun Issue.urgencyLevel(): Int {
    return when (this.urgency?.lowercase()) {
        "alta" -> 2
        "media" -> 1
        "baixa" -> 0
        else -> 0
    }
}
