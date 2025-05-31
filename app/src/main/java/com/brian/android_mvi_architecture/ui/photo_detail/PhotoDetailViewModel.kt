package com.brian.android_mvi_architecture.ui.photo_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brian.android_mvi_architecture.data.repository.PhotoRepository
import com.brian.android_mvi_architecture.di.Dispatcher
import com.brian.android_mvi_architecture.di.PhotoDispatcher
import com.brian.android_mvi_architecture.ui.mapper.toPhotoUi
import com.brian.android_mvi_architecture.ui.photos.PhotoUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoDetailViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    @Dispatcher(PhotoDispatcher.IO) private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<PhotoDetailUiState>(PhotoDetailUiState.Loading)
    val uiState: StateFlow<PhotoDetailUiState> = _uiState.asStateFlow()

    fun processIntent(intent: PhotoDetailIntent) {
        when (intent) {
            is PhotoDetailIntent.FetchPhotoDetail -> {
                getPhotoDetail(intent.photoId)
            }

            is PhotoDetailIntent.UpdatePhotoState -> {
                updatePhotoState(photoId = intent.photoId, isFavourite = intent.isFavourite)
            }
        }
    }

    private fun getPhotoDetail(photoId: Int) {
        viewModelScope.launch {
            photoRepository.getPhotoById(photoId)
                .onStart { _uiState.value = PhotoDetailUiState.Loading }
                .collect { result ->
                    result.fold(
                        onSuccess = { favouritePhoto ->
                            val photoUi = favouritePhoto.toPhotoUi()
                            _uiState.value = PhotoDetailUiState.Success(photoUi)
                        },
                        onFailure = {
                            _uiState.value =
                                PhotoDetailUiState.Error(message = it.message ?: "Unknown error")
                        }
                    )
                }
        }
    }

    private fun updatePhotoState(photoId: Int, isFavourite: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            photoRepository.updatePhoto(photoId, isFavourite)
        }
    }
}

sealed interface PhotoDetailUiState {
    object Loading : PhotoDetailUiState
    data class Success(val photoUi: PhotoUi) : PhotoDetailUiState
    data class Error(val message: String) : PhotoDetailUiState
}

sealed class PhotoDetailIntent {
    data class FetchPhotoDetail(val photoId: Int) : PhotoDetailIntent()
    data class UpdatePhotoState(val photoId: Int, val isFavourite: Boolean) : PhotoDetailIntent()
}

fun PhotoDetailUiState.asPhotoUiOrEmpty(): PhotoUi {
    return (this as? PhotoDetailUiState.Success)?.photoUi ?: PhotoUi.EMPTY_PHOTO_UI
}