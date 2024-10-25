package pojo

import java.util.Date

data class users(val name :String,val surname :String? = null, val mail :String? = null, val pass : String? = null, val trainer : Boolean? = false, val birthDate : Date? = null, val userLevel : Int? = 0) {
}