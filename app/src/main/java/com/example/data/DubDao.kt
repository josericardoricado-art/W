package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DubDao {
    @Query("SELECT * FROM dub_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<DubHistoryEntity>>

    @Query("SELECT * FROM dub_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<DubHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: DubHistoryEntity)

    @Delete
    suspend fun deleteHistory(item: DubHistoryEntity)

    @Query("UPDATE dub_history SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFav: Boolean)

    @Query("DELETE FROM dub_history")
    suspend fun clearAll()
}
