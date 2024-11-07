package pojo

import com.google.firebase.Timestamp

data class Historic (
    var workoutName: String? = null,
    var workoutLevel: Int? = 0,
    var workoutId: String? = null,
    var videoUrl: String? = null,
    var exerciseNumber: Int? = 0,
    var exerciseList: List<Exercise>? = null,
    var exercisePercent: String? = null,
    var finishDate: Timestamp? = null,
    var providedTime: Int? = 0,
    var totalTime: Int? = 0
)