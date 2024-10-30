package pojo

data class Workout(
    val workoutName: String? = null,
    val workoutLevel: Int? = 0,
    val workoutId: String? = null,
    val videoUrl: String? = null,
    val exerciseNumber: Int? = 0,
    val exerciseList: List<Exercise>? = null
)