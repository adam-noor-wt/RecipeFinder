package com.example.recipefinder.ui.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipefinder.ui.theme.Primary
import com.example.recipefinder.ui.theme.Secondary
import com.example.recipefinder.ui.theme.authFieldColors
import kotlinx.coroutines.launch

@Composable
fun LoginModal(refresh : () -> Unit,viewModel: LoginViewModel = viewModel<LoginViewModel>()) {
    val uiState = remember{viewModel.uiState}
    val isLoading = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val signUp = remember{mutableStateOf(false)}
    val loginError = remember { mutableStateOf<String?>(null) }
    val signUpError = remember { mutableStateOf<String?>(null) }

    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }
    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue)
    }
    fun onConfirmPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(confirmPassword = newValue)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black.copy(alpha = 0.6f)),
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.85f),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            AnimatedContent(
                targetState = signUp.value,
                transitionSpec = {
                    val direction = if (targetState) 1 else -1
                    (slideInHorizontally(tween(300)) { width -> direction * width } + fadeIn(tween(300)))
                        .togetherWith(slideOutHorizontally(tween(300)) { width -> -direction * width } + fadeOut(tween(300)))
                },
                label = "login_signup_transition",
            ) { isSignUp ->
                if (!isSignUp) {
                    LoginForm(
                        uiState = uiState.value,
                        isLoading = isLoading.value,
                        errorMessage = loginError.value,
                        onEmailChange = ::onEmailChange,
                        onPasswordChange = ::onPasswordChange,
                        onSubmit = {
                            coroutineScope.launch {
                                isLoading.value = true
                                val error = viewModel.authentication()
                                isLoading.value = false
                                loginError.value = authErrorMessage(error)
                                if (error == null) {
                                    refresh()
                                }
                            }
                        },
                        onSwitchToSignUp = {
                            loginError.value = null
                            signUp.value = true
                        },
                    )
                } else {
                    SignUpForm(
                        uiState = uiState.value,
                        isLoading = isLoading.value,
                        errorMessage = signUpError.value,
                        onEmailChange = ::onEmailChange,
                        onPasswordChange = ::onPasswordChange,
                        onConfirmPasswordChange = ::onConfirmPasswordChange,
                        onSubmit = {
                            coroutineScope.launch {
                                isLoading.value = true
                                signUpError.value = null
                                val signUpFailure = viewModel.signUp()
                                if (signUpFailure == null) {
                                    val loginFailure = viewModel.authentication()
                                    isLoading.value = false
                                    if (loginFailure == null) {
                                        refresh()
                                    } else {
                                        signUpError.value = authErrorMessage(loginFailure)
                                    }
                                } else {
                                    isLoading.value = false
                                    signUpError.value = authErrorMessage(signUpFailure)
                                }
                            }
                        },
                        onSwitchToLogin = {
                            signUpError.value = null
                            signUp.value = false
                        },
                    )
                }
            }
        }
    }
}

private fun isValidEmail(email: String) = email.isNotBlank() && email.contains("@")

@Composable
private fun AuthFormHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = Color.White,
        style = MaterialTheme.typography.headlineMedium,
    )
}

@Composable
private fun AuthErrorMessage(message: String?) {
    AnimatedVisibility(visible = message != null) {
        Text(
            text = message.orEmpty(),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AuthSubmitButton(text: String, isLoading: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(containerColor = Secondary, contentColor = Color.White),
        onClick = onClick,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .size(20.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Text(text, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun LoginForm(
    uiState: LoginUiState,
    isLoading: Boolean,
    errorMessage: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSwitchToSignUp: () -> Unit,
) {
    val isFormValid = isValidEmail(uiState.email) && uiState.password.isNotBlank()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AuthFormHeader("Welcome back")
        EmailField(uiState.email, onEmailChange)
        PasswordField(uiState.password, onPasswordChange, label = "Password")
        AuthSubmitButton("Log In", isLoading, isFormValid, onSubmit)
        AuthErrorMessage(errorMessage)
        TextButton(onClick = onSwitchToSignUp) {
            Text("Don't have an account? Sign up", color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SignUpForm(
    uiState: LoginUiState,
    isLoading: Boolean,
    errorMessage: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSwitchToLogin: () -> Unit,
) {
    val isFormValid = isValidEmail(uiState.email) &&
        uiState.password.isNotBlank() &&
        uiState.confirmPassword == uiState.password
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AuthFormHeader("Create your account")
        EmailField(uiState.email, onEmailChange)
        PasswordField(uiState.password, onPasswordChange, label = "Password")
        PasswordField(uiState.confirmPassword, onConfirmPasswordChange, label = "Confirm password")
        AuthSubmitButton("Sign Up", isLoading, isFormValid, onSubmit)
        AuthErrorMessage(errorMessage)
        TextButton(onClick = onSwitchToLogin) {
            Text("Already have an account? Log in", color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun EmailField(value: String, onNewValue: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onNewValue,
        label = { Text("Email") },
        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
        colors = authFieldColors(),
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
fun PasswordField(value: String, onNewValue: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    var passwordVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onNewValue,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        colors = authFieldColors(),
        shape = MaterialTheme.shapes.medium,
    )
}
