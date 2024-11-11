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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import pojo.User
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private lateinit var userField: EditText
    private lateinit var userNameField: EditText
    private lateinit var userSurnameField: EditText
    private lateinit var userEmailField: EditText
    private lateinit var userBirthDateField: EditText
    private lateinit var userType: EditText

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var spinnerLanguage: Spinner
    private lateinit var spinnerTheme: Spinner
    private lateinit var editor: Editor
    private lateinit var useride: String
    private lateinit var user: User


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        useride = intent.getStringExtra("iduser").toString()
        userField = findViewById(R.id.userIdFiled)
        userNameField = findViewById(R.id.NameFiled)
        userSurnameField = findViewById(R.id.SurNameFiled)
        userEmailField = findViewById(R.id.emailFiled)
        userBirthDateField = findViewById(R.id.dateFiled)
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

        spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedThemeId = when (p2) {
                    0 -> R.style.lightTheme
                    1 -> R.style.nightTheme
                    else -> R.style.nightTheme
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
    }

    private fun setupLanguageSpinner() {
        val savedLanguage = sharedPreferences.getString("selected_language", "en")
        val position = when (savedLanguage) {
            "en" -> 0
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
                    sharedPreferences.edit().putString("selected_language", selectedLanguage)
                        .apply()
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
                        val userBirthDate = document.getTimestamp("birthDate")
                        Log.e("user", userBirthDate.toString())
                        val isTrainer = document.getBoolean("trainer")
                        val formattedDate = userBirthDate?.toDate()?.let {
                            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it)
                            Log.e("it", it.toString())
                            user = User(
                                userName.toString(),
                                userSurname,
                                userEmail,
                                it.toString(),
                                isTrainer
                            )
                        }

                        loadUserInfo(user)

                        editor.putString("thisId", useride)
                        editor.apply()

                    }
                }
                .addOnFailureListener { _ ->
                    Toast.makeText(
                        this@ProfileActivity,
                        "Error al cargar los datos",
                        Toast.LENGTH_SHORT
                    ).show()

                }
        }
    }

    private fun loadUserInfo(user: User) {
        userField.setText(useride)
        userNameField.setText(user.name)
        userSurnameField.setText(user.surname)
        userEmailField.setText(user.mail)
        userBirthDateField.setText(user.birthDate.toString())
        userType.setText(if (user.trainer == true) "Entrenador" else "Cliente")

    }

    private fun disableTextFiled() {
        userField.isEnabled = false
        userNameField.isEnabled = false
        userSurnameField.isEnabled = false
        userEmailField.isEnabled = false
        userBirthDateField.isEnabled = false
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






