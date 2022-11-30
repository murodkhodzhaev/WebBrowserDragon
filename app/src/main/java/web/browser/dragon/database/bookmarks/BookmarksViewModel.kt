package web.browser.dragon.database.bookmarks

import androidx.lifecycle.*
import web.browser.dragon.model.Bookmark
import kotlinx.coroutines.launch

class BookmarksViewModel(private val repository: BookmarksRepository) : ViewModel() {
    var allBookmarks: LiveData<List<Bookmark>> = repository.allBookmarks
    var allBookmarksSync: List<Bookmark> = repository.allBookmarksSync
    var simpleBookmarks: LiveData<List<Bookmark>> = repository.simpleBookmarks
    var simpleBookmarksSync: List<Bookmark> = repository.simpleBookmarksSync
    var popularBookmarks: LiveData<List<Bookmark>> = repository.popularBookmarks
    var popularBookmarksSync: List<Bookmark> = repository.popularBookmarksSync

    fun checkIfBookmarkAlreadyAdded(url: String): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        viewModelScope.launch {
            val bookmarks = repository.allBookmarksSync
            data.postValue(bookmarks?.find { it.link == url } != null)
        }

        return data
    }

    fun insert(bookmark: Bookmark): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repository.insert(bookmark)
            data.postValue(true)
        }

        return data
    }
    fun insertAll(bookmarks: List<Bookmark>): LiveData<Boolean> {

        val data = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repository.insertAll(bookmarks)
            data.postValue(true)
        }

        return data
    }

    fun update(bookmark: Bookmark): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repository.update(bookmark)
            data.postValue(true)
        }
        return data
    }
    fun delete(bookmark: Bookmark): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repository.delete(bookmark)
            data.postValue(true)
        }

        return data
    }
}

class BookmarksViewModelFactory(private val repository: BookmarksRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookmarksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookmarksViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}