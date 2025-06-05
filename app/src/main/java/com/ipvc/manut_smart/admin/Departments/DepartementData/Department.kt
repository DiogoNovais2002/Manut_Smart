package com.ipvc.manut_smart.admin.Departments.DepartementData // ou ajuste o package conforme onde criou

data class Department(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    var is_active: Boolean = true
)