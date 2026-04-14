package com.securekey.sample.ui.screens

import android.util.Log
import androidx.credentials.CreateCredentialRequest
import androidx.credentials.CreateCredentialResponse
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialCustomException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialInterruptedException
import androidx.credentials.exceptions.CreateCredentialProviderConfigurationException
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginNavEvent {
    data object NavigateToHome : LoginNavEvent()
}

class LoginViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<LoginNavEvent>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<LoginNavEvent> = _navigationEvent.asSharedFlow()

    fun signInWithSavedPassword(
        getCredential: suspend (GetCredentialRequest) -> GetCredentialResponse,
        onFilled: (id: String, password: String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = null
            _errorMessage.value = null
            try {
                val request = GetCredentialRequest(listOf(GetPasswordOption()))
                val response = getCredential(request)
                when (val cred = response.credential) {
                    is PasswordCredential -> {
                        onFilled(cred.id, cred.password)
                        _statusMessage.value = "Filled from saved credential"
                        _navigationEvent.emit(LoginNavEvent.NavigateToHome)
                    }
                    is CustomCredential -> {
                        _errorMessage.value = "Unsupported credential type: ${cred.type}"
                    }
                    else -> {
                        _errorMessage.value = "Unsupported credential type"
                    }
                }
            } catch (e: GetCredentialException) {
                handleGetException(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveAndProceed(
        username: String,
        password: String,
        createCredential: suspend (CreateCredentialRequest) -> CreateCredentialResponse
    ) {
        viewModelScope.launch {
            if (username.isBlank() || password.isBlank()) {
                println("@@@@ username = ${username.isBlank()} or password ${password.isBlank()}")
                _navigationEvent.emit(LoginNavEvent.NavigateToHome)
                return@launch
            }
            _isLoading.value = true
            _statusMessage.value = null
            _errorMessage.value = null
            try {
                createCredential(CreatePasswordRequest(id = username, password = password))
            } catch (e: CreateCredentialException) {
                handleCreateException(e)
            } finally {
                _isLoading.value = false
                _navigationEvent.emit(LoginNavEvent.NavigateToHome)
            }
        }
    }

    fun clearMessages() {
        _statusMessage.value = null
        _errorMessage.value = null
    }

    private fun handleGetException(e: GetCredentialException) {
        Log.e(TAG, "getCredential failed: ${e::class.simpleName}", e)
        _errorMessage.value = when (e) {
            is NoCredentialException ->
                "No saved credentials — sign in once and save first"
            is GetCredentialCancellationException -> null
            is GetCredentialInterruptedException ->
                "Interrupted — please try again"
            is GetCredentialProviderConfigurationException ->
                "Credential provider not configured"
            is GetCredentialUnknownException ->
                "Unknown credential manager error"
            is GetCredentialCustomException ->
                "Provider-specific error"
            else -> "Credential manager error: ${e.errorMessage ?: e.message}"
        }
    }

    private fun handleCreateException(e: CreateCredentialException) {
        Log.e(TAG, "createCredential failed: ${e::class.simpleName}", e)
        _errorMessage.value = when (e) {
            is CreateCredentialCancellationException -> null
            is CreateCredentialInterruptedException ->
                "Save interrupted — please try again"
            is CreateCredentialProviderConfigurationException ->
                "Credential provider not configured"
            is CreateCredentialUnknownException ->
                "Couldn't save credential"
            is CreateCredentialCustomException ->
                "Provider-specific error saving credential"
            else -> "Save failed: ${e.errorMessage ?: e.message}"
        }
    }

    companion object {
        private const val TAG = "LoginVM"
    }
}
