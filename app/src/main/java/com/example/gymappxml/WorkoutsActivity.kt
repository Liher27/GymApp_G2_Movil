package com.example.gymappxml

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pojo.Workout


class WorkoutsActivity : AppCompatActivity() {
    private lateinit var keyid: String
    private lateinit var showLevel: TextView
    private lateinit var filterText: TextView

    private lateinit var workoutsListView: ListView
    private lateinit var exerciseListView: ListView

    private lateinit var chooser: Intent

    private lateinit var trainerButton: Button

    private lateinit var workoutsList: List<Workout>
    private lateinit var workoutsNames: List<String>
    private lateinit var workoutsAdapter: ArrayAdapter<String>
    private lateinit var exerciseList: List<String>
    private lateinit var exerciseProgress: String
    private lateinit var exerciseDateFinish: Timestamp
    private var exerciseTime: Int = 0
    private var exerciseTotalTime: Int = 0

    private lateinit var workoutName: String
    private var workoutLevel: Int = 0
    private lateinit var id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workouts)

        trainerButton = findViewById(R.id.trainerButton)
        keyid = intent.getStringExtra("id") ?: "default_value"
        showLevel = findViewById(R.id.textView2)

        getUserLevel()
        lifecycleScope.launch {
            loadWorkouts()

            if (userIsTrainer()) {
                trainerButton.visibility = View.VISIBLE
            } else {
                trainerButton.visibility = View.GONE
            }
        }



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
            } else {
               // val level = filterText.text.toString().toInt()
                //    filterWorkouts(level)
            }

        }

        findViewById<Button>(R.id.button3).setOnClickListener {
            webActionOnClick()
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


    //  private fun filterWorkouts(level: Int) {

    //   }

    private suspend fun loadWorkouts() {
        workoutName = ""
        workoutsNames = mutableListOf()
        val db = Firebase.firestore
        val userId = intent.getStringExtra("id")
        workoutsListView = findViewById(R.id.workoutList)
        workoutsList = mutableListOf()

        userId?.let { id ->
            withContext(Dispatchers.IO) {
                val result =
                    db.collection("users").document(id).collection("userHistory_0").get()
                        .await()
                Log.e("result", result.size().toString())
                for (document in result) {

                    val workoutNameRef = document.getDocumentReference("workoutName")
                    val workoutLevelRef = document.getDocumentReference("workoutLevel")

                    exerciseTime = document.getLong("providedTime")?.toInt()!!
                    exerciseTotalTime = document.getLong("totalTime")?.toInt()!!
                    exerciseProgress = document.getString("exercisePercent")!!
                    exerciseDateFinish = document.getTimestamp("finishDate")!!

                    workoutLevelRef?.get()?.await()?.let { workoutDocument ->
                        workoutLevel = workoutDocument.getLong("level")?.toInt()!!
                    }

                    workoutNameRef?.get()?.await()?.let { workoutDocument ->
                        workoutName = workoutDocument.getString("workoutName")!!
                        val workoutId = getDocumentID(workoutName)
                        val workout = Workout(workoutName, workoutLevel, workoutId)

                        (workoutsList as MutableList<Workout>).add(workout)
                        (workoutsNames as MutableList<String>).add(workout.workoutName!!)

                        withContext(Dispatchers.Main) {
                            workoutsAdapter = ArrayAdapter(
                                this@WorkoutsActivity,
                                android.R.layout.simple_list_item_1,
                                workoutsNames
                            )
                            workoutsListView.adapter = workoutsAdapter
                        }
                    }
                }
            }
        }
    }

    private suspend fun getDocumentID(workoutName: String): String {
        return withContext(Dispatchers.IO) {
            val querySnapshot = Firebase.firestore.collection("workouts")
                .whereEqualTo("workoutName", workoutName).get().await()
            if (!querySnapshot.isEmpty) {
                Log.e("ID", querySnapshot.documents[0].id)
                querySnapshot.documents[0].id
            } else {
                ""
            }
        }

    }

    private fun loadExercises(index: Int) {
        val db = Firebase.firestore
        exerciseListView = findViewById(R.id.exerciseView)
        exerciseList = mutableListOf()

        id = workoutsList[index].workoutId.toString()

        db.collection("workouts").document(id)
            .collection("workoutExercises").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val exerciseName =
                        document.getString("exerciseName")
                    val restTime =
                        document.getLong("restTime")?.toInt()
                    val seriesNumber =
                        document.getLong("seriesNumber")?.toInt()
                    (exerciseList as MutableList<String>).add(
                        "Nombre: $exerciseName"
                    )
                    (exerciseList as MutableList<String>).add(
                        "Tiempo de descanso: $restTime"
                    )
                    (exerciseList as MutableList<String>).add(
                        "Número de series: $seriesNumber"
                    )
                    (exerciseList as MutableList<String>).add(
                        ""
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
                    val levelText = "Nivel del usuario: $level"
                    showLevel.text = levelText
                }
            }.addOnFailureListener { exception ->
                Log.e("showUserData", "Error getting user data: ", exception)

            }
        }
    }

    private suspend fun userIsTrainer(): Boolean {
        return withContext(Dispatchers.IO) {
            val querySnapshot = Firebase.firestore.collection("users").document(keyid).get().await()
            querySnapshot?.getBoolean("trainer") ?: false
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun webActionOnClick() {
        if (workoutsList[1].videoUrl.toString().isNotEmpty()) {
            intent = Intent()
            intent.setAction(Intent.ACTION_VIEW)
            var videoUrl: String = workoutsList[1].videoUrl.toString()
            Log.e("URL", videoUrl)
            videoUrl = if (videoUrl.contains("http://")) videoUrl else "https://$videoUrl"
            intent.setData(Uri.parse(videoUrl))

            chooser = Intent.createChooser(intent, getText(R.string.txt_intent_web))

            if (chooser.resolveActivity(packageManager) != null) {
                startActivity(chooser)
                Toast.makeText(this, getText(R.string.txt_ok), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, getText(R.string.txt_error_2), Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, getText(R.string.txt_error_1), Toast.LENGTH_LONG).show()
        }
    }
}