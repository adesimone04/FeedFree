package com.example.feedfree.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feedfree.data.MockRepository
import com.example.feedfree.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    // Stato reattivo interno (privato, modificabile solo da questo ViewModel)
    private val _uiState = MutableStateFlow<User?>(null)

    // Stato reattivo pubblico (immutabile, la UI può solo "osservarlo")
    val uiState: StateFlow<User?> = _uiState.asStateFlow()

    init {
        // Appena il ViewModel viene creato, avvia il caricamento dei dati
        loadUserData()
    }

    private fun loadUserData() {
        // viewModelScope esegue la coroutine in background.
        // È perfetto per attendere gli 800ms della funzione suspend senza bloccare l'app.
        viewModelScope.launch {
            // Essendo un 'object', chiamiamo direttamente MockRepository.getUser()
            val user = MockRepository.getCurrentUser()

            // Il codice qui si ferma in attesa del delay di MockRepository,
            // per poi aggiornare lo stato con i dati ricevuti.
            _uiState.value = user
        }
    }
}