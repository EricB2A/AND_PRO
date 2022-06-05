package com.example.blender

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.System.DATE_FORMAT
import android.view.View
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
    private lateinit var interestedInSpinner : Spinner
    private lateinit var genderSpinner : Spinner

    private lateinit var pseudoEditText: EditText
    private lateinit var firstNameEditText: EditText
    private lateinit var birthdateEditText: EditText

    private lateinit var birthdate: Calendar
    private lateinit var birthdayBtn: ImageButton
    private lateinit var interestedInAdapter: ArrayAdapter<String>
    private lateinit var interestedInGenders : MutableList<String>
    private lateinit var genderAdapter: ArrayAdapter<String>
    private lateinit var genders: MutableList<String>

    private lateinit var validateBtn: Button
    private lateinit var cancelBtn: Button

    private val formatter: SimpleDateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

    private fun setInterestedInDropdown(){
        interestedInGenders = resources.getStringArray(R.array.interestGenders).toMutableList()
        interestedInGenders.add(EMPTY_LIST_ENTRY_POSITION, resources.getString(R.string.genderEmpty))
        interestedInAdapter = ArrayAdapter<String>(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            interestedInGenders
        )
        interestedInSpinner.adapter = interestedInAdapter

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
    }

    private fun setGenderDropdown(){
        genders = resources.getStringArray(R.array.genders).toMutableList()
        genders.add(EMPTY_LIST_ENTRY_POSITION, resources.getString(R.string.genderEmpty))
        genderAdapter = ArrayAdapter<String>(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            genders
        )
        genderSpinner.adapter = genderAdapter

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

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        birthdayBtn = findViewById(R.id.ibBirthday)
        pseudoEditText = findViewById(R.id.edittext_pseudo)
        firstNameEditText = findViewById(R.id.edittext_firstname)
        birthdateEditText = findViewById(R.id.edittext_birthdate)
        interestedInSpinner = findViewById(R.id.spinner_interestedIn)
        genderSpinner = findViewById(R.id.spinner_gender)

        validateBtn = findViewById(R.id.btn_valid)
        cancelBtn = findViewById(R.id.btn_cancel)

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


        validateBtn.setOnClickListener {
            println("clicked de ses morts")
            val pseudo = pseudoEditText.text.toString();
            val firstname = firstNameEditText.text.toString();
            val age = birthdateEditText.text.toString();

            val interestedIn = interestedInSpinner.selectedItem.toString();
            val gender = genderSpinner.selectedItem.toString();

            /*
            var interestedInEnum
            val interestedInEnum = if interestedIn == "Homme" {
                InterestGender.Man;
            } else if interestedIn == "Femme" {
                interestedInEnum = InterestGender.WOMAN;
            } else {
                interestedInEnum = InterestGender.ANY;
            }

             */

            val interestedInEnum = when(interestedIn) {
                "Homme" -> InterestGender.MAN
                "Femme" -> InterestGender.WOMAN
                else -> InterestGender.ANY
            }

            val genderEnum = when(gender) {
                "Homme" -> Gender.MAN
                "Femme" -> Gender.WOMAN
                else -> Gender.OTHER
            }


            val profile = Profile(
                null,
                pseudo,
                firstname,
                birthdate,
                genderEnum,
                interestedInEnum,
                true
            )
            println("===")
            println(profile.toString())
            println("===")
        }

        setGenderDropdown()
        setInterestedInDropdown()
    }

    companion object {
        const val EMPTY_LIST_ENTRY_POSITION = 0
        const val DATE_PICKER = "DATE_PICKER_MODAL"
        const val DATE_FORMAT = "dd.MMM.yyyy"
    }

}