package com.example.blender.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.blender.models.Profile

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profile where mine = 1")
    fun getMyProfile(): LiveData<Profile>

    @Query("SELECT * FROM profile")
    fun getAll(): LiveData<List<Profile>>

    @Query("SELECT * FROM profile WHERE id = :id")
    fun getById(id: Int): Profile

    @Insert
    fun insert(profile: Profile)

    @Update
    fun update(profile: Profile)

    @Delete
    fun delete(profile: Profile)

}