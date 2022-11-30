package web.browser.dragon.database.history

import androidx.lifecycle.LiveData
import androidx.room.*
import web.browser.dragon.model.HistoryRecord
import io.reactivex.Single

@Dao
interface HistoryRecordsDao {
    @Query("SELECT * FROM history_record_table ORDER BY dateTimestamp DESC")
    fun getAllHistoryRecords(): LiveData<List<HistoryRecord>>

    @Query("SELECT * FROM history_record_table WHERE isVisible == 1 ORDER BY dateTimestamp DESC")
    fun getVisibleHistoryRecords(): LiveData<List<HistoryRecord>>

    @Query("SELECT * FROM history_record_table ORDER BY dateTimestamp DESC")
    fun getAllHistoryRecordsSync(): Single<List<HistoryRecord>>

    @Query("SELECT * FROM history_record_table ORDER BY dateTimestamp DESC")
    fun getAllHistoryRecordsSync1(): List<HistoryRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appModel: HistoryRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSynced(appModel: HistoryRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appModels: List<HistoryRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSynced(appModels: List<HistoryRecord>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(appModel: HistoryRecord)

    @Query("DELETE FROM history_record_table")
    suspend fun deleteAll()

    @Query("DELETE FROM history_record_table WHERE dateTimestamp == :dateTimestamp")
    fun delete(dateTimestamp: Long)
}