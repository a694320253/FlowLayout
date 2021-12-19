package com.blog.flowlayout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import com.example.lib.FlowLayout

class MainActivity2 : AppCompatActivity() {
    private lateinit var tabArray: Array<String>
    private var tabIndex: Int = 0
    private lateinit var flowlayout: FlowContentLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        tabArray = resources.getStringArray(R.array.tab_array)
        flowlayout = findViewById(R.id.flowlayout)
        flowlayout.addViews(tabArray.toMutableList())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
}