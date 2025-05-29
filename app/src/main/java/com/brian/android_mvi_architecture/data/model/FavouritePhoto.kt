package com.brian.android_mvi_architecture.data.model

data class FavouritePhoto(
    val photo: Photo,
    val isFavourite: Boolean = false,
)
