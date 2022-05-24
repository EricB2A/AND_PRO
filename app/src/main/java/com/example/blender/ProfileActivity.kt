package com.example.blender

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

class ProfileActivity : AppCompatActivity() {
    // Components
    private lateinit var interestedInSpinner : Spinner
    private lateinit var genderSpinner : Spinner

    private lateinit var interestedInAdapter: ArrayAdapter<String>
    private lateinit var interestedInGenders : MutableList<String>
    private lateinit var genderAdapter: ArrayAdapter<String>
    private lateinit var genders: MutableList<String>

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

        interestedInSpinner = findViewById(R.id.spinner_interestedIn)
        genderSpinner = findViewById(R.id.spinner_gender)

        setGenderDropdown()
        setInterestedInDropdown()
    }

    companion object {
        const val EMPTY_LIST_ENTRY_POSITION = 0
    }

}