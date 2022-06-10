package com.example.blender.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.blender.models.Gender
import com.example.blender.models.InterestGender
import com.example.blender.models.Profile
import java.util.*

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profile where mine = 1 LIMIT 1")
    fun getMyProfile(): LiveData<Profile?>

    @Query("SELECT * FROM profile")
    fun getAll(): LiveData<List<Profile>>

    @Query("SELECT * FROM profile WHERE id = :id")
    fun getById(id: Int): Profile

    @Query("SELECT * FROM profile WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUUID(uuid: UUID) : Profile?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(profile: Profile)

    @Update
    fun update(profile: Profile)

    @Query("UPDATE profile " +
            "SET pseudo = :pseudo, firstname = :firstname, birthdate = :birthday, gender = :gender, interestedIn = :interestGender " +
            "WHERE uuid = :uuid")
    fun updateByUUID(pseudo : String, firstname : String, birthday : Calendar, gender: Gender, interestGender: InterestGender, uuid: UUID)

    @Delete
    fun delete(profile: Profile)

    @Query("DELETE FROM profile")
    fun deleteAll()

}