package com.ipvc.manut_smart.admin.Devices.DeviceData

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.ipvc.manut_smart.R

class DeviceListAdapter(
    private val context: Context,
    private val devices: List<Device>,
    private val onToggleClick: (Device) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = devices.size
    override fun getItem(position: Int): Any = devices[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.item_device_toggle, parent, false)

        val device = devices[position]
        val infoText = view.findViewById<TextView>(R.id.deviceInfo)
        val toggleButton = view.findViewById<Button>(R.id.buttonToggle)

        infoText.text = buildString {
            append(device.branch)
            append(" - ")
            append(device.model)
            append("\n")
            append(device.deviceType)
            append(" • SN: ")
            append(device.serialNumber)
        }

        toggleButton.text = if (device.isActive)
            context.getString(R.string.Inactive)
        else
            context.getString(R.string.Active)

        toggleButton.setOnClickListener {
            onToggleClick(device)
        }

        return view
    }
}
