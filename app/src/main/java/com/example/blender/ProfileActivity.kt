package com.example.blender

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.blender.models.Gender
import com.example.blender.models.InterestGender
import com.example.blender.models.Profile
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var datePicker: MaterialDatePicker<Long>

    // Components
    private lateinit var interestedInSpinner: Spinner
    private lateinit var genderSpinner: Spinner

    private lateinit var pseudoEditText: EditText
    private lateinit var firstNameEditText: EditText
    private lateinit var birthdateEditText: EditText

    private lateinit var birthdate: Calendar
    private lateinit var birthdayBtn: ImageButton
    private lateinit var interestedInAdapter: ArrayAdapter<String>
    private lateinit var interestedInGenders: MutableList<String>
    private lateinit var genderAdapter: ArrayAdapter<String>
    private lateinit var genders: MutableList<String>

    private lateinit var validateBtn: Button
    private lateinit var imageBtn: ImageButton

    private var profileImage: Bitmap? = null

    private val formatter: SimpleDateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Log.d("PICK IMAGE", "Success")
            Log.d("PICK IMAGE", data.toString())
            Log.d("PICK IMAGE", data!!.data.toString())
            val input = applicationContext.contentResolver.openInputStream(data.data!!)
            val bmp = BitmapFactory.decodeStream(input)
            imageBtn.setImageBitmap(bmp)
            profileImage = bmp
        }
    }

    private fun setInterestedInDropdown() {
        interestedInGenders = resources.getStringArray(R.array.interestGenders).toMutableList()
        interestedInAdapter = ArrayAdapter<String>(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            interestedInGenders
        )
        interestedInSpinner.adapter = interestedInAdapter
    }

    private fun setGenderDropdown() {
        genders = resources.getStringArray(R.array.genders).toMutableList()
        genderAdapter = ArrayAdapter<String>(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            genders
        )
        genderSpinner.adapter = genderAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val repository = (application as Blender).repository
        val profile =  repository.getMyProfile()

        // Si aucun utilisateur n'est présent dans la DB, on en créé un nouveau
        profile.observe(this){ p ->
            if (p == null) {
                val newProfile = Profile(
                    null,
                    "",
                    "",
                    Calendar.getInstance(),
                    Gender.MAN,
                    InterestGender.ANY,
                    true,
                    UUID.randomUUID().toString()
                )
                repository.insertProfile(newProfile)
            }

        }

        birthdayBtn = findViewById(R.id.ibBirthday)
        pseudoEditText = findViewById(R.id.edittext_pseudo)
        firstNameEditText = findViewById(R.id.edittext_firstname)
        birthdateEditText = findViewById(R.id.edittext_birthdate)
        interestedInSpinner = findViewById(R.id.spinner_interestedIn)
        genderSpinner = findViewById(R.id.spinner_gender)
        imageBtn = findViewById(R.id.ibSelfie)

        // Set les fields avec les données provenant de la DB
        profile.observe(this){ p ->

            if(p == null) {
                return@observe
            }

                pseudoEditText.setText(p.pseudo)
                firstNameEditText.setText(p.firstname)

                val nGender = when (p.gender) {
                    Gender.MAN -> 0
                    Gender.WOMAN -> 1
                }
                genderSpinner.setSelection(nGender)

                val nInterestedIn = when(p.interestedIn) {
                    InterestGender.MAN -> 0
                    InterestGender.WOMAN -> 1
                    InterestGender.ANY -> 2
                }
                interestedInSpinner.setSelection(nInterestedIn)

                birthdateEditText.setText(formatter.format(p.birthdate.time))
            if (p.image != null) {
                val bmp = BitmapFactory.decodeByteArray(p.image,0, p.image!!.size)
                imageBtn.setImageBitmap(bmp)
            }

        }

        validateBtn = findViewById(R.id.btn_valid)

        val datePickerConstraint =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now()).build()

        // Création du date picker avec les contraintes
        datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("age")
            .setCalendarConstraints(datePickerConstraint)
            .build()
        // On affiche le datepicker au clic sur le bouton
        birthdayBtn.setOnClickListener {
            datePicker.show(supportFragmentManager, DATE_PICKER)
        }

        // Lors du choix de la date, on la "sauvegarde" et on affiche la string
        // correspondante dans le EditText correspondant
        datePicker.addOnPositiveButtonClickListener { instant ->
            birthdate = Calendar.Builder().setInstant(instant).build()
            birthdateEditText.setText(formatter.format(birthdate.time))
        }

        // Gestion de l'image de profil
        imageBtn.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
        }

        Log.d("ProfileActivity", "profile : $profile")

        validateBtn.setOnClickListener {
            val profile = repository.getMyProfile()

            val pseudo = pseudoEditText.text.toString()
            val firstname = firstNameEditText.text.toString()
            val interestedIn = interestedInSpinner.selectedItem.toString()
            val gender = genderSpinner.selectedItem.toString()

            // Validations simples des champs
            if(pseudo.isEmpty()) {
                pseudoEditText.error = "Ne peut être vide"
                return@setOnClickListener
            }else if(!pseudo.matches("[A-Za-z0-9._]*".toRegex())){
                pseudoEditText.error = "Ne peut contenir de caractères spéciaux, sauf . ou _"
                return@setOnClickListener
            }

            if(firstname.isEmpty()){
                firstNameEditText.error = "Ne peut être vide"
                return@setOnClickListener
            } else if(!firstname.matches("[A-Za-z]*".toRegex())){
                firstNameEditText.error = "Prénom ne peut contenir que des lettres"
                return@setOnClickListener
            }

            // Conversion des valeurs String en Enum..
            val interestedInEnum = when (interestedIn) {
                "Homme" -> InterestGender.MAN
                "Femme" -> InterestGender.WOMAN
                else -> InterestGender.ANY
            }

            val genderEnum = when (gender) {
                "Homme" -> Gender.MAN
                "Femme" -> Gender.WOMAN
                else -> Gender.MAN
            }

            // Redimensionnement et compression de l'image pour son stockage
            var bArray: ByteArray? = null
            if (profileImage != null) {
                val bos = ByteArrayOutputStream()
                profileImage = Bitmap.createScaledBitmap(profileImage!!, 512, 512, false)
                profileImage!!.compress(Bitmap.CompressFormat.PNG, 100, bos)
                bArray = bos.toByteArray()
            }

           // Mise à jour de l'instance du modèle avec les infos. du formulaire
            val updatedProfile = Profile(
                null,
                pseudo,
                firstname,
                Calendar.getInstance(),
                genderEnum,
                interestedInEnum,
                true,
                "12345",
                bArray

            )

            // Et update du profil dans la DB
            profile.observe(this) {
                if (it == null) {
                    return@observe
                }
                updatedProfile.id = it.id
                updatedProfile.uuid = it.uuid
                repository.updateProfile(updatedProfile)

            }

            finish()
        }

        setGenderDropdown()
        setInterestedInDropdown()
    }


    companion object {
        const val DATE_PICKER = "DATE_PICKER_MODAL"
        const val DATE_FORMAT = "dd.MMM.yyyy"
        const val PICK_IMAGE = 1
    }

}