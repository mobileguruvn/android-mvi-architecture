package com.brian.android_mvi_architecture.ui.photo_detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.brian.android_mvi_architecture.R
import com.brian.android_mvi_architecture.ui.photos.PhotoUi

@Composable
fun PhotoDetailScreen(
    photoId: Int,
    viewModel: PhotoDetailViewModel = hiltViewModel(),
    navigateUp: () -> Unit
) {

    LaunchedEffect(photoId) {
        viewModel.processIntent(intent = PhotoDetailIntent.FetchPhotoDetail(photoId))
    }

    val state by viewModel.uiState.collectAsState()
    val photoUi = state.asPhotoUiOrEmpty()

    var isShowDialogConfirmation by rememberSaveable { mutableStateOf(false) }

    if (isShowDialogConfirmation) {
        DislikeConfirmationDialog(
            onConfirm = {
                viewModel.processIntent(
                    intent = PhotoDetailIntent.UpdatePhotoState(
                        photoId = photoUi.id,
                        false
                    )
                )
                isShowDialogConfirmation = false
            },
            onDismiss = {
                isShowDialogConfirmation = false
            }
        )
    }

    Scaffold(
        topBar = {
            PhotoDetailToolbar(
                photoUi = photoUi,
                navigateUp = navigateUp,
                enable = state is PhotoDetailUiState.Success,
                onToggleFavourite = {
                    if (photoUi.isFavourite) {
                        isShowDialogConfirmation = true
                    } else {
                        viewModel.processIntent(
                            intent = PhotoDetailIntent.UpdatePhotoState(
                                photoId = photoUi.id,
                                true
                            )
                        )
                    }
                })
        }
    ) { contentPadding ->
        when (state) {
            is PhotoDetailUiState.Loading -> {
                ShowLoading(contentPadding = contentPadding)
            }

            is PhotoDetailUiState.Success -> {
                val photoUi = (state as PhotoDetailUiState.Success).photoUi
                PhotoDetailContent(photoUi = photoUi, contentPadding)
            }

            is PhotoDetailUiState.Error -> {
                ShowError(contentPadding = contentPadding)
            }
        }
    }
}


@Composable
private fun ShowLoading(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Loading...")
    }
}

@Composable
private fun PhotoDetailContent(
    photoUi: PhotoUi,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PhotoImage(
            photoUi.imageUrl,
            photoUi.title
        )
        Spacer(modifier = Modifier.height(32.dp))
        PhotoTitle(photoUi.title)
        Spacer(modifier = Modifier.height(8.dp))

    }
}

@Composable
private fun ShowError(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Something went wrong 😢")
    }
}

@Composable
private fun PhotoImage(imageUrl: String, contentDescription: String) {
    Image(
        painter = rememberAsyncImagePainter(imageUrl),
        contentDescription = contentDescription,
        modifier = Modifier
            .size(250.dp)
            .background(Color.LightGray)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
private fun PhotoTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoDetailToolbar(
    photoUi: PhotoUi,
    navigateUp: () -> Unit,
    enable: Boolean,
    onToggleFavourite: (PhotoUi) -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
            Text(text = stringResource(R.string.photo_detail_title))
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = navigateUp) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
            }
        },
        actions = {
            IconButton(onClick = { onToggleFavourite(photoUi) }, enabled = enable) {
                Icon(
                    imageVector = if (photoUi.isFavourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "back"
                )
            }
        }
    )
}


@Composable
fun DislikeConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text("Are you sure dislike the photo?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm".uppercase())
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel".uppercase())
            }
        }
    )
}