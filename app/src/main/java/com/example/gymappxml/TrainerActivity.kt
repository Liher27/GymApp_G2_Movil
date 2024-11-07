package com.example.gymappxml

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pojo.Exercise
import pojo.Workout


class TrainerActivity : AppCompatActivity() {

    private lateinit var filterText: TextView
    private lateinit var workoutsListView: ListView
    private lateinit var exerciseListView: ListView

    private lateinit var workoutsList: List<Workout>
    private lateinit var workoutsNames: List<String>
    private lateinit var exerciseList: List<String>
    private lateinit var workoutsAdapter: ArrayAdapter<String>
    private lateinit var workoutMap: HashMap<Int, Workout>

    private lateinit var keyid: String
    private lateinit var videoUrl: String
    private lateinit var workoutInfo: String
    private lateinit var id: String

    private var workoutSelected: Boolean = false

    private var listIndex: Int = 0

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
            if (workoutSelected) {
                lifecycleScope.launch {
                    modifyWorkout(workoutsList[listIndex])
                }
            } else
                Toast.makeText(this, "No has seleccionado ningún workout", Toast.LENGTH_SHORT)
                    .show()
        }

        findViewById<Button>(R.id.trainerDeleteBtn).setOnClickListener {
            lifecycleScope.launch {
                deleteWorkout(workoutsList[listIndex].workoutId.toString())
            }
        }

        findViewById<Button>(R.id.trainerAddBtn).setOnClickListener {
            lifecycleScope.launch {
                createWorkout()
            }
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
            workoutSelected = true
            listIndex = position
            loadExercises(listIndex)
            videoUrl = workoutsList[listIndex].video.toString()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun createWorkout() {
        val db = Firebase.firestore
        val workout = Workout()
        try {
            val workoutId = suspendCancellableCoroutine<String?> { continuation ->
                showInputDialog(this@TrainerActivity, "Ingresa el nuevo Id del workout") { input ->
                    continuation.resume(input, null)
                }
            }
            workout.workoutId = workoutId

            val workoutName = suspendCancellableCoroutine<String?> { continuation ->
                showInputDialog(this@TrainerActivity, "Ingresa el nuevo nombre") { input ->
                    continuation.resume(input, null)
                }
            }
            workout.workoutName = workoutName

            val level = suspendCancellableCoroutine<Int?> { continuation ->
                showInputDialog(
                    this@TrainerActivity,
                    "Ingresa el nuevo nivel del workout"
                ) { input ->
                    input.toIntOrNull()?.let { continuation.resume(it, null) }
                }
            }
            level?.let { workout.level = it }

            val videoUrl = suspendCancellableCoroutine<String?> { continuation ->
                showInputDialog(this@TrainerActivity, "Ingresa un nuevo link") { input ->
                    continuation.resume(input, null)
                }
            }
            workout.video = videoUrl
            Log.e("workout", workout.toString())


            lifecycleScope.launch {
                workout.workoutId?.let {
                    db.collection("workouts").document(it).set(workout).await()
                    var addMoreExercises = true
                    while (addMoreExercises) {
                        addMoreExercises = suspendCancellableCoroutine { continuation ->
                            dialogYesOrNo(
                                this@TrainerActivity,
                                workout.workoutId
                            ) { addAnother ->
                                continuation.resume(addAnother, null)
                            }
                        }
                    }
                }
                Toast.makeText(this@TrainerActivity, "Workout creado!", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(
                this@TrainerActivity,
                "No se ha podido crear el workout",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun modifyWorkout(workout: Workout) {
        val db = Firebase.firestore
        try {
            val workoutName = suspendCancellableCoroutine<String?> { continuation ->
                showInputDialog(this@TrainerActivity, "Ingresa el nuevo nombre") { input ->
                    continuation.resume(input, null)
                }
            }
            workout.workoutName = workoutName

            val level = suspendCancellableCoroutine<Int?> { continuation ->
                showInputDialog(
                    this@TrainerActivity,
                    "Ingresa el nuevo nivel del workout"
                ) { input ->
                    input.toIntOrNull()?.let { continuation.resume(it, null) }
                }
            }
            level?.let { workout.level = it }

            val videoUrl = suspendCancellableCoroutine<String?> { continuation ->
                showInputDialog(this@TrainerActivity, "Ingresa un nuevo link") { input ->
                    continuation.resume(input, null)
                }
            }
            workout.video = videoUrl

            lifecycleScope.launch {
                workout.workoutId?.let {
                    db.collection("workouts").document(it)
                        .set(workout).await()
                    var addMoreExercises = true
                    while (addMoreExercises) {
                        addMoreExercises = suspendCancellableCoroutine { continuation ->
                            dialogYesOrNo(
                                this@TrainerActivity,
                                workout.workoutId
                            ) { addAnother ->
                                continuation.resume(addAnother, null)
                            }
                        }
                    }
                    Toast.makeText(
                        this@TrainerActivity,
                        "Workout modificado!",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                this@TrainerActivity,
                "No se ha podido crear el workout",
                Toast.LENGTH_SHORT
            ).show()
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun createExercise(workoutId: String?, exercise: Exercise) {
        val exerciseName = suspendCancellableCoroutine<String?> { continuation ->
            showInputDialog(this@TrainerActivity, "Ingresa el nombre") { input ->
                continuation.resume(input, null)
            }
        }
        exercise.exerciseName = exerciseName

        val seriesNumber = suspendCancellableCoroutine<Int?> { continuation ->
            showInputDialog(this@TrainerActivity, "Ingresa las series del ejercicio") { input ->
                input.toIntOrNull()?.let { continuation.resume(it, null) }
            }
        }
        seriesNumber?.let { exercise.seriesNumber = it }

        val restTime = suspendCancellableCoroutine<Int?> { continuation ->
            showInputDialog(this@TrainerActivity, "Ingresa el tiempo de descanso") { input ->
                input.toIntOrNull()?.let { continuation.resume(it, null) }
            }
        }
        restTime?.let { exercise.restTime = it }

        val db = Firebase.firestore
        db.collection("workouts").document(workoutId.toString()).collection("workoutExercises")
            .document(exerciseName.toString())
            .set(exercise)
    }

    private suspend fun loadWorkouts() {
        workoutsNames = mutableListOf()
        workoutMap = hashMapOf()
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
                val workoutUrl = document.getString("video")
                val workoutId = workoutName?.let { getDocumentID(it) }
                val workout = Workout(
                    workoutName,
                    workoutLevel,
                    workoutId,
                    workoutUrl
                )
                workoutMap[workoutLevel] = workout
                workoutInfo = "Workout $workoutName"
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

    private fun loadExercises(index: Int) {
        exerciseListView = findViewById(R.id.exerciseView)
        exerciseList = mutableListOf()
        val db = Firebase.firestore
        id = workoutsList[index].workoutId.toString()
        val level = workoutsList[index].level
        val videoUrl = workoutsList[index].video

        (exerciseList as MutableList<String>).add(
            "Nivel: $level "
        )

        (exerciseList as MutableList<String>).add(
            "Video: $videoUrl"
        )

        (exerciseList as MutableList<String>).add(
            ""
        )

        (exerciseList as MutableList<String>).add(
            "Ejercicios:"
        )

        db.collection("workouts").document(id)
            .collection("workoutExercises").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val exerciseName =
                        document.getString("exerciseName")

                    val exerciseRestTime = document.getLong("restTime")?.toInt()!!.toString()

                    val exerciseSeriesNumber =
                        document.getLong("seriesNumber")?.toInt()!!.toString()

                    (exerciseList as MutableList<String>).add(
                        ""
                    )

                    (exerciseList as MutableList<String>).add(
                        "Nombre del ejercicio: $exerciseName"
                    )
                    (exerciseList as MutableList<String>).add(
                        "Tiempo de descanso: $exerciseRestTime minutos"
                    )

                    (exerciseList as MutableList<String>).add(
                        "Numero de series: $exerciseSeriesNumber"
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

    private fun showInputDialog(
        context: Context,
        title: String,
        onInputReceived: (String) -> Unit
    ) {
        val input = EditText(context)

        AlertDialog.Builder(context).apply {
            setTitle(title)
            setView(input)
            setPositiveButton("Aceptar") { _, _ ->
                onInputReceived(input.text.toString())
            }
            setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
        }.show()
    }

    private fun dialogYesOrNo(
        context: Context,
        workoutId: String?,
        onYesClicked: (Boolean) -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setPositiveButton("Yes") { _, _ ->
            lifecycleScope.launch {
                val exercise = Exercise(null, null, null)
                createExercise(workoutId.toString(), exercise)
            }
            onYesClicked(true)
        }
        builder.setNegativeButton("No") { _, _ ->
            onYesClicked(false)
        }
        val alert = builder.create()
        alert.setMessage("Do you want to add an exercise?")
        alert.show()
    }

    private fun filterWorkouts(level: Int) {
        if (level < workoutsList.size) {
            for (workout in workoutsList) {
                if (workout.level == level) {
                    (workoutsNames as MutableList<String>).add(workout.workoutName!!)
                    workoutsAdapter.notifyDataSetChanged()
                } else {
                    (workoutsNames as MutableList<String>).remove(workout.workoutName!!)
                }
            }
        } else
            Toast.makeText(this, "No hay ningun nivel", Toast.LENGTH_SHORT).show()

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
}