package com.example.gymappxml

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import pojo.Exercise
import pojo.Workout


class WorkoutsActivity : AppCompatActivity() {

    private lateinit var showLevel: TextView
    private lateinit var filterText: TextView
    private lateinit var workoutsListView: ListView
    private lateinit var exerciseListView: ListView

    private lateinit var chooser: Intent

    private lateinit var workoutsList: List<Workout>
    private lateinit var workoutsNames: List<String>
    private lateinit var exerciseList: List<String>
    private lateinit var historicList: List<Exercise>
    private lateinit var workoutsAdapter: ArrayAdapter<String>

    private lateinit var keyid: String
    private lateinit var videoUrl: String
    private lateinit var workoutName: String
    private lateinit var workoutInfo: String
    private lateinit var id: String

    private var workoutLevel: Int = 0

    private var isTrainer: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workouts)
        keyid = intent.getStringExtra("id") ?: "default_value"
        showLevel = findViewById(R.id.textView2)
        filterText = findViewById(R.id.editNumberText)
        videoUrl = ""
        getUserLevel()
        lifecycleScope.launch {
            loadWorkouts()
            isTrainer = userIsTrainer()

        }

        findViewById<Button>(R.id.trainerButton).setOnClickListener {
            if (isTrainer) {
                val intent = Intent(this@WorkoutsActivity, TrainerActivity::class.java).apply {
                    putExtra("id", keyid)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "No eres un entrenador", Toast.LENGTH_SHORT).show()
            }

        }

        findViewById<Button>(R.id.workoutBackBtn).setOnClickListener {
            val intent = Intent(this@WorkoutsActivity, LoginActivity::class.java).apply {
            }
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.workoutFilteringBtn).setOnClickListener {
            if (filterText.text.isEmpty()) {
                Toast.makeText(this, "No has seleccionado ningún nivel", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch {
                    loadWorkouts()
                }
            } else {
                val level = filterText.text.toString().toInt()
                filterWorkouts(level)
            }

        }

        findViewById<Button>(R.id.button3).setOnClickListener {
            if (videoUrl.isNotEmpty()) {
                webActionOnClick(videoUrl)
            } else {
                Toast.makeText(this, "No hay ningun video", Toast.LENGTH_SHORT).show()
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
            videoUrl = workoutsList[position].videoUrl.toString()
        }
    }


    private fun filterWorkouts(level: Int) {
        if (level < workoutsList.size) {
            for (workout in workoutsList) {
                if (workout.workoutLevel == level) {
                    (workoutsNames as MutableList<String>).add(workout.workoutName!!)
                    workoutsAdapter.notifyDataSetChanged()
                } else {
                    (workoutsNames as MutableList<String>).remove(workout.workoutName!!)
                }
            }
        } else
            Toast.makeText(this, "No hay ningun nivel", Toast.LENGTH_SHORT).show()

    }

    private suspend fun loadWorkouts() {
        workoutName = ""
        workoutsNames = mutableListOf()
        historicList = mutableListOf()
        val db = Firebase.firestore
        val userId = intent.getStringExtra("id")
        workoutsListView = findViewById(R.id.workoutList)
        workoutsList = mutableListOf()

        userId?.let { id ->
            withContext(Dispatchers.IO) {
                val result =
                    db.collection("users").document(id).collection("userHistory_0").get()
                        .await()

                for (document in result) {

                    val workoutNameRef = document.getDocumentReference("workoutName")
                    val workoutLevelRef = document.getDocumentReference("workoutLevel")
                    val workoutProgress = document.getString("exercisePercent")
                    val workoutProvidedTime = document.getLong("providedTime")?.toInt()!!
                    val workoutTotalTime = document.getLong("totalTime")?.toInt()!!
                    val workoutFinishDate = document.getTimestamp("finishDate")

                    workoutLevelRef?.get()?.await()?.let { workoutDocument ->
                        workoutLevel = workoutDocument.getLong("level")?.toInt()!!
                    }

                    workoutNameRef?.get()?.await()?.let { workoutDocument ->
                        workoutName = workoutDocument.getString("workoutName")!!
                        val workoutId = getDocumentID(workoutName)
                        val workout = Workout(
                            workoutName,
                            workoutLevel,
                            workoutId,
                            null,
                            null,
                            null,
                            workoutProgress,
                            workoutFinishDate,
                            workoutProvidedTime,
                            workoutTotalTime
                        )
                       workoutInfo = "Nombre: $workoutName Nivel $workoutLevel"
                        (workoutsList as MutableList<Workout>).add(workout)
                        (workoutsNames as MutableList<String>).add(workoutInfo)

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
                db.collection("workouts").get().addOnSuccessListener { workoutresult ->
                    for (document in workoutresult) {
                        val workoutUrl = document.getString("video")
                        workoutsList.forEach { workout ->
                            if (workout.workoutName == document.getString("workoutName")) {
                                workout.videoUrl = workoutUrl
                            }
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
                querySnapshot.documents[0].id
            } else {
                ""
            }
        }

    }

    private fun loadExercises(index: Int) {
        var i = 0
        exerciseListView = findViewById(R.id.exerciseView)
        exerciseList = mutableListOf()
        val db = Firebase.firestore
        id = workoutsList[index].workoutId.toString()

        db.collection("workouts").document(id)
            .collection("workoutExercises").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val exerciseName =
                        document.getString("exerciseName")

                    (exerciseList as MutableList<String>).add(
                        "Nombre: $exerciseName"
                    )
                    (exerciseList as MutableList<String>).add(
                        "Tiempo total: ${workoutsList[i].totalTime}"
                    )
                    (exerciseList as MutableList<String>).add(
                        "Tiempo proporcionado: ${workoutsList[i].providedTime}"
                    )
                    (exerciseList as MutableList<String>).add(
                        "Porcentaje de progreso: ${workoutsList[i].exercisePercent}"
                    )
                    (exerciseList as MutableList<String>).add(
                        "Fecha de finalización: ${workoutsList[i].finishDate?.toDate()}"
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

                    i++
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
            }.addOnFailureListener {
                Toast.makeText(this, "Error al obtener el nivel del usuario", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private suspend fun userIsTrainer(): Boolean {
        return withContext(Dispatchers.IO) {
            val querySnapshot =
                Firebase.firestore.collection("users").document(keyid).get().await()
            querySnapshot?.getBoolean("trainer") ?: false
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun webActionOnClick(videoUrl: String) {
        if (videoUrl.isNotEmpty()) {
            intent = Intent()
            intent.setAction(Intent.ACTION_VIEW)
            val intentUrl: String =
                if (videoUrl.contains("https://")) videoUrl else "https://$videoUrl"
            intent.setData(Uri.parse(intentUrl))

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