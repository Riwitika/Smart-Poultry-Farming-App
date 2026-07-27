package com.smartpoultry.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartpoultry.app.domain.model.PredictionRecord
import com.smartpoultry.app.domain.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val predictions: List<PredictionRecord> = emptyList(),
    val searchQuery: String = "",
    val filterDisease: String = "All",
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filterDisease = MutableStateFlow("All")
    val filterDisease: StateFlow<String> = _filterDisease

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val rawPredictionsFlow = flowOf(FirebaseAuth.getInstance().currentUser?.uid)
        .flatMapLatest { uid ->
            if (uid != null) {
                firestoreRepository.getPredictions(uid)
            } else {
                flowOf(emptyList())
            }
        }

    val uiState: StateFlow<HistoryUiState> = combine(
        rawPredictionsFlow,
        _searchQuery,
        _filterDisease,
        _error,
        _isLoading
    ) { list, search, filter, err, loading ->
        val filtered = list.filter { record ->
            // Filter by search query (checks disease name)
            val matchesSearch = record.diseaseName.contains(search, ignoreCase = true)
            // Filter by disease class selector
            val matchesFilter = if (filter == "All") {
                true
            } else if (filter == "Healthy") {
                record.diseaseName.lowercase().contains("healthy")
            } else {
                record.diseaseName.lowercase().contains(filter.lowercase()) && !record.diseaseName.lowercase().contains("healthy")
            }
            matchesSearch && matchesFilter
        }
        HistoryUiState(
            predictions = filtered,
            searchQuery = search,
            filterDisease = filter,
            isLoading = false,
            error = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterDisease(filter: String) {
        _filterDisease.value = filter
    }

    fun deletePrediction(predictionId: String) {
        viewModelScope.launch {
            firestoreRepository.deletePrediction(predictionId)
                .onFailure { exception ->
                    _error.value = "Failed to delete prediction: ${exception.localizedMessage}"
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
