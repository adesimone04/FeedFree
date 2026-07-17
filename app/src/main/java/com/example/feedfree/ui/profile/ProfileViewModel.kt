package com.example.feedfree.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feedfree.data.MockRepository
import com.example.feedfree.models.CustomActivity
import com.example.feedfree.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<User?>(null)
    val uiState: StateFlow<User?> = _uiState.asStateFlow()

    // 1. STATO GLOBALE DELLE ATTIVITÀ (Condiviso tra Home e Badges)
    private val _activities = MutableStateFlow<List<CustomActivity>>(emptyList())
    val activities: StateFlow<List<CustomActivity>> = _activities.asStateFlow()

    // 2. STATO PER LA NAVIGAZIONE DIRETTA AL TROFEO
    private val _selectedActivityForDetails = MutableStateFlow<CustomActivity?>(null)
    val selectedActivityForDetails: StateFlow<CustomActivity?> = _selectedActivityForDetails.asStateFlow()

    init {
        loadUserData()
        loadActivities()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = MockRepository.getCurrentUser()
        }
    }

    private fun loadActivities() {
        viewModelScope.launch {
            _activities.value = MockRepository.getCustomActivities()
        }
    }

    // Funzione per aggiornare un'attività (es. completarla dalla Home)
    // Funzione per aggiornare un'attività (es. completarla dalla Home) o crearne una nuova
    fun updateActivity(updatedActivity: CustomActivity) {
        // 1. Aggiorna o aggiunge l'attività alla lista globale
        _activities.update { currentList ->
            val index = currentList.indexOfFirst { it.id == updatedActivity.id }
            if (index != -1) {
                // Modifica: L'attività esiste, la sostituiamo
                val mutableList = currentList.toMutableList()
                mutableList[index] = updatedActivity
                mutableList
            } else {
                // Creazione: L'attività non esiste, la aggiungiamo in coda
                currentList + updatedActivity
            }
        }

        // 2. Sincronizza la vista di dettaglio (Fondamentale per far funzionare le spunte!)
        if (_selectedActivityForDetails.value?.id == updatedActivity.id) {
            _selectedActivityForDetails.value = updatedActivity
        }
    }

    // Funzione per selezionare o deselezionare un trofeo da visualizzare
    fun selectActivityForDetails(activity: CustomActivity?) {
        _selectedActivityForDetails.value = activity
    }
}