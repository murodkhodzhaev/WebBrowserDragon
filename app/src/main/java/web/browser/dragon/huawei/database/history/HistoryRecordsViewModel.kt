package web.browser.dragon.huawei.database.history

import androidx.lifecycle.*
import web.browser.dragon.huawei.model.HistoryRecord
import kotlinx.coroutines.launch

class HistoryRecordsViewModel(private val repository: HistoryRecordsRepository) : ViewModel() {
    var allHistoryRecords: LiveData<List<HistoryRecord>> = repository.allHistoryRecords
    var visibleHistoryRecords: LiveData<List<HistoryRecord>> = repository.visibleHistoryRecords

    fun insert(historyRecord: HistoryRecord) = viewModelScope.launch {
        repository.insert(historyRecord)
    }
    fun insertAll(historyRecords: List<HistoryRecord>): LiveData<Boolean> {

        val data = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repository.insertAll(historyRecords)
            data.postValue(true)
        }

        return data
    }

    fun update(historyRecord: HistoryRecord): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repository.update(historyRecord)
            data.postValue(true)
        }
        return data
    }
    fun delete(historyRecord: HistoryRecord): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repository.delete(historyRecord)
            data.postValue(true)
        }

        return data
    }
}

class HistoryRecordsViewModelFactory(private val repository: HistoryRecordsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryRecordsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryRecordsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}