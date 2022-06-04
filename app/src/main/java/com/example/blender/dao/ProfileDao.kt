package com.example.blender.dao

import androidx.room.*
import com.example.blender.models.Profile

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile")
    fun getAll(): List<Profile>

    @Query("SELECT * FROM profile WHERE id = :id")
    fun getById(id: Int): Profile

    @Insert
    fun insert(profile: Profile)

    @Update
    fun update(profile: Profile)

    @Delete
    fun delete(profile: Profile)

}