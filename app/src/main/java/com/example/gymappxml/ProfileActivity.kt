package com.example.gymappxml

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.common.internal.service.Common
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import org.intellij.lang.annotations.Language
import java.text.SimpleDateFormat
import java.util.Date

class ProfileActivity : AppCompatActivity() {
    private lateinit var userFiled : EditText
    private lateinit var userNameFiled : EditText
    private lateinit var userSurnameFiled : EditText
    private lateinit var userEmailFiled : EditText
    private lateinit var userBirtyDateFiled : EditText
    private lateinit var userType : EditText
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchMode : Switch
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var spinnerLanguage: Spinner
    private var isDarkTheme = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        switchMode = findViewById(R.id.switch1)
        userFiled = findViewById(R.id.userIdFiled)
        userNameFiled = findViewById(R.id.NameFiled)
        userSurnameFiled = findViewById(R.id.SurNameFiled)
        userEmailFiled = findViewById(R.id.emailFiled)
        userBirtyDateFiled = findViewById(R.id.dateFiled)
        userType = findViewById(R.id.editText4)
        spinnerLanguage= findViewById(R.id.spinnerLan)

        sharedPreferences = getSharedPreferences("document_sharedPreferences", MODE_PRIVATE)

        val backButton : Button = findViewById(R.id.profileBackButton)

        backButton.setOnClickListener{
            val intent = Intent(this@ProfileActivity, WorkoutsActivity::class.java)
            startActivity(intent)
            finish()
        }

        ArrayAdapter.createFromResource(
            this,R.array.Language,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerLanguage.adapter = adapter
        }

        switchMode.setOnCheckedChangeListener{
            _, isChecked ->
            val theme = sharedPreferences.getInt("theme",AppCompatDelegate.MODE_NIGHT_YES)
            if (isChecked){
                if (theme == AppCompatDelegate.MODE_NIGHT_YES)

                    AppCompatDelegate.MODE_NIGHT_NO
            }else{
                AppCompatDelegate.MODE_NIGHT_YES
            }
            sharedPreferences.edit().putInt("theme",theme).apply()
            AppCompatDelegate.setDefaultNightMode(theme)

        }
        showUserData()
        disableTextFiled()
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


                        if (isTrainer == true){
                            userType.setText("Entrenador")
                        }else{
                            userType.setText("Cliente")
                        }
                        userBirtyDate?.let {
                            val date = it.toDate()
                            val dateFormat = SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                            val formattedDate = dateFormat.format(date)
                            userBirtyDateFiled.setText(formattedDate)
                        }

                        userFiled.setText(userId)
                        userNameFiled.setText(userName)
                        userSurnameFiled.setText(userSurname)
                        userEmailFiled.setText(userEmail)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("showUserData", "Error getting user data: ", exception)

                }
        }
    }
    private fun disableTextFiled(){
        userFiled.isEnabled = false
        userNameFiled.isEnabled = false
        userSurnameFiled.isEnabled = false
        userEmailFiled.isEnabled = false
        userBirtyDateFiled.isEnabled = false
        userType.isEnabled = false
    }
    private fun checkLanguage(context: Context): Context{

        val config = context.resources.configuration

        return context
    }
}