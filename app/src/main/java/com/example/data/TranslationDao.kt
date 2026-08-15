package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {
    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
    fun getAllTranslations(): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM translation_history WHERE isSaved = 1 ORDER BY timestamp DESC")
    fun getSavedTranslations(): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM translation_history WHERE category = :category ORDER BY timestamp DESC")
    fun getTranslationsByCategory(category: String): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM translation_history WHERE originalText LIKE '%' || :query || '%' OR translatedText LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchTranslations(query: String): Flow<List<TranslationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(item: TranslationEntity): Long

    @Query("UPDATE translation_history SET isSaved = :isSaved WHERE id = :id")
    suspend fun updateSavedState(id: Long, isSaved: Boolean)

    @Query("DELETE FROM translation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM translation_history")
    suspend fun clearAll()
}
