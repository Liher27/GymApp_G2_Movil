package pojo

import com.google.firebase.Timestamp

data class Exercise(
    val exerciseName: String? = null,
    val exerciseImageUrl: String? = null,
    val restTime: Int? = 0,
    val seriesNumber: Int? = 0,
    val progress: String? = null,
    val finishDate: Timestamp? = null,
    val totalTime: Int? = 0,
    val providedTime: Int? = 0
)
