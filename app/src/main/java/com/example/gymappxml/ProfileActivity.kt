package com.example.gymappxml
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import pojo.User
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.log

class ProfileActivity : AppCompatActivity() {
    private lateinit var userFiled: EditText
    private lateinit var userNameFiled: EditText
    private lateinit var userSurnameFiled: EditText
    private lateinit var userEmailFiled: EditText
    private lateinit var userBirtyDateFiled: EditText
    private lateinit var userType: EditText

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var spinnerLanguage: Spinner
    private lateinit var spinnerTheme : Spinner
    private lateinit var editor : Editor
    private lateinit var useride : String
    private lateinit var user: User




    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        useride = intent.getStringExtra("iduser").toString()
        userFiled = findViewById(R.id.userIdFiled)
        userNameFiled = findViewById(R.id.NameFiled)
        userSurnameFiled = findViewById(R.id.SurNameFiled)
        userEmailFiled = findViewById(R.id.emailFiled)
        userBirtyDateFiled = findViewById(R.id.dateFiled)
        userType = findViewById(R.id.editText4)
        spinnerLanguage = findViewById(R.id.spinner2)
        spinnerTheme = findViewById(R.id.spinner3)
        sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE)
        editor = sharedPreferences.edit()
        val savedLanguage = sharedPreferences.getString("selected_language", "es")
        setLocale(savedLanguage ?: "es", this)
        val backButton: Button = findViewById(R.id.profileBackButton)

        backButton.setOnClickListener {
            val intent = Intent(this@ProfileActivity, WorkoutsActivity::class.java).apply {
                putExtra("id", useride)
            }
            startActivity(intent)
            finish()

        }
        ArrayAdapter.createFromResource(
            this, R.array.themeMode,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_layout)
            spinnerTheme.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this, R.array.Language,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_layout)
            spinnerLanguage.adapter = adapter
        }

        spinnerTheme.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedThemeId = when(p2){
                    0 -> R.style.Theme_GymAppXML
                    1 -> R.style.lightTheme
                    else -> R.style.Theme_GymAppXML
                }
                sharedPreferences.edit().putInt("selected_Theme", selectedThemeId).apply()
                setTheme(selectedThemeId)



            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        setupLanguageSpinner()
        showUserData()
        disableTextFiled()
        Log.i("ididid","el id es ${useride}")


    }
    private fun setupLanguageSpinner() {
        val savedLanguage = sharedPreferences.getString("selected_language", "en")
        val position = when (savedLanguage) {
            "en" ->0
            "es" -> 1
            else -> 0
        }
        spinnerLanguage.setSelection(position)

        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, p2: Int, p3: Long) {
                val selectedLanguage = when (p2) {
                    0 -> "en"
                    1 -> "es"
                    else -> "en"
                }

                val currentLanguage = sharedPreferences.getString("selected_language", "es")

                if (currentLanguage != selectedLanguage) {
                    sharedPreferences.edit().putString("selected_language", selectedLanguage).apply()
                    setLocale(selectedLanguage, context = baseContext)
                    recreate()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun showUserData() {
        val db = Firebase.firestore


        useride.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val userName = document.getString("name")
                        val userSurname = document.getString("surname")
                        val userEmail = document.getString("mail")
                        val userBirtyDate = document.getTimestamp("birthDate")
                        val isTrainer = document.getBoolean("trainer")
                        var trainerOCliente : String? = null
                        if (isTrainer == true) {
                            trainerOCliente = "Entrenador"
                        } else {
                            trainerOCliente = "Cliente"
                        }
                        val formattedDate = userBirtyDate?.toDate()?.let {
                            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it)
                        }

                        user = userName?.let {
                            User(
                                it,
                                userSurname,
                                userEmail,
                                formattedDate,
                                isTrainer
                            )
                        }!!
                        loadUserInfo(user)

                        editor.putString("thisId",useride)
                        editor.apply()

                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("showUserData", "Error getting user data: ", exception)

                }
        }
    }
    private fun loadUserInfo (user: User){
        userFiled.setText(useride)
        userNameFiled.setText(user.name)
        userSurnameFiled.setText(user.surname)
        userEmailFiled.setText(user.mail)
        val formattedDate = user.birthDate?.let {
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it)
        }
        userBirtyDateFiled.setText(formattedDate)
        userType.setText(if(user.trainer == true)"Entrenador" else "Cliente")

    }

    private fun disableTextFiled() {
        userFiled.isEnabled = false
        userNameFiled.isEnabled = false
        userSurnameFiled.isEnabled = false
        userEmailFiled.isEnabled = false
        userBirtyDateFiled.isEnabled = false
        userType.isEnabled = false
    }

}
private fun setLocale(languageCode: String, context: Context) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}






