package com.example.recipefinder.ui.login

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)

/**
 * Maps a Firebase Auth failure to a customer-friendly message, logging the raw
 * exception so unexpected failures can still be diagnosed from Logcat.
 */
fun authErrorMessage(error: Throwable?): String? {
    if (error == null) return null
    Log.e("Auth", "Authentication failed: ${error::class.simpleName}: ${error.message}", error)
    return when (error) {
        is FirebaseAuthUserCollisionException ->
            "An account with this email already exists. Try logging in instead."
        is FirebaseAuthWeakPasswordException ->
            "Please choose a stronger password (at least 6 characters)."
        is FirebaseAuthInvalidCredentialsException ->
            "That email address doesn't look right. Please check it and try again."
        is FirebaseAuthInvalidUserException ->
            "We couldn't find an account with that email."
        is FirebaseNetworkException ->
            "Network error — please check your connection and try again."
        is IllegalArgumentException ->
            error.message ?: "Something went wrong, please try again."
        else ->
            "Something went wrong, please try again."
    }
}

class LoginViewModel : ViewModel() {
    var uiState = mutableStateOf(LoginUiState())
    private val accountService: AccountService = AccountServiceImpl()

    suspend fun authentication(): Throwable? = suspendCoroutine { cont ->
        accountService.authenticate(uiState.value.email, uiState.value.password) { error ->
            cont.resume(error)
        }
    }

    suspend fun signUp(): Throwable? = suspendCoroutine { cont ->
        accountService.signUp(
            uiState.value.email,
            uiState.value.password,
            uiState.value.confirmPassword
        ) { error ->
            cont.resume(error)
        }
    }
}