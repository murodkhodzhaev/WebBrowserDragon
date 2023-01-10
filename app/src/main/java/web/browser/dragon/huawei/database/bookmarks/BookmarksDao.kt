package web.browser.dragon.huawei.database.bookmarks

import androidx.lifecycle.LiveData
import androidx.room.*
import web.browser.dragon.huawei.model.Bookmark
import io.reactivex.Single

@Dao
interface BookmarksDao {
    @Query("SELECT * FROM bookmark_table ORDER BY title ASC")
    fun getAllBookmarks(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmark_table WHERE isPopular == 0 ORDER BY title ASC")
    fun getSimpleBookmarks(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmark_table WHERE isPopular == 1 ORDER BY title ASC")
    fun getPopularBookmarks(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM bookmark_table ORDER BY title ASC")
    fun getAllBookmarksSync(): Single<List<Bookmark>>

    @Query("SELECT * FROM bookmark_table ORDER BY title ASC")
    fun getAllBookmarksSync1(): List<Bookmark>

    @Query("SELECT * FROM bookmark_table WHERE isPopular == 0 ORDER BY title ASC")
    fun getSimpleBookmarksSync1(): List<Bookmark>

    @Query("SELECT * FROM bookmark_table WHERE isPopular == 1 ORDER BY title ASC")
    fun getPopularBookmarksSync1(): List<Bookmark>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appModel: Bookmark)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSynced(appModel: Bookmark)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appModels: List<Bookmark>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSynced(appModels: List<Bookmark>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(appModel: Bookmark)

    @Query("DELETE FROM bookmark_table")
    suspend fun deleteAll()

    @Query("DELETE FROM bookmark_table WHERE id == :id")
    fun delete(id: Long)
}