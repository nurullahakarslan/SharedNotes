package com.example.myapplication.ui.screens.notes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

class NoteDetailViewModel : ViewModel() {
    private val firestoreRepository = FirestoreRepository()
    private val authRepository = AuthRepository()

    var title by mutableStateOf("")
    var content by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var isSaving by mutableStateOf(false)

    fun loadNote(folderId: String, noteId: String) {
        if (noteId == "new") return
        isLoading = true
        viewModelScope.launch {
            val notes = firestoreRepository.getNotesForFolder(folderId)
            val note = notes.find { it.id == noteId }
            if (note != null) {
                title = note.title
                content = note.content
            }
            isLoading = false
        }
    }

    suspend fun saveNote(folderId: String, noteId: String): Boolean {
        isSaving = true
        val userId = authRepository.getCurrentUserId() ?: return false
        val success = if (noteId == "new") {
            firestoreRepository.addNoteToFolder(folderId, title, content, userId)
        } else {
            firestoreRepository.updateNote(folderId, noteId, title, content)
        }
        isSaving = false
        return success
    }

    suspend fun deleteNote(folderId: String, noteId: String): Boolean {
        if (noteId == "new") return true
        isSaving = true
        val success = firestoreRepository.deleteNote(folderId, noteId)
        isSaving = false
        return success
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    folderId: String,
    noteId: String,
    onBackClick: () -> Unit,
    viewModel: NoteDetailViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(noteId) {
        viewModel.loadNote(folderId, noteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == "new") "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (noteId != "new") {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                if (viewModel.deleteNote(folderId, noteId)) {
                                    onBackClick()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    IconButton(onClick = {
                        coroutineScope.launch {
                            if (viewModel.saveNote(folderId, noteId)) {
                                onBackClick()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Done, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.title,
                    onValueChange = { viewModel.title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = viewModel.content,
                    onValueChange = { viewModel.content = it },
                    label = { Text("Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }
    }
}
