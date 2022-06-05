package com.example.blender.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.blender.Repository
import com.example.blender.models.ConversationMessage

class ConversationViewModel(private val repository: Repository) : ViewModel() {
      fun getConversationMessage( id : Long) : LiveData<ConversationMessage> {
              return repository.getConversationMessage(id)
      }

}

class ConversationViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if(modelClass.isAssignableFrom(ConversationViewModel::class.java)) {
                        return ConversationViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
        }
}