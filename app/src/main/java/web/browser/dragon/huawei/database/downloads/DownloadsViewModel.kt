package web.browser.dragon.huawei.database.downloads

import androidx.lifecycle.*
import web.browser.dragon.huawei.model.DownloadModel
import kotlinx.coroutines.launch

class DownloadModelsViewModel(private val repository: DownloadsRepository) : ViewModel() {
    var allDownloadModels: LiveData<List<DownloadModel>> = repository.allDownloadModels

    fun insert(download: DownloadModel)
    {
        viewModelScope.launch {
            repository.insert(download)
        }
    }
    fun insertAll(downloads: List<DownloadModel>): LiveData<Boolean> {

        val data = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repository.insertAll(downloads)
            data.postValue(true)
        }

        return data
    }

    fun update(download: DownloadModel): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repository.update(download)
            data.postValue(true)
        }
        return data
    }
    fun delete(download: DownloadModel): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repository.delete(download)
            data.postValue(true)
        }

        return data
    }
}

class DownloadModelsViewModelFactory(private val repository: DownloadsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DownloadModelsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DownloadModelsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}