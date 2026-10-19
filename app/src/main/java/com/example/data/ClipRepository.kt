package com.example.data

import kotlinx.coroutines.flow.Flow

class ClipRepository(private val clipDao: ClipDao) {
    val allClips: Flow<List<ClipItem>> = clipDao.getAllClips()
    val favoriteClips: Flow<List<ClipItem>> = clipDao.getFavoriteClips()

    fun searchClips(query: String): Flow<List<ClipItem>> = clipDao.searchClips(query)

    suspend fun insert(item: ClipItem) {
        clipDao.insertClip(item)
    }

    suspend fun update(item: ClipItem) {
        clipDao.updateClip(item)
    }

    suspend fun deleteById(id: Long) {
        clipDao.deleteClipById(id)
    }

    suspend fun clearNonFavorites() {
        clipDao.clearNonFavorites()
    }

    suspend fun clearAll() {
        clipDao.clearAll()
    }
}
