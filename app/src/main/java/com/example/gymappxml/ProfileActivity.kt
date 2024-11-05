package com.example.gymappxml
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import pojo.User
import java.text.SimpleDateFormat
import java.util.Locale

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
    private var isDarkTheme = true
    private lateinit var userInfo : ArrayList<User>




    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        userFiled = findViewById(R.id.userIdFiled)
        userNameFiled = findViewById(R.id.NameFiled)
        userSurnameFiled = findViewById(R.id.SurNameFiled)
        userEmailFiled = findViewById(R.id.emailFiled)
        userBirtyDateFiled = findViewById(R.id.dateFiled)
        userType = findViewById(R.id.editText4)
        spinnerLanguage = findViewById(R.id.spinner2)
        spinnerTheme = findViewById(R.id.spinner3)

        sharedPreferences = getSharedPreferences("document_sharedPreferences", MODE_PRIVATE)
        val savedLanguage = sharedPreferences.getString("selected_language", "es")
        setLocale(savedLanguage ?: "es", this)
        val backButton: Button = findViewById(R.id.profileBackButton)

        backButton.setOnClickListener {
            val intent = Intent(this@ProfileActivity, WorkoutsActivity::class.java)
            startActivity(intent)
            finish()
        }
        ArrayAdapter.createFromResource(
            this, R.array.themeMode,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTheme.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this, R.array.Language,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerLanguage.adapter = adapter
        }

        spinnerTheme.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedThemeId = when(p2){
                    0 -> R.style.Theme_GymAppXML
                    1 -> R.style.lighttheme
                    else -> R.style.Theme_GymAppXML
                }

                setTheme(selectedThemeId)
                sharedPreferences.edit().putInt("selected_theme", selectedThemeId).apply()



            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        /*spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, p2: Int, p3: Long) {
              val selecetedlanguage = when(p2){
                  0 -> "es"
                  1 -> "en"
                  else -> "es"
              }
                setLocale(selecetedlanguage, context = baseContext)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }*/

        setupLanguageSpinner()
        showUserData()
        disableTextFiled()


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
        val userId = intent.getStringExtra("iduser")

        userId?.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val userName = document.getString("name")
                        val userSurname = document.getString("surname")
                        val userEmail = document.getString("mail")
                        val userBirtyDate = document.getTimestamp("birthDate")
                        val isTrainer = document.getBoolean("trainer")



                        if (isTrainer == true) {
                            userType.setText("Entrenador")
                        } else {
                            userType.setText("Cliente")
                        }
                        val formattedDate = userBirtyDate?.toDate()?.let {
                            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it)
                        }
                        val user = userName?.let {
                            User(
                                it,
                                userSurname,
                                userEmail,
                                formattedDate,
                                isTrainer
                            )
                        }

                        userFiled.setText(userId)
                        userNameFiled.setText(userName)
                        userSurnameFiled.setText(userSurname)
                        userEmailFiled.setText(userEmail)
                        userBirtyDateFiled.setText(formattedDate)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("showUserData", "Error getting user data: ", exception)

                }
        }
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






