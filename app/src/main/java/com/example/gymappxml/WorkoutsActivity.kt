package com.example.gymappxml

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import pojo.Exercise
import pojo.Workout


class WorkoutsActivity : AppCompatActivity() {
    private lateinit var keyid: String
    private lateinit var showLevel: TextView
    private lateinit var filterText: TextView

    private lateinit var workoutsListView: ListView
    private lateinit var exerciseListView: ListView

    private lateinit var trainerButton: Button
    private lateinit var exerciseImage: ImageView

    private lateinit var workoutsList: List<Workout>
    private lateinit var workoutsNames: List<String>
    private lateinit var workoutsAdapter: ArrayAdapter<String>
    private lateinit var exerciseList: List<String>

    private lateinit var workoutName: String
    private var workoutLevel: Int = 0
    private lateinit var id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workouts)

        trainerButton = findViewById<Button>(R.id.trainerButton)
        keyid = intent.getStringExtra("id") ?: "default_value"
        showLevel = findViewById(R.id.textView2)

        getUserLevel()
        loadWorkouts()

        if (userIsTrainer()) {
            trainerButton.setOnClickListener {
                val intent = Intent(this@WorkoutsActivity, TrainerActivity::class.java).apply {
                    putExtra("id", keyid)
                }
                startActivity(intent)
                finish()
            }
        } else {
            trainerButton.setVisibility(View.GONE);
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
            } else {
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
            loadExercises(position)
        }
    }

    private fun filterWorkouts(level: Int) {

    }

    private fun loadWorkouts() {
        workoutName = ""
        workoutsNames = mutableListOf()
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
                            document.getDocumentReference("workoutLevel")

                        workoutLevelRef?.get()?.addOnSuccessListener { workoutDocument ->
                            workoutLevel = workoutDocument.getLong("level")?.toInt()!!
                        }

                        workoutNameRef?.get()?.addOnSuccessListener { workoutDocument ->
                            workoutName = workoutDocument.getString("workoutName")!!

                            val workoutId =
                                document.getDocumentReference(workoutName)?.id

                            val workout = Workout(workoutName, workoutLevel, workoutId)

                            (workoutsList as MutableList<Workout>).add(workout)
                            (workoutsNames as MutableList<String>).add(workout.workoutName!!)

                            workoutsAdapter = ArrayAdapter(
                                this,
                                android.R.layout.simple_list_item_1,
                                workoutsNames
                            )
                            workoutsListView.adapter = workoutsAdapter

                        }

                    }
                }
        }
    }

    private fun loadExercises(index: Int) {
        val db = Firebase.firestore
        exerciseListView = findViewById(R.id.exerciseView)
        exerciseList = mutableListOf()

        id = getWorkoutId(workoutsList[index].workoutName)
        Log.e("WorkoutID", id)
        db.collection("workouts").document("workout_0")
            .collection("workoutExercises").get()
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
                    loadExerciseImage(image)
                    (exerciseList as MutableList<String>).add(
                        "Nombre: $exerciseName            Tiempo de descanso: $restTime             Número de series: $seriesNumber "
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

    private fun getWorkoutId(workoutName: String?): String {
        val db = Firebase.firestore
        var id = ""
        db.collection("workouts").whereEqualTo("workoutName", workoutName).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    id = document.id
                }
            }
        return id
    }

    private fun loadExerciseImage(image: String?) {
        exerciseImage = findViewById(R.id.exerciseImage)
        val decodedString: ByteArray = Base64.decode(image, Base64.DEFAULT)
        val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        exerciseImage.setImageBitmap(decodedByte)
    }

    private fun getUserLevel() {
        val db = Firebase.firestore
        keyid.let { id ->
            db.collection("users").document(id).get().addOnSuccessListener { document ->
                if (document != null) {
                    val level = document.getLong("userLevel")
                    val levelText = "Nivel del usuario: $level"
                    showLevel.text = levelText
                }
            }.addOnFailureListener { exception ->
                Log.e("showUserData", "Error getting user data: ", exception)

            }
        }
    }

    private fun userIsTrainer(): Boolean {
        var ret = false
        val db = Firebase.firestore
        keyid.let { id ->
            db.collection("users").document(id).get().addOnSuccessListener { document ->
                if (document != null) {
                    val isTrainer = document.getBoolean("trainer")
                    ret = isTrainer ?: false
                }
            }
        }
        return ret
    }
}