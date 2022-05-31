package com.example.blender

import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recycler = findViewById<RecyclerView>(R.id.discussions)
        val adapter = DiscussionRecyclerAdapter()
        recycler.adapter= adapter
        recycler.layoutManager= LinearLayoutManager(this)
        val list = mutableListOf(Discussion("Alec Berney", "123"), Discussion("Eric Broutba", "456"), Discussion("Manu", "voleur", true))
        for(i in 1..20) {
            list.add(Discussion("123", "123"))
        }
        adapter.items = list
    }
}
