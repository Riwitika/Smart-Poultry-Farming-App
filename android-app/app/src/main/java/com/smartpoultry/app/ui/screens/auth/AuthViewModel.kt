package com.smartpoultry.app.ui.screens.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartpoultry.app.domain.model.User
import com.smartpoultry.app.domain.model.UserProfile
import com.smartpoultry.app.domain.repository.AuthRepository
import com.smartpoultry.app.domain.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val message: String) : AuthUiState
    data class Error(val errorMsg: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collectLatest { user ->
                _currentUser.value = user
                if (user != null) {
                    ensureUserProfileExists(user)
                }
            }
        }
    }

    private fun ensureUserProfileExists(user: User) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(user.userId)
        docRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val joinedTime = firebaseUser?.metadata?.creationTimestamp ?: System.currentTimeMillis()
                val profile = UserProfile(
                    uid = user.userId,
                    fullName = user.displayName.ifBlank { firebaseUser?.displayName ?: "Farmer" },
                    email = user.email,
                    createdAt = joinedTime
                )
                viewModelScope.launch {
                    firestoreRepository.createUserProfile(profile)
                }
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (!validateEmailAndPassword(email, password)) return

        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            authRepository.login(email.trim(), password)
                .onSuccess { user ->
                    ensureUserProfileExists(user)
                    _currentUser.value = user
                    _authState.value = AuthUiState.Success("Logged in successfully")
                    onSuccess()
                }
                .onFailure { exception ->
                    _authState.value = AuthUiState.Error(mapFirebaseError(exception))
                }
        }
    }

    fun register(email: String, password: String, passwordConfirm: String, displayName: String, onSuccess: () -> Unit) {
        if (displayName.trim().isEmpty()) {
            _authState.value = AuthUiState.Error("Full Name is required")
            return
        }
        if (!validateEmailAndPassword(email, password)) return
        if (password != passwordConfirm) {
            _authState.value = AuthUiState.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            authRepository.register(email.trim(), password, displayName.trim())
                .onSuccess { user ->
                    val profile = UserProfile(
                        uid = user.userId,
                        fullName = displayName.trim(),
                        email = user.email,
                        createdAt = System.currentTimeMillis()
                    )
                    firestoreRepository.createUserProfile(profile)
                        .onSuccess {
                            _currentUser.value = user
                            _authState.value = AuthUiState.Success("Account registered successfully")
                            onSuccess()
                        }
                        .onFailure { exception ->
                            _authState.value = AuthUiState.Error("Profile creation failed: ${exception.message}")
                        }
                }
                .onFailure { exception ->
                    _authState.value = AuthUiState.Error(mapFirebaseError(exception))
                }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        val trimmed = email.trim()
        if (trimmed.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            _authState.value = AuthUiState.Error("Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            authRepository.sendPasswordResetEmail(trimmed)
                .onSuccess {
                    _authState.value = AuthUiState.Success("Password reset email sent successfully")
                }
                .onFailure { exception ->
                    _authState.value = AuthUiState.Error(mapFirebaseError(exception))
                }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
                .onSuccess {
                    _currentUser.value = null
                    _authState.value = AuthUiState.Idle
                    onSuccess()
                }
                .onFailure { exception ->
                    _authState.value = AuthUiState.Error(exception.message ?: "Logout failed")
                }
        }
    }

    fun resetState() {
        _authState.value = AuthUiState.Idle
    }

    private fun validateEmailAndPassword(email: String, password: String): Boolean {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) {
            _authState.value = AuthUiState.Error("Email address is required")
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _authState.value = AuthUiState.Error("Invalid email format")
            return false
        }
        if (password.isEmpty()) {
            _authState.value = AuthUiState.Error("Password is required")
            return false
        }
        if (password.length < 6) {
            _authState.value = AuthUiState.Error("Password must be at least 6 characters")
            return false
        }
        return true
    }

    private fun mapFirebaseError(exception: Throwable): String {
        val msg = exception.message ?: ""
        return when {
            msg.contains("WEAK_PASSWORD", ignoreCase = true) -> "Password is too weak. Must be at least 6 characters."
            msg.contains("EMAIL_ALREADY_IN_USE", ignoreCase = true) || msg.contains("already in use", ignoreCase = true) -> "This email address is already in use by another account."
            msg.contains("INVALID_CREDENTIALS", ignoreCase = true) || msg.contains("wrong password", ignoreCase = true) || msg.contains("no user record", ignoreCase = true) -> "Invalid email or password. Please try again."
            msg.contains("network", ignoreCase = true) -> "Network connection error. Check your connection."
            else -> exception.localizedMessage ?: "An unexpected authentication error occurred."
        }
    }
}
