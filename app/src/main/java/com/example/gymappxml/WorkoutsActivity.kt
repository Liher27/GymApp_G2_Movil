package com.example.gymappxml

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import pojo.Exercise
import pojo.Workout


class WorkoutsActivity : AppCompatActivity() {
    private lateinit var idView: TextView
    private lateinit var keyid: String
    private lateinit var showLevel: TextView
    private lateinit var filterButton: Button
    private lateinit var filterText: TextView
    private lateinit var trainerButton: Button
    private lateinit var backButton: Button

    private lateinit var workoutsList: List<Workout>
    private lateinit var exerciseList: List<Exercise>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workouts)

        keyid = intent.getStringExtra("id") ?: "default_value"
        showLevel = findViewById(R.id.textView2)

        getUserLevel()
        loadWorkouts()
        val button: Button = findViewById(R.id.profileButton)
        button.setOnClickListener {
            val intent = Intent(this@WorkoutsActivity, ProfileActivity::class.java).apply {
                putExtra("iduser", keyid)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun loadWorkouts() {
        val db = Firebase.firestore
        val userId = intent.getStringExtra("id")

        userId?.let { id ->
            db.collection("users")
                .document(id)
                .collection("userHistory_0")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            workoutsList = task.result.toObjects(Workout::class.java)
                        }
                    } else {
                        print("Error getting documents:")
                    }
                }
        }
    }

    private fun getUserLevel() {
        val db = Firebase.firestore
        keyid.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val level = document.getLong("userLevel")
                        showLevel.text = "Nivel del usuario: $level"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("showUserData", "Error getting user data: ", exception)

                }
        }
    }
}