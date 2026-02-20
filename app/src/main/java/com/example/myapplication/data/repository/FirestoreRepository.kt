package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Folder
import com.example.myapplication.data.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getFoldersForUser(userId: String): List<Folder> {
        return try {
            val ownedFolders = firestore.collection("folders")
                .whereEqualTo("ownerId", userId)
                .get().await()

            val sharedFolders = firestore.collection("folders")
                .whereArrayContains("sharedWith", userId)
                .get().await()

            val combined = ownedFolders.documents.mapNotNull { it.toObject(Folder::class.java) } +
                    sharedFolders.documents.mapNotNull { it.toObject(Folder::class.java) }

            combined.distinctBy { it.id }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createFolder(name: String, ownerId: String): Boolean {
        return try {
            val id = UUID.randomUUID().toString()
            val folder = Folder(id = id, name = name, ownerId = ownerId)
            firestore.collection("folders").document(id).set(folder).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun shareFolder(folderId: String, editorUserId: String): Boolean {
        return try {
            val folderRef = firestore.collection("folders").document(folderId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(folderRef)
                val folder = snapshot.toObject(Folder::class.java)
                if (folder != null && !folder.sharedWith.contains(editorUserId)) {
                    val newSharedWith = folder.sharedWith.toMutableList().apply { add(editorUserId) }
                    transaction.update(folderRef, "sharedWith", newSharedWith)
                }
            }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getFolderById(folderId: String): Folder? {
        return try {
            val doc = firestore.collection("folders").document(folderId).get().await()
            doc.toObject(Folder::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getNotesForFolder(folderId: String): List<Note> {
        return try {
            val result = firestore.collection("folders").document(folderId)
                .collection("notes")
                .get().await()
            result.documents.mapNotNull { it.toObject(Note::class.java) }
                .sortedByDescending { it.lastEditedAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addNoteToFolder(folderId: String, title: String, content: String, authorId: String): Boolean {
        return try {
            val id = UUID.randomUUID().toString()
            val note = Note(id = id, folderId = folderId, title = title, content = content, authorId = authorId)
            firestore.collection("folders").document(folderId)
                .collection("notes").document(id)
                .set(note).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateNote(folderId: String, noteId: String, title: String, content: String): Boolean {
        return try {
            val updates = mapOf(
                "title" to title,
                "content" to content,
                "lastEditedAt" to System.currentTimeMillis()
            )
            firestore.collection("folders").document(folderId)
                .collection("notes").document(noteId)
                .update(updates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteNote(folderId: String, noteId: String): Boolean {
        return try {
            firestore.collection("folders").document(folderId)
                .collection("notes").document(noteId)
                .delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun findUserByEmail(email: String): String? {
        return try {
            val docs = firestore.collection("users").whereEqualTo("email", email).get().await()
            if (!docs.isEmpty) {
                docs.documents[0].getString("uid")
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getUserEmailById(userId: String): String {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.getString("email") ?: "Unknown User"
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown User"
        }
    }
}
