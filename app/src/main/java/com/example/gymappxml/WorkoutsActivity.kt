package com.example.gymappxml

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WorkoutsActivity : AppCompatActivity() {
    private lateinit var idView : TextView
    private lateinit var keyid : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workouts)

        keyid= intent.getStringExtra("id").toString()

        Log.i("this user id ", "es $keyid")
        val button: Button = findViewById(R.id.button4)
        button.setOnClickListener {
            val intent = Intent(this@WorkoutsActivity, ProfileActivity::class.java).apply {
                putExtra("iduser",keyid)
            }

            startActivity(intent)

            finish()
        }
    }
}