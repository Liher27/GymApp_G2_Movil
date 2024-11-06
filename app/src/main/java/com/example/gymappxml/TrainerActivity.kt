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
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pojo.Workout


class TrainerActivity : AppCompatActivity() {

    private lateinit var filterText: TextView
    private lateinit var workoutsListView: ListView
    private lateinit var exerciseListView: ListView

    private lateinit var workoutsList: List<Workout>
    private lateinit var workoutsNames: List<String>
    private lateinit var exerciseList: List<String>
    private lateinit var workoutsAdapter: ArrayAdapter<String>
    private lateinit var historicInfo: HashMap<Int, Workout>

    private lateinit var keyid: String
    private lateinit var videoUrl: String
    private lateinit var workoutName: String
    private lateinit var workoutInfo: String
    private lateinit var id: String

    private var listIndex: Int = 0
    private var workoutLevel: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer)
        keyid = intent.getStringExtra("id") ?: "default_value"
        filterText = findViewById(R.id.editNumberText)
        videoUrl = ""

        lifecycleScope.launch {
            loadWorkouts()
        }

        findViewById<Button>(R.id.workoutBackBtn).setOnClickListener {
            val intent = Intent(this@TrainerActivity, WorkoutsActivity::class.java).apply {
                putExtra("id", keyid)
            }
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.trainerModifyButton).setOnClickListener {

        }

        findViewById<Button>(R.id.trainerDeleteBtn).setOnClickListener {
            lifecycleScope.launch {
                deleteWorkout(workoutsList[listIndex].workoutId.toString())
            }
        }

        findViewById<Button>(R.id.trainerAddBtn).setOnClickListener {

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

        workoutsListView.setOnItemClickListener { _, _, position, _ ->
            listIndex = position
            loadExercises(listIndex)
            videoUrl = workoutsList[listIndex].videoUrl.toString()
        }
    }

    private suspend fun deleteWorkout(workoutId: String): Boolean {
        return try {
            Firebase.firestore.collection("workouts").document(workoutId).delete().await()
            true
        } catch (e: Exception) {
            false
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
        workoutsNames = mutableListOf()
        historicInfo = hashMapOf()
        val db = Firebase.firestore
        workoutsListView = findViewById(R.id.workoutList)
        workoutsList = mutableListOf()

        withContext(Dispatchers.IO) {
            val result =
                db.collection("workouts").get()
                    .await()

            for (document in result) {
                val workoutName = document.getString("workoutName")
                val workoutLevel = document.getLong("level")?.toInt()!!
                val workoutProgress = document.getString("exercisePercent")
                val workoutUrl = document.getString("video")
                val workoutId = workoutName?.let { getDocumentID(it) }
                val workout = Workout(
                    workoutName,
                    workoutLevel,
                    workoutId,
                    workoutUrl,
                    null,
                    null,
                    workoutProgress,
                    null,
                    null,
                    null
                )
                historicInfo[workoutLevel] = workout
                workoutInfo = "Nombre: $workoutName Nivel $workoutLevel"
                (workoutsList as MutableList<Workout>).add(workout)
                (workoutsNames as MutableList<String>).add(workoutInfo)

                withContext(Dispatchers.Main) {
                    workoutsAdapter = ArrayAdapter(
                        this@TrainerActivity,
                        android.R.layout.simple_list_item_1,
                        workoutsNames
                    )
                    workoutsListView.adapter = workoutsAdapter
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
                        "Tiempo total: ${historicInfo[workoutsList[index].workoutLevel]?.totalTime}"
                    )
                    (exerciseList as MutableList<String>).add(
                        "Tiempo proporcionado: ${historicInfo[workoutsList[index].workoutLevel]?.providedTime}"
                    )
                    (exerciseList as MutableList<String>).add(
                        "Porcentaje de progreso: ${historicInfo[workoutsList[index].workoutLevel]?.exercisePercent}"
                    )
                    (exerciseList as MutableList<String>).add(
                        "Fecha de finalización: ${historicInfo[workoutsList[index].workoutLevel]?.finishDate?.toDate()}"
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
}