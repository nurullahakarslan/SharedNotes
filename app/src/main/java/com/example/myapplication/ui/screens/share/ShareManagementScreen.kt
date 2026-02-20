package com.example.myapplication.ui.screens.share

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Folder
import com.example.myapplication.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

class ShareViewModel : ViewModel() {
    private val firestoreRepository = FirestoreRepository()

    var folder by mutableStateOf<Folder?>(null)
    var emailInput by mutableStateOf("")
    var message by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    fun loadFolder(folderId: String) {
        isLoading = true
        viewModelScope.launch {
            folder = firestoreRepository.getFolderById(folderId)
            isLoading = false
        }
    }

    suspend fun shareFolder(folderId: String): Boolean {
        if (emailInput.isBlank()) {
            message = "Please enter an email"
            return false
        }
        isLoading = true
        message = null
        val userId = firestoreRepository.findUserByEmail(emailInput.trim())
        if (userId == null) {
            message = "User not found"
            isLoading = false
            return false
        }

        if (folder?.sharedWith?.contains(userId) == true) {
            message = "User already has access"
            isLoading = false
            return false
        }

        val success = firestoreRepository.shareFolder(folderId, userId)
        if (success) {
            message = "Folder shared successfully!"
            emailInput = ""
            loadFolder(folderId)
        } else {
            message = "Failed to share folder"
        }
        isLoading = false
        return success
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareManagementScreen(
    folderId: String,
    folderName: String,
    onBackClick: () -> Unit,
    viewModel: ShareViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(folderId) {
        viewModel.loadFolder(folderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share: $folderName") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (viewModel.isLoading && viewModel.folder == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(text = "Add Collaborator", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = viewModel.emailInput,
                            onValueChange = { viewModel.emailInput = it },
                            label = { Text("User Email") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            coroutineScope.launch {
                                viewModel.shareFolder(folderId)
                            }
                        }) {
                            Text("Share")
                        }
                    }

                    if (viewModel.message != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.message!!,
                            color = if (viewModel.message!!.contains("success")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Shared With", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    val sharedCount = viewModel.folder?.sharedWith?.size ?: 0
                    if (sharedCount == 0) {
                        Text(
                            "Not shared with anyone yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(viewModel.folder?.sharedWith ?: emptyList()) { userId ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null)
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text("User ID: $userId")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
