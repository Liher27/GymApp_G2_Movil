package pojo

import com.google.firebase.Timestamp

data class Workout(
    val workoutName: String? = null,
    val workoutLevel: Int? = 0,
    val workoutId: String? = null,
    var videoUrl: String? = null,
    val exerciseNumber: Int? = 0,
    val exerciseList: List<Exercise>? = null,
    val exercisePercent : String? =null,
    val finishDate: Timestamp? = null,
    val providedTime: Int? = 0,
    val totalTime: Int? = 0

)