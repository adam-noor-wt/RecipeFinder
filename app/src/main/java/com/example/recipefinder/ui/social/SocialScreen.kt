package com.example.recipefinder.ui.social

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.recipefinder.data.PublicProfile
import com.example.recipefinder.ui.theme.Primary
import com.example.recipefinder.ui.theme.Typography

/** A followed user. [profile] is null / not public when the user is private (shown as "Private user"). */
data class FollowedUser(val uid: String, val profile: PublicProfile?, val unviewedCount: Int)

fun PublicProfile?.visibleName(): String = when {
    this == null || !isPublic -> "Private user"
    displayName.isBlank() -> "Anonymous"
    else -> displayName
}

@Composable
fun SocialScreen(
    followed: List<FollowedUser>,
    /** Runs a name/uid search over public profiles, delivering results to the callback. */
    onSearch: (String, (List<PublicProfile>) -> Unit) -> Unit,
    onUserClick: (String) -> Unit,
    onUnfollow: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<PublicProfile>?>(null) }
    val doSearch = {
        if (query.isBlank()) results = null
        else onSearch(query) { results = it }
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Social", style = Typography.headlineMedium, color = Primary)
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                if (it.isBlank()) results = null
            },
            label = { Text("Search users by name or ID") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { doSearch() }),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        val found = results
        if (found != null) {
            Text("Search results", style = Typography.titleMedium, color = Primary)
            if (found.isEmpty()) Text("No users found", modifier = Modifier.padding(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(found, key = { it.uid }) { profile ->
                    UserRow(profile.visibleName(), 0, true) { onUserClick(profile.uid) }
                }
            }
        } else {
            Text("Following", style = Typography.titleMedium, color = Primary)
            if (followed.isEmpty()) Text("You aren't following anyone yet", modifier = Modifier.padding(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(followed, key = { it.uid }) { user ->
                    val canOpen = user.profile?.isPublic == true
                    UserRow(
                        name = user.profile.visibleName(),
                        unviewed = user.unviewedCount,
                        clickable = canOpen,
                        onUnfollow = { onUnfollow(user.uid) },
                        onClick = { onUserClick(user.uid) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UserRow(
    name: String,
    unviewed: Int,
    clickable: Boolean,
    onUnfollow: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = clickable, onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(Icons.Filled.AccountCircle, contentDescription = "Profile icon", tint = Primary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(12.dp))
            Text(name, style = Typography.titleMedium)
            if (unviewed > 0) {
                Spacer(Modifier.width(8.dp))
                Badge { Text("$unviewed new") }
            }
        }
        if (onUnfollow != null) OutlinedButton(onClick = onUnfollow) { Text("Unfollow") }
    }
}
