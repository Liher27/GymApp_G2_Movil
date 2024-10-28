package com.example.gymappxml

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import kotlin.math.log

class WorkoutsActivity : AppCompatActivity() {
    private lateinit var idView : TextView
    private lateinit var keyid : String
    private lateinit var showLevel : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workouts)

        keyid = intent.getStringExtra("id") ?: "default_value"
        showLevel = findViewById(R.id.textView2)


        getUserLevel()
        val button: Button = findViewById(R.id.button4)
        button.setOnClickListener {
            val intent = Intent(this@WorkoutsActivity, ProfileActivity::class.java).apply {
                putExtra("iduser",keyid)
            }
            startActivity(intent)
            finish()
        }
    }
    private fun getUserLevel(){
        val db = Firebase.firestore
        keyid.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val level = document.getLong("userLevel")
                        showLevel.setText("Nivel del usuario" + " " +level)
                        Log.i("this user level","is${level}")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("showUserData", "Error getting user data: ", exception)

                }
        }
    }
}