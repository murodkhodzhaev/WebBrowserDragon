package web.browser.dragon.database.downloads

import androidx.lifecycle.LiveData
import androidx.room.*
import web.browser.dragon.model.DownloadModel
import io.reactivex.Single

@Dao
interface DownloadsDao {
    @Query("SELECT * FROM downloads_table ORDER BY dateTimestamp DESC")
    fun getAllDownloads(): LiveData<List<DownloadModel>>

    @Query("SELECT * FROM downloads_table ORDER BY dateTimestamp DESC")
    fun getAllDownloadsSync(): Single<List<DownloadModel>>

    @Query("SELECT * FROM downloads_table ORDER BY dateTimestamp DESC")
    fun getAllDownloadsSync1(): List<DownloadModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appModel: DownloadModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSynced(appModel: DownloadModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appModels: List<DownloadModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSynced(appModels: List<DownloadModel>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(appModel: DownloadModel)

    @Query("DELETE FROM downloads_table")
    suspend fun deleteAll()

    @Query("DELETE FROM downloads_table WHERE dateTimestamp == :dateTimestamp")
    fun delete(dateTimestamp: Long)
}