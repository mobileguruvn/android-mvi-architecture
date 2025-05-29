package com.brian.android_mvi_architecture.ui.mapper

import com.brian.android_mvi_architecture.data.model.FavouritePhoto
import com.brian.android_mvi_architecture.data.model.Photo
import com.brian.android_mvi_architecture.ui.photos.PhotoUi

fun Photo.toPhotoUi(isFavourite: Boolean) = PhotoUi(
    id = id,
    title = title,
    imageUrl = url,
    thumbnailUrl = thumbnailUrl,
    isFavourite = isFavourite,
)

fun FavouritePhoto.toPhotoUi() = PhotoUi(
    id = photo.id,
    title = photo.title,
    imageUrl = photo.url,
    thumbnailUrl = photo.thumbnailUrl,
    isFavourite = isFavourite
)