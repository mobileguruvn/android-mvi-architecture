package com.brian.android_mvi_architecture.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.brian.android_mvi_architecture.ui.photo_detail.PhotoDetailScreen
import com.brian.android_mvi_architecture.ui.photos.PhotosScreen
import com.brian.android_mvi_architecture.ui.photos.PhotosViewModel

@Composable
fun PhotoNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.PhotoList.route,
        modifier
    ) {
        composable(Screen.PhotoList.route) {
            PhotosScreen(
                onPhotoClick = { photoId ->
                    navController.navigate(Screen.PhotoDetail.createRoute(photoId = photoId))
                }
            )
        }

        composable(
            route = Screen.PhotoDetail.route,
            arguments = listOf(navArgument(PHOTO_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getInt(PHOTO_ID) ?: return@composable
            PhotoDetailScreen(photoId = photoId, navigateUp = {
                navController.navigateUp()
            })
        }
    }
}