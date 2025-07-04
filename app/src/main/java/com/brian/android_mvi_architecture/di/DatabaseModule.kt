package com.brian.android_mvi_architecture.di

import android.content.Context
import androidx.room.Room
import com.brian.android_mvi_architecture.data.local.database.PhotoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providePhotoDatabase(@ApplicationContext context: Context) : PhotoDatabase {
        return Room.databaseBuilder(
            context,
            PhotoDatabase::class.java,
            "photo_database"
        ).build()
    }

    @Singleton
    @Provides
    fun providePhotoDao(photoDatabase: PhotoDatabase) = photoDatabase.photoDao()

}