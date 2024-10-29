package com.example.gymappxml

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import pojo.Exercise


class WorkoutsActivity : AppCompatActivity() {
    private lateinit var keyid: String
    private lateinit var showLevel: TextView
    private lateinit var filterText: TextView

    private lateinit var workoutsListView: ListView
    private lateinit var exerciseListView: ListView

    private lateinit var workoutsList: List<String>
    private lateinit var exerciseList: List<Exercise>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workouts)

        keyid = intent.getStringExtra("id") ?: "default_value"
        showLevel = findViewById(R.id.textView2)

        getUserLevel()
        loadWorkouts()

        findViewById<Button>(R.id.trainerButton).setOnClickListener {
            val intent = Intent(this@WorkoutsActivity, TrainerActivity::class.java).apply {
                putExtra("id", keyid)
            }
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.workoutBackBtn).setOnClickListener {
            val intent = Intent(this@WorkoutsActivity, LoginActivity::class.java).apply {
            }
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.workoutFilteringBtn).setOnClickListener {
        if (filterText.text.toString().isEmpty()) {
            Toast.makeText(this, "No has seleccionado ningún nivel", Toast.LENGTH_SHORT).show()
        }else{
            val level = filterText.text.toString().toInt()
            filterWorkouts(level)
        }

        }

        findViewById<Button>(R.id.profileButton).setOnClickListener {
            val intent = Intent(this@WorkoutsActivity, ProfileActivity::class.java).apply {
                putExtra("iduser", keyid)
            }
            startActivity(intent)
            finish()
        }

        workoutsListView.setOnItemClickListener { _, _, position, _ ->
            val workoutName = workoutsListView.getItemAtPosition(position).toString()
            loadExercises(workoutName)
        }
    }

    private fun filterWorkouts(level: Int) {

    }

    private fun loadWorkouts() {
        val db = Firebase.firestore
        val userId = intent.getStringExtra("id")
        workoutsListView = findViewById(R.id.workoutList)
        workoutsList = mutableListOf()
        userId?.let { id ->
            db.collection("users").document(id).collection("userHistory_0").get()
                .addOnSuccessListener { result ->
                    for (document in result) {

                        val workoutNameRef =
                            document.getDocumentReference("workoutName")
                        val workoutLevelRef =
                            document.getDocumentReference("workoutLvl")
                        workoutNameRef?.get()?.addOnSuccessListener { workoutDocument ->
                            val workoutName =
                                workoutDocument.getString("workoutName")
                            (workoutsList as MutableList<String>).add(workoutName ?: "")
                            val adapter =
                                ArrayAdapter(
                                    this,
                                    android.R.layout.simple_list_item_1,
                                    workoutsList
                                )
                            workoutsListView.adapter = adapter
                        }
                    }
                }
        }
    }

    private fun loadExercises(workoutName: String) {
        val db = Firebase.firestore
        exerciseListView = findViewById(R.id.workoutList)
        exerciseList = mutableListOf()

        db.collection("workouts").document(workoutName).collection("workoutExercises").get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    val exerciseName =
                        document.getString("exerciseName")
                    val image =
                        document.getString("image")
                    val restTime =
                        document.getLong("restTime")?.toInt()
                    val seriesNumber =
                        document.getLong("seriesNumber")?.toInt()

                    (exerciseList as MutableList<Exercise>).add(
                        Exercise(
                            exerciseName,
                            image, restTime, seriesNumber
                        )
                    )
                    val adapter =
                        ArrayAdapter(
                            this,
                            android.R.layout.simple_list_item_1,
                            exerciseList
                        )
                    exerciseListView.adapter = adapter
                }
            }
    }

    private fun getUserLevel() {
        val db = Firebase.firestore
        keyid.let { id ->
            db.collection("users").document(id).get().addOnSuccessListener { document ->
                if (document != null) {
                    val level = document.getLong("userLevel")
                    showLevel.text = "Nivel del usuario: $level"
                }
            }.addOnFailureListener { exception ->
                Log.e("showUserData", "Error getting user data: ", exception)

            }
        }
    }
}