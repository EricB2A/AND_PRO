package com.example.blender

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.blender.models.Gender
import com.example.blender.models.InterestGender
import com.example.blender.models.Profile
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
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

    private val formatter: SimpleDateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

    private fun setInterestedInDropdown() {
        interestedInGenders = resources.getStringArray(R.array.interestGenders).toMutableList()
        /*
        interestedInGenders.add(
            EMPTY_LIST_ENTRY_POSITION,
            resources.getString(R.string.genderEmpty)
        )
         */
        interestedInAdapter = ArrayAdapter<String>(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            interestedInGenders
        )
        interestedInSpinner.adapter = interestedInAdapter

        /*
        // TODO: delete me at refactor
        // On définie les comportements du spinner lors de la sélection des élements
        interestedInSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

         */
    }

    private fun setGenderDropdown() {
        genders = resources.getStringArray(R.array.genders).toMutableList()
        //genders.add(EMPTY_LIST_ENTRY_POSITION, resources.getString(R.string.genderEmpty))
        genderAdapter = ArrayAdapter<String>(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            genders
        )
        genderSpinner.adapter = genderAdapter

        /*
        //TODO: delete me at refactor
        // On définie les comportements du spinner lors de la sélection des élements
        genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

         */
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val repository = (application as Blender).repository
        val profile =  repository.getMyProfile()
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

        // Set les fields
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

        Log.d("ProfileActivity", "profile : $profile")

        validateBtn.setOnClickListener {
            val profile = repository.getMyProfile()

            val pseudo = pseudoEditText.text.toString()
            val firstname = firstNameEditText.text.toString()
            val interestedIn = interestedInSpinner.selectedItem.toString()
            val gender = genderSpinner.selectedItem.toString()
            // FIELDS VALIDATION

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

            // TODO Guillaume: voir si on peut utiliser Converter ici
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

            val updatedProfile = Profile(
                null,
                pseudo,
                firstname,
                Calendar.getInstance(),
                genderEnum,
                interestedInEnum,
                true,
                UUID.randomUUID().toString()
            )

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
    }

}