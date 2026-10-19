package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clips")
data class ClipItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val tags: String = "",
    val sourceApp: String = "System"
)
