package com.example.myapplication.ui.screens.folders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Folder
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

class FolderViewModel : ViewModel() {
    private val firestoreRepository = FirestoreRepository()
    private val authRepository = AuthRepository()

    var folders by mutableStateOf<List<Folder>>(emptyList())
    var isLoading by mutableStateOf(true)
    var newFolderName by mutableStateOf("")

    init {
        loadFolders()
    }

    fun loadFolders() {
        isLoading = true
        viewModelScope.launch {
            authRepository.getCurrentUserId()?.let { userId ->
                folders = firestoreRepository.getFoldersForUser(userId)
            }
            isLoading = false
        }
    }

    fun createFolder() {
        if (newFolderName.isNotBlank()) {
            viewModelScope.launch {
                authRepository.getCurrentUserId()?.let { userId ->
                    val success = firestoreRepository.createFolder(newFolderName, userId)
                    if (success) {
                        newFolderName = ""
                        loadFolders() // reload
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListScreen(
    onFolderClick: (String, String) -> Unit,
    onShareClick: (String, String) -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: FolderViewModel = viewModel()
) {
    val authRepo = remember { AuthRepository() }
    val isOwner = { folder: Folder -> folder.ownerId == authRepo.getCurrentUserId() }
    
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("New Folder") },
            text = {
                OutlinedTextField(
                    value = viewModel.newFolderName,
                    onValueChange = { viewModel.newFolderName = it },
                    label = { Text("Folder Name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        viewModel.createFolder()
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Folders") },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Folder")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.folders.isEmpty()) {
                Text(
                    "No folders found. Create one!",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.folders) { folder ->
                        FolderItem(
                            folder = folder,
                            isOwner = isOwner(folder),
                            onClick = { onFolderClick(folder.id, folder.name) },
                            onShareClick = { onShareClick(folder.id, folder.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FolderItem(
    folder: Folder,
    isOwner: Boolean,
    onClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = folder.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (isOwner) "Owner" else "Shared with you",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isOwner) {
                IconButton(onClick = onShareClick) {
                    Icon(Icons.Default.Share, contentDescription = "Share Folder")
                }
            }
        }
    }
}
