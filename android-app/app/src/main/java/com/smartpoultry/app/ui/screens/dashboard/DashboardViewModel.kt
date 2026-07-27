package com.smartpoultry.app.ui.screens.dashboard

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

data class DashboardUiState(
    val userProfile: UserProfile? = null,
    val predictions: List<PredictionRecord> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "User session not found"
            )
            return
        }

        viewModelScope.launch {
            // Stream User Profile details
            launch {
                firestoreRepository.getUserProfile(uid).collectLatest { profile ->
                    _uiState.value = _uiState.value.copy(
                        userProfile = profile,
                        isLoading = false
                    )
                }
            }

            // Stream User Predictions in real-time
            launch {
                firestoreRepository.getPredictions(uid).collectLatest { predictionRecords ->
                    _uiState.value = _uiState.value.copy(
                        predictions = predictionRecords,
                        isLoading = false
                    )
                }
            }
        }
    }
}
