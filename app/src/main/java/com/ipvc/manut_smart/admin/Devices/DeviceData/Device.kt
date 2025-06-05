package com.ipvc.manut_smart.admin.Devices.DeviceData

data class Device(
    val id: String,
    val branch: String,
    val serialNumber: String,
    val model: String,
    val departmentId: String,
    val deviceType: String,
    val isActive: Boolean
)