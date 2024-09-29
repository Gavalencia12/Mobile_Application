package com.example.carhive.di

import android.content.Context
import com.example.carhive.Data.initial.repository.AuthRepository
import com.example.carhive.Data.initial.repository.FirebaseRepositoryImpl
import com.example.carhive.Data.initial.storage.UserPreferences
import com.example.carhive.Domain.initial.usecase.LoginUseCase
import com.example.carhive.Domain.initial.usecase.RegisterUseCase
import com.example.carhive.Domain.initial.usecase.SaveUserToDatabaseUseCase
import com.example.carhive.Domain.initial.usecase.UploadToProfileImageUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        database: FirebaseDatabase,
        storage: FirebaseStorage,
    ): AuthRepository = FirebaseRepositoryImpl(auth, database, storage)

    @Provides
    @Singleton
    fun provideContext(application: android.app.Application): Context {
        return application
    }

    @Provides
    @Singleton
    fun provideRegisterUseCase(repository: AuthRepository): RegisterUseCase {
        return RegisterUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUploadProfileImageUseCase(repository: AuthRepository): UploadToProfileImageUseCase {
        return UploadToProfileImageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSaveUserToDatabaseUseCase(repository: AuthRepository): SaveUserToDatabaseUseCase {
        return SaveUserToDatabaseUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideLoginUserCase(repository: AuthRepository): LoginUseCase {
        return LoginUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUserPreferences(context: Context): UserPreferences {
        return UserPreferences(context)
    }


}
