package com.example.blender.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.blender.Repository

class DiscussionViewModel(private val repository: Repository) : ViewModel() {
    val allDiscussions = repository.conversations
}

class DiscussionViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(DiscussionViewModel::class.java)) {
            return DiscussionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}