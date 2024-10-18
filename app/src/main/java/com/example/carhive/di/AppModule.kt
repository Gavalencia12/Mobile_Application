package com.example.carhive.di

import android.content.Context
import android.content.SharedPreferences
import com.example.carhive.Data.repository.AuthRepository
import com.example.carhive.Data.datasource.local.UserRepositoryImpl
import com.example.carhive.Data.datasource.remote.Firebase.FirebaseAuthDataSource
import com.example.carhive.Data.datasource.remote.Firebase.FirebaseDatabaseDataSource
import com.example.carhive.Data.datasource.remote.Firebase.FirebaseStorageDataSource
import com.example.carhive.Data.datasource.remote.RepositoryImpl
import com.example.carhive.Data.mapper.UserMapper
import com.example.carhive.Data.repository.SessionRepository
import com.example.carhive.Data.repository.UserRepository
import com.example.carhive.Data.datasource.local.SessionImpl
import com.example.carhive.Domain.usecase.auth.GetCurrentUserIdUseCase
import com.example.carhive.Domain.usecase.user.ClearUserPreferencesUseCase
import com.example.carhive.Domain.usecase.user.GetPasswordUseCase
import com.example.carhive.Domain.usecase.user.GetUserPreferencesUseCase
import com.example.carhive.Domain.usecase.auth.LoginUseCase
import com.example.carhive.Domain.usecase.auth.RegisterUseCase
import com.example.carhive.Domain.usecase.database.GetAllCarsFromDatabaseUseCase
import com.example.carhive.Domain.usecase.database.GetUserDataUseCase
import com.example.carhive.Domain.usecase.user.SavePasswordUseCase
import com.example.carhive.Domain.usecase.user.SaveUserPreferencesUseCase
import com.example.carhive.Domain.usecase.database.SaveUserToDatabaseUseCase
import com.example.carhive.Domain.usecase.database.UpdateTermsSellerUseCase
import com.example.carhive.Domain.usecase.database.UpdateUserRoleUseCase
import com.example.carhive.Domain.usecase.database.UploadToProfileImageUseCase
import com.example.carhive.Domain.usecase.session.GetUserRoleUseCase
import com.example.carhive.Domain.usecase.session.IsUserAuthenticatedUseCase
import com.example.carhive.Domain.usecase.session.SaveUserRoleUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger Hilt para la inyección de dependencias de la aplicación.
 *
 * Proporciona instancias singleton de las clases necesarias para el funcionamiento
 * de la aplicación, incluyendo Firebase, repositorios y casos de uso.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance() // Proporciona la instancia de FirebaseAuth.

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase =
        FirebaseDatabase.getInstance() // Proporciona la instancia de FirebaseDatabase.

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage =
        FirebaseStorage.getInstance() // Proporciona la instancia de FirebaseStorage.

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        database: FirebaseDatabase,
        storage: FirebaseStorage,
        dataSourceDatabase: FirebaseDatabaseDataSource,
        dataSourceAuth: FirebaseAuthDataSource,
        dataSourceStorage: FirebaseStorageDataSource,
        userMapper: UserMapper
    ): AuthRepository = RepositoryImpl(
        auth, database, storage, dataSourceDatabase, dataSourceAuth, dataSourceStorage, userMapper
    ) // Proporciona la implementación de AuthRepository.

    @Provides
    @Singleton
    fun provideContext(application: android.app.Application): Context =
        application // Proporciona el contexto de la aplicación.

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(
            "UserPrefs",
            Context.MODE_PRIVATE
        ) // Proporciona SharedPreferences para la gestión de preferencias del usuario.

    @Provides
    @Singleton
    fun provideUserRepository(
        sharedPreferences: SharedPreferences,
        userMapper: UserMapper
    ): UserRepository = UserRepositoryImpl(
        sharedPreferences,
        userMapper
    ) // Proporciona la implementación de UserRepository.

    @Provides
    @Singleton
    fun provideSessionRepository(
        sharedPreferences: SharedPreferences,
        repository: AuthRepository,
    ): SessionRepository =
        SessionImpl(
            sharedPreferences,
            repository
        ) // Proporciona la implementación de SessionRepository.

    // Provisión de casos de uso relacionados con la sesión y la autenticación
    @Provides
    @Singleton
    fun provideSaveUserRoleUseCase(repository: SessionRepository): SaveUserRoleUseCase =
        SaveUserRoleUseCase(repository)

    @Provides
    @Singleton
    fun provideGetUserRoleUseCase(repository: SessionRepository): GetUserRoleUseCase =
        GetUserRoleUseCase(repository)

    @Provides
    @Singleton
    fun provideIsUserAuthenticatedUseCase(repository: SessionRepository): IsUserAuthenticatedUseCase =
        IsUserAuthenticatedUseCase(repository)

    @Provides
    @Singleton
    fun provideGetCurrentUserIdUseCase(repository: AuthRepository): GetCurrentUserIdUseCase =
        GetCurrentUserIdUseCase(repository)

    @Provides
    @Singleton
    fun provideUpdateUserRoleUseCase(repository: AuthRepository): UpdateUserRoleUseCase =
        UpdateUserRoleUseCase(repository)

    @Provides
    @Singleton
    fun provideUpdateTermsSellerUseCase(repository: AuthRepository): UpdateTermsSellerUseCase =
        UpdateTermsSellerUseCase(repository)

    // Provisión de casos de uso relacionados con la gestión de usuarios
    @Provides
    @Singleton
    fun provideUploadProfileImageUseCase(repository: AuthRepository): UploadToProfileImageUseCase =
        UploadToProfileImageUseCase(repository)

    @Provides
    @Singleton
    fun provideSaveUserToDatabaseUseCase(repository: AuthRepository): SaveUserToDatabaseUseCase =
        SaveUserToDatabaseUseCase(repository)

    @Provides
    @Singleton
    fun provideGetUserDataUseCase(repository: AuthRepository): GetUserDataUseCase =
        GetUserDataUseCase(repository)

    @Provides
    @Singleton
    fun provideGetAllCarsFromDatabaseUseCase(repository: AuthRepository): GetAllCarsFromDatabaseUseCase =
        GetAllCarsFromDatabaseUseCase(repository)

    @Provides
    @Singleton
    fun provideRegisterUseCase(repository: AuthRepository): RegisterUseCase =
        RegisterUseCase(repository)

    @Provides
    @Singleton
    fun provideLoginUserCase(repository: AuthRepository): LoginUseCase =
        LoginUseCase(repository)

    @Provides
    @Singleton
    fun provideSavePasswordUseCase(repository: UserRepository): SavePasswordUseCase =
        SavePasswordUseCase(repository)

    @Provides
    @Singleton
    fun provideSaveUserPreferencesUseCase(userPreferences: UserRepository): SaveUserPreferencesUseCase =
        SaveUserPreferencesUseCase(userPreferences)

    @Provides
    @Singleton
    fun provideGetUserPreferencesUseCase(userPreferences: UserRepository): GetUserPreferencesUseCase =
        GetUserPreferencesUseCase(userPreferences)

    @Provides
    @Singleton
    fun provideClearUserPreferencesUseCase(userPreferences: UserRepository): ClearUserPreferencesUseCase =
        ClearUserPreferencesUseCase(userPreferences)

    @Provides
    @Singleton
    fun provideGetPasswordUseCase(userPreferences: UserRepositoryImpl): GetPasswordUseCase =
        GetPasswordUseCase(userPreferences)
}
