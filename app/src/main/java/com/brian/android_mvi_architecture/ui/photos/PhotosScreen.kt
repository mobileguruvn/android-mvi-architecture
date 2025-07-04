package com.brian.android_mvi_architecture.ui.photos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brian.android_mvi_architecture.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@Composable
fun PhotosScreen(
    modifier: Modifier = Modifier,
    viewModel: PhotosViewModel = hiltViewModel(),
    onPhotoClick: (Int) -> Unit,
) {

    val state by viewModel.state.collectAsState()
    val searchQuery by viewModel.query.collectAsState()
    val context = LocalContext.current

    // Handle one-time effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PhotosEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            PhotoActionTopBar(
                isFavourite = false,
                onActionClick = { viewModel.processIntent(intent = PhotosIntent.ToggleFavourite) },
            )
        }
    ) { contentPadding ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            SearchView(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.processIntent(PhotosIntent.SearchPhotos(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            when (state) {
                is PhotosUiState.Loading -> ShowLoadingView()
                is PhotosUiState.ErrorWithRetry -> ShowErrorView((state as PhotosUiState.ErrorWithRetry).message)
                is PhotosUiState.Success -> PhotoList(
                    photos = (state as PhotosUiState.Success).photos,
                    onPhotoClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

}

@Composable
private fun PhotoList(
    photos: List<PhotoUi>,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (photos.isEmpty()) {
        ShowEmptyView()
    } else {
        LazyColumn(modifier) {
            items(photos) { photo ->
                PhotoItem(photo, onPhotoClick)
            }
        }
    }
}

@Composable
private fun PhotoItem(
    photo: PhotoUi,
    onPhotoClick: (Int) -> Unit,
) {
    val avatarColor = generateRandomColor()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                onPhotoClick(photo.id)
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "photo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp)
                    .background(avatarColor, CircleShape)
                    .clip(CircleShape),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = photo.title, modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (photo.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "favourite"
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
private fun generateRandomColor(): Color = remember {
    Color(
        red = Random.nextFloat(),
        green = Random.nextFloat(),
        blue = Random.nextFloat(),
        alpha = 1f
    )
}

@Composable
private fun SearchView(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = modifier,
            placeholder = { Text(text = "Search") },
            singleLine = true,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoActionTopBar(
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit,
    isFavourite: Boolean = false,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.photo_list_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        modifier = modifier,
        actions = {
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null
                )
            }
        }
    )
}

@Composable
private fun ShowEmptyView() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "No photos found",
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun ShowLoadingView() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun ShowErrorView(errorMessage: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Failed to load data: $errorMessage",
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Preview
@Composable
fun PhotoListPreview() {

}