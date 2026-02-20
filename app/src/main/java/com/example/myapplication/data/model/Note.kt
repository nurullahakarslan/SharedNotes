package com.example.myapplication.data.model

data class Note(
    val id: String = "",
    val folderId: String = "",
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val lastEditedAt: Long = System.currentTimeMillis()
)
