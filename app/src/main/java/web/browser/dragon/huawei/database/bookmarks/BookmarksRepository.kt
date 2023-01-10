package web.browser.dragon.huawei.database.bookmarks

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import web.browser.dragon.huawei.model.Bookmark
import io.reactivex.Single

class BookmarksRepository(private val bookmarksDao: BookmarksDao) {
    val allBookmarks: LiveData<List<Bookmark>> = bookmarksDao.getAllBookmarks()
    val allBookmarksSync: List<Bookmark> = bookmarksDao.getAllBookmarksSync1()
    val simpleBookmarks: LiveData<List<Bookmark>> = bookmarksDao.getSimpleBookmarks()
    val simpleBookmarksSync: List<Bookmark> = bookmarksDao.getSimpleBookmarksSync1()
    val popularBookmarks: LiveData<List<Bookmark>> = bookmarksDao.getPopularBookmarks()
    val popularBookmarksSync: List<Bookmark> = bookmarksDao.getPopularBookmarksSync1()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun get(): Single<List<Bookmark>> {
        return bookmarksDao.getAllBookmarksSync()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(bookmark: Bookmark) {
        bookmarksDao.insert(bookmark)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertAll(Bookmarks: List<Bookmark>) {
        bookmarksDao.insertAll(Bookmarks)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(bookmark: Bookmark) {
        bookmarksDao.update(bookmark)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(bookmark: Bookmark) {
        bookmarksDao.delete(bookmark.id)
    }
}