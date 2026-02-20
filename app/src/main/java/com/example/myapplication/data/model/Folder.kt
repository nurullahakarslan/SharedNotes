package com.example.myapplication.data.model

data class Folder(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "", // UID of the creator
    val sharedWith: List<String> = emptyList(), // List of UIDs or emails who have "Editor" role
    val createdAt: Long = System.currentTimeMillis()
)
