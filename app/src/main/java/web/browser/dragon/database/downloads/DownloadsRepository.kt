package web.browser.dragon.database.downloads

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import web.browser.dragon.model.DownloadModel
import io.reactivex.Single

class DownloadsRepository(private val downloadsDao: DownloadsDao) {
    val allDownloadModels: LiveData<List<DownloadModel>> = downloadsDao.getAllDownloads()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun get(): Single<List<DownloadModel>> {
        return downloadsDao.getAllDownloadsSync()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(download: DownloadModel) {
        downloadsDao.insert(download)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertAll(DownloadModels: List<DownloadModel>) {
        downloadsDao.insertAll(DownloadModels)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(download: DownloadModel) {
        downloadsDao.update(download)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(download: DownloadModel) {
        downloadsDao.delete(download.dateTimestamp)
    }
}