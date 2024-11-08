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
    private lateinit var exerciseList: List<Exercise>
    private lateinit var workoutsNames: List<String>
    private lateinit var exerciseInfo: List<String>
    private lateinit var workoutsAdapter: ArrayAdapter<String>
    private lateinit var exerciseAdapter: ArrayAdapter<String>
    private lateinit var workoutMap: HashMap<Int, Workout>

    private lateinit var keyid: String
    private lateinit var videoUrl: String
    private lateinit var workoutInfo: String
    private lateinit var id: String

    private var workoutSelected: Boolean = false
    private var exerciseSelected: Boolean = false

    private var listIndex: Int = 0
    private var exerciseListIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer)
        keyid = intent.getStringExtra("id") ?: "default_value"
        filterText = findViewById(R.id.editNumberText)
        exerciseListView = findViewById(R.id.exerciseView)
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
            } else if (exerciseSelected) {
                lifecycleScope.launch {
                    modifyExercise(exerciseList[exerciseListIndex])
                }
            } else
                Toast.makeText(this, "No has seleccionado ningún workout", Toast.LENGTH_SHORT)
                    .show()
        }

        findViewById<Button>(R.id.trainerDeleteBtn).setOnClickListener {
            if (workoutSelected) {
                lifecycleScope.launch {
                    deleteWorkout(workoutsList[listIndex].workoutId.toString())
                }
            } else
                Toast.makeText(this, "No has seleccionado ningún workout", Toast.LENGTH_SHORT)
                    .show()
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
            exerciseSelected = false
            listIndex = position
            loadExercises(listIndex)
            videoUrl = workoutsList[listIndex].video.toString()
        }
        exerciseListView.setOnItemClickListener { _, _, position, _ ->
            exerciseSelected = true
            workoutSelected = false
            exerciseListIndex = position
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
                                workout.workoutId!!
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
        exerciseInfo = mutableListOf()
        val db = Firebase.firestore
        id = workoutsList[index].workoutId.toString()
        val level = workoutsList[index].level
        exerciseList = mutableListOf()
        val videoUrl = workoutsList[index].video

        (exerciseInfo as MutableList<String>).add(
            "Nivel: $level "
        )

        (exerciseInfo as MutableList<String>).add(
            "Video: $videoUrl"
        )

        (exerciseInfo as MutableList<String>).add(
            ""
        )

        (exerciseInfo as MutableList<String>).add(
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

                    val exercise = Exercise(
                        exerciseName,
                        exerciseRestTime.toInt(),
                        exerciseSeriesNumber.toInt()
                    )
                    (exerciseList as MutableList<Exercise>).add(
                        exercise
                    )

                    (exerciseInfo as MutableList<String>).add(
                        ""
                    )

                    (exerciseInfo as MutableList<String>).add(
                        "Nombre del ejercicio: $exerciseName"
                    )
                    (exerciseInfo as MutableList<String>).add(
                        "Tiempo de descanso: $exerciseRestTime minutos"
                    )

                    (exerciseInfo as MutableList<String>).add(
                        "Numero de series: $exerciseSeriesNumber"
                    )

                    exerciseAdapter =
                        ArrayAdapter(
                            this@TrainerActivity,
                            android.R.layout.simple_list_item_1,
                            exerciseInfo
                        )
                    exerciseListView.adapter = exerciseAdapter

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
        workoutId: String,
        onYesClicked: (Boolean) -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setPositiveButton("Yes") { _, _ ->
            lifecycleScope.launch {
                val exercise = Exercise(null, null, null)
                createExercise(workoutId, exercise)
            }
            onYesClicked(true)
        }
        builder.setNegativeButton("No") { _, _ ->
            onYesClicked(false)
        }
        val alert = builder.create()
        alert.setMessage("Quieres crear un ejercicio nuevo?")
        alert.show()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun modifyExercise(exercise: Exercise) {
        val db = Firebase.firestore
        val exerciseId = exercise.exerciseName
        val exerciseName = suspendCancellableCoroutine<String?> { continuation ->
            showInputDialog(this@TrainerActivity, "Ingresa el nuevo nombre") { input ->
                continuation.resume(input, null)
            }
        }
        if (exerciseName.toString().isNotEmpty())
            exercise.exerciseName = exerciseName

        val seriesNumber = suspendCancellableCoroutine<Int?> { continuation ->
            showInputDialog(this@TrainerActivity, "Ingresa las series del ejercicio") { input ->
                input.toIntOrNull()?.let { continuation.resume(it, null) }
            }
        }
        if (seriesNumber.toString().isNotEmpty())
            seriesNumber?.let { exercise.seriesNumber = it }

        val restTime = suspendCancellableCoroutine<Int?> { continuation ->
            showInputDialog(this@TrainerActivity, "Ingresa las series del ejercicio") { input ->
                input.toIntOrNull()?.let { continuation.resume(it, null) }
            }
        }
        if (restTime.toString().isNotEmpty())
            restTime?.let { exercise.restTime = it }

        db.collection("workouts").document(workoutsList[listIndex].workoutId.toString())
            .collection("workoutExercises").document(exerciseId.toString()).set(exercise)

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