package com.example.recipefinder.ui.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.recipefinder.ui.login.AccountServiceImpl
import com.example.recipefinder.ui.theme.RecipeFinderTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun SettingsScreen(
    signOut : () -> Unit,
)
{
    Column (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Display Name: ${Firebase.auth.currentUser?.displayName ?: "Not set"}")
            Button(
                onClick = { AccountServiceImpl().editDisplayName("Hey") { print("a") } },
            ) {
                Text("Edit")
            }
        }
        Button(onClick = { signOut() }) {
            Text("Sign Out")
        }
    }

}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    RecipeFinderTheme {
        SettingsScreen( signOut = { print("A") } )
    }
}