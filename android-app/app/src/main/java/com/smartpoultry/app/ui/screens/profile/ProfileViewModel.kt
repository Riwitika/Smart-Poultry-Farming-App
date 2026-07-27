package com.smartpoultry.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartpoultry.app.domain.model.PredictionRecord
import com.smartpoultry.app.domain.model.UserProfile
import com.smartpoultry.app.domain.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val predictions: List<PredictionRecord> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "User session expired"
            )
            return
        }

        viewModelScope.launch {
            // Stream profile details
            launch {
                firestoreRepository.getUserProfile(uid).collectLatest { profile ->
                    _uiState.value = _uiState.value.copy(
                        userProfile = profile,
                        isLoading = false
                    )
                }
            }

            // Stream predictions list for stats alignment
            launch {
                firestoreRepository.getPredictions(uid).collectLatest { list ->
                    _uiState.value = _uiState.value.copy(
                        predictions = list,
                        isLoading = false
                    )
                }
            }
        }
    }
}
