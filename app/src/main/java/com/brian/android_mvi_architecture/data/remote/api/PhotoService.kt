package com.brian.android_mvi_architecture.data.remote.api

import com.brian.android_mvi_architecture.data.model.Photo
import retrofit2.Response
import retrofit2.http.GET

interface PhotoService {

    @GET("photos")
    suspend fun getPhotos(): Response<List<Photo>>
}