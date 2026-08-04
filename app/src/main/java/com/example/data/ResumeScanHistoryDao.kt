package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ResumeScanHistoryDao {

    @Query("SELECT * FROM resume_scan_history ORDER BY scanDate DESC")
    fun getAllScans(): Flow<List<ResumeScanHistoryEntity>>

    @Query("SELECT * FROM resume_scan_history WHERE bookmarked = 1 ORDER BY scanDate DESC")
    fun getStarredScans(): Flow<List<ResumeScanHistoryEntity>>

    @Query("SELECT * FROM resume_scan_history ORDER BY matchScore DESC")
    fun getScansSortedByScore(): Flow<List<ResumeScanHistoryEntity>>

    @Query("SELECT * FROM resume_scan_history WHERE id = :scanId LIMIT 1")
    suspend fun getScanById(scanId: String): ResumeScanHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ResumeScanHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllScans(scans: List<ResumeScanHistoryEntity>)

    @Update
    suspend fun updateScan(scan: ResumeScanHistoryEntity)

    @Query("UPDATE resume_scan_history SET bookmarked = :bookmarked WHERE id = :scanId")
    suspend fun updateBookmark(scanId: String, bookmarked: Boolean)

    @Delete
    suspend fun deleteScan(scan: ResumeScanHistoryEntity)

    @Query("DELETE FROM resume_scan_history WHERE id = :scanId")
    suspend fun deleteScanById(scanId: String)

    @Query("DELETE FROM resume_scan_history")
    suspend fun clearAllScans()

    @Query("SELECT COUNT(*) FROM resume_scan_history")
    suspend fun getTotalScanCount(): Int

    @Query("SELECT COUNT(DISTINCT resumeId) FROM resume_scan_history")
    suspend fun getUniqueResumeCount(): Int

    @Query("SELECT MAX(matchScore) FROM resume_scan_history WHERE matchScore IS NOT NULL")
    suspend fun getHighestScore(): Int?

    @Query("SELECT COUNT(*) FROM resume_scan_history WHERE matchScore >= 80")
    suspend fun getHighScoringCount(): Int
}
