package com.brian.android_mvi_architecture.ui.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brian.android_mvi_architecture.data.repository.PhotoRepository
import com.brian.android_mvi_architecture.ui.mapper.toPhotoUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(private val photoRepository: PhotoRepository) :
    ViewModel() {

    private val _state = MutableStateFlow<PhotosUiState>(PhotosUiState.Loading)
    val state: StateFlow<PhotosUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<PhotosEffect>()
    val effect: SharedFlow<PhotosEffect> = _effect.asSharedFlow()

    private val _query = MutableStateFlow("")
    val query : StateFlow<String> = _query.asStateFlow()

    private val isFavourite = MutableStateFlow(false)

    init {
        fetchPhotos()
    }

    fun processIntent(intent: PhotosIntent) {
        when (intent) {
            is PhotosIntent.SearchPhotos -> _query.value = intent.query
            is PhotosIntent.ToggleFavourite -> isFavourite.value = !isFavourite.value
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun fetchPhotos() {
        viewModelScope.launch {
            combine(
                query,
                isFavourite,
                ::Pair
            ).debounce(300)
                .onStart { _state.value = PhotosUiState.Loading }
                .flatMapLatest { (query, fav) ->
                    val source =
                        if (fav) photoRepository.getFavouritePhotos() else photoRepository.getPhotos()
                    source.map { result ->
                        result.getOrElse { emptyList() }
                            .filter { it.photo.title.contains(query, ignoreCase = true) }
                            .map { it.photo.toPhotoUi(it.isFavourite) }
                    }
                }
                .catch {
                    _effect.emit(PhotosEffect.ShowError(it.message ?: "Unknown error"))
                    _state.value = PhotosUiState.ErrorWithRetry(message = it.message ?: "Unknown error")
                }
                .collect { photoList ->
                    _state.value = PhotosUiState.Success(photoList)
                }

        }
    }
}

sealed class PhotosUiState {
    object Loading : PhotosUiState()
    data class Success(
        val photos: List<PhotoUi>
    ) : PhotosUiState()
    data class ErrorWithRetry(val message : String) : PhotosUiState()
}

sealed class PhotosIntent {
    data class SearchPhotos(val query: String) : PhotosIntent()
    object ToggleFavourite : PhotosIntent()
}

sealed class PhotosEffect {
    data class ShowError(val message: String) : PhotosEffect()
}

data class PhotoUi(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val isFavourite: Boolean,
)