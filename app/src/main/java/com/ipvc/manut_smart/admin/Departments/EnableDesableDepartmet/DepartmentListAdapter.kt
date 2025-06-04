package com.ipvc.manut_smart.admin.Departments.EnableDesableDepartmet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.ipvc.manut_smart.R
import com.ipvc.manut_smart.admin.Departments.DepartementData.Department

class DepartmentListAdapter(
    private val context: Context,
    private val departments: List<Department>,
    private val onToggleClick: (Department) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = departments.size
    override fun getItem(position: Int): Any = departments[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.item_department_toggle, parent, false)

        val department = departments[position]
        val nameText = view.findViewById<TextView>(R.id.departmentName)
        val toggleButton = view.findViewById<Button>(R.id.buttonToggle)

        nameText.text = buildString {
            append(department.id)
            append("\n")
            append(department.name)
            append(" - ")
            append(department.location)
        }

        toggleButton.text = if (department.is_active)
            context.getString(R.string.Inactive)
        else
            context.getString(R.string.Active)


        toggleButton.setOnClickListener {
            onToggleClick(department)
        }

        return view
    }
}
