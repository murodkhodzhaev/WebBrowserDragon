package web.browser.dragon.huawei.database.history

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import web.browser.dragon.huawei.model.HistoryRecord
import io.reactivex.Single

class HistoryRecordsRepository(private val history_recordsDao: HistoryRecordsDao) {
    val allHistoryRecords: LiveData<List<HistoryRecord>> = history_recordsDao.getAllHistoryRecords()
    val visibleHistoryRecords: LiveData<List<HistoryRecord>> = history_recordsDao.getVisibleHistoryRecords()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun get(): Single<List<HistoryRecord>> {
        return history_recordsDao.getAllHistoryRecordsSync()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(history_record: HistoryRecord) {
        history_recordsDao.insert(history_record)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertAll(HistoryRecords: List<HistoryRecord>) {
        history_recordsDao.insertAll(HistoryRecords)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(history_record: HistoryRecord) {
        history_recordsDao.update(history_record)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(history_record: HistoryRecord) {
        history_recordsDao.delete(history_record.dateTimestamp)
    }
}