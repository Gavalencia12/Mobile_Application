package com.example.carhive.di

import android.content.Context
import android.content.SharedPreferences
import com.example.carhive.data.datasource.local.SessionImpl
import com.example.carhive.data.datasource.local.UserRepositoryImpl
import com.example.carhive.data.datasource.remote.ChatRepositoryImpl
import com.example.carhive.data.datasource.remote.Firebase.FirebaseAuthDataSource
import com.example.carhive.data.datasource.remote.Firebase.FirebaseDatabaseDataSource
import com.example.carhive.data.datasource.remote.Firebase.FirebaseStorageDataSource
import com.example.carhive.data.datasource.remote.NotificationsRepositoryImpl
import com.example.carhive.data.datasource.remote.RepositoryImpl
import com.example.carhive.data.mapper.CarMapper
import com.example.carhive.data.mapper.MessageMapper
import com.example.carhive.data.mapper.UserMapper
import com.example.carhive.data.repository.AuthRepository
import com.example.carhive.data.repository.ChatRepository
import com.example.carhive.data.repository.NotificationsRepository
import com.example.carhive.data.repository.SessionRepository
import com.example.carhive.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for injecting application dependencies.
 *
 * Provides Singleton instances of the necessary classes of the application.
 *
 * This module provides the FirebaseAuth, FirebaseDatabase, FirebaseStorage instances and all the
 * application repositories, this module is only intended for third party repositories and instances.
 *
 */

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance() // Provides the FirebaseAuth instance.

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase =
        FirebaseDatabase.getInstance() // Provides the FirebaseDatabase instance .

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage =
        FirebaseStorage.getInstance() // Provides the FirebaseStorage instance.

    @Provides
    @Singleton
    fun provideContext(application: android.app.Application): Context =
        application // Provides the context of the application.

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(
            "UserPrefs",
            Context.MODE_PRIVATE
        ) // Provides SharedPreferences for managing user preferences .


    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        database: FirebaseDatabase,
        storage: FirebaseStorage,
        dataSourceDatabase: FirebaseDatabaseDataSource,
        dataSourceAuth: FirebaseAuthDataSource,
        dataSourceStorage: FirebaseStorageDataSource,
        userMapper: UserMapper,
        carMapper: CarMapper
    ): AuthRepository = RepositoryImpl(
        auth,
        database,
        storage,
        dataSourceDatabase,
        dataSourceAuth,
        dataSourceStorage,
        userMapper,
        carMapper
    ) // Provides the implementation of AuthRepository.

    @Provides
    @Singleton
    fun provideUserRepository(
        sharedPreferences: SharedPreferences,
        userMapper: UserMapper
    ): UserRepository = UserRepositoryImpl(
        sharedPreferences,
        userMapper
    ) // Provides the implementation of UserRepository.

    @Provides
    @Singleton
    fun provideSessionRepository(
        sharedPreferences: SharedPreferences,
        repository: AuthRepository,
    ): SessionRepository = SessionImpl(
        sharedPreferences,
        repository
    ) // Provides the implementation of SessionRepository.

    @Provides
    @Singleton
    fun provideChatRepository(
        context: Context,
        messageMapper: MessageMapper,
        database: FirebaseDatabase,
        storage: FirebaseStorage
    ) : ChatRepository = ChatRepositoryImpl(
        context,
        database,
        storage,
        messageMapper
    ) // Provides the implementation of ChatRepository.

    @Provides
    @Singleton
    fun provideNotificationsRepository(
        context: Context,
        database: FirebaseDatabase
    ) : NotificationsRepository = NotificationsRepositoryImpl(
        context,
        database
    )

}