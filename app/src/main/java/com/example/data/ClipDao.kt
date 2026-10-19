package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipDao {
    @Query("SELECT * FROM clips ORDER BY timestamp DESC")
    fun getAllClips(): Flow<List<ClipItem>>

    @Query("SELECT * FROM clips WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteClips(): Flow<List<ClipItem>>

    @Query("SELECT * FROM clips WHERE text LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchClips(query: String): Flow<List<ClipItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClip(item: ClipItem): Long

    @Update
    suspend fun updateClip(item: ClipItem)

    @Query("DELETE FROM clips WHERE id = :id")
    suspend fun deleteClipById(id: Long)

    @Query("DELETE FROM clips WHERE isFavorite = 0")
    suspend fun clearNonFavorites()

    @Query("DELETE FROM clips")
    suspend fun clearAll()
}
