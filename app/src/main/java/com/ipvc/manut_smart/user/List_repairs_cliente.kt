package com.ipvc.manut_smart.user
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ipvc.manut_smart.R

class List_repairs_cliente : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list_repairs_cliente)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnExpand = findViewById<FrameLayout>(R.id.btnExpand)
        val detailsLayout = findViewById<LinearLayout>(R.id.detailsLayout)

        btnExpand.setOnClickListener {
            detailsLayout.visibility =
                if (detailsLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

}