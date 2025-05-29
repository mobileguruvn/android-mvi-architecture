package com.brian.android_mvi_architecture.navigation

const val PHOTO_ID = "photoId"

sealed class Screen(val route: String) {
    object PhotoList : Screen("photo_list")
    object PhotoDetail : Screen("photo_detail/{$PHOTO_ID}") {
        fun createRoute(photoId: Int) = "photo_detail/$photoId"
    }

}