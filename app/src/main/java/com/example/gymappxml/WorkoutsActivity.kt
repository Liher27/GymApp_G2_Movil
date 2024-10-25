package com.example.gymappxml

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WorkoutsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workouts)

        val button: Button = findViewById(R.id.button4)
        button.setOnClickListener {
            val intent = Intent(this@WorkoutsActivity, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}