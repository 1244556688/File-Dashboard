package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedbackRuleDao {
    @Query("SELECT * FROM feedback_rules ORDER BY createdTime DESC")
    fun getAllRules(): Flow<List<FeedbackRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: FeedbackRule)

    @Delete
    suspend fun deleteRule(rule: FeedbackRule)

    @Query("DELETE FROM feedback_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Int)

    @Query("DELETE FROM feedback_rules")
    suspend fun clearAll()
}
