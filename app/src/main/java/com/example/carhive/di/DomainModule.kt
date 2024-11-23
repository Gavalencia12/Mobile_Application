package com.example.carhive.di

import com.example.carhive.data.datasource.local.UserRepositoryImpl
import com.example.carhive.data.repository.AuthRepository
import com.example.carhive.data.repository.SessionRepository
import com.example.carhive.data.repository.UserRepository
import com.example.carhive.Domain.usecase.auth.GetCurrentUserIdUseCase
import com.example.carhive.Domain.usecase.auth.IsVerifiedTheEmailUseCase
import com.example.carhive.Domain.usecase.auth.LoginUseCase
import com.example.carhive.Domain.usecase.auth.RegisterUseCase
import com.example.carhive.Domain.usecase.chats.CleanUpDatabaseUseCase
import com.example.carhive.Domain.usecase.chats.DeleteMessageForUserUseCase
import com.example.carhive.Domain.usecase.chats.GetAllMessagesOnceUseCase
import com.example.carhive.Domain.usecase.chats.GetInterestedUsersUseCase
import com.example.carhive.Domain.usecase.chats.GetMessagesUseCase
import com.example.carhive.Domain.usecase.chats.GetSupportUsersUseCase
import com.example.carhive.Domain.usecase.chats.GetUserInfoUseCase
import com.example.carhive.Domain.usecase.chats.SendFileMessageUseCase
import com.example.carhive.Domain.usecase.chats.SendMessageUseCase
import com.example.carhive.Domain.usecase.chats.UpdateMessageStatusUseCase
import com.example.carhive.Domain.usecase.database.DeleteCarInDatabaseUseCase
import com.example.carhive.Domain.usecase.database.GetAllCarsFromDatabaseUseCase
import com.example.carhive.Domain.usecase.database.GetCarUserInDatabaseUseCase
import com.example.carhive.Domain.usecase.database.GetUserDataUseCase
import com.example.carhive.Domain.usecase.database.SaveCarToDatabaseUseCase
import com.example.carhive.Domain.usecase.database.SaveUserToDatabaseUseCase
import com.example.carhive.Domain.usecase.database.UpdateCarToDatabaseUseCase
import com.example.carhive.Domain.usecase.database.UpdateTermsSellerUseCase
import com.example.carhive.Domain.usecase.database.UpdateUserRoleUseCase
import com.example.carhive.Domain.usecase.database.UploadToCarImageUseCase
import com.example.carhive.Domain.usecase.database.UploadToProfileImageUseCase
import com.example.carhive.Domain.usecase.favorites.AddCarToFavoritesUseCase
import com.example.carhive.Domain.usecase.favorites.GetCarFavoriteCountAndUsersUseCase
import com.example.carhive.Domain.usecase.favorites.GetUserFavoriteCarsUseCase
import com.example.carhive.Domain.usecase.favorites.GetUserFavoritesUseCase
import com.example.carhive.Domain.usecase.favorites.RemoveCarFromFavoritesUseCase
import com.example.carhive.Domain.usecase.notifications.AddNotificationUseCase
import com.example.carhive.Domain.usecase.notifications.ListenForCarApprovalStatusUseCase
import com.example.carhive.Domain.usecase.notifications.ListenForChatMessagesUseCase
import com.example.carhive.Domain.usecase.notifications.ListenForNewFavoritesUseCase
import com.example.carhive.Domain.usecase.notifications.ListenForUserVerificationUseCase
import com.example.carhive.Domain.usecase.notifications.MarkNotificationAsReadUseCase
import com.example.carhive.Domain.usecase.notifications.NotifyCarApprovalStatusUseCase
import com.example.carhive.Domain.usecase.notifications.ShowNotificationUseCase
import com.example.carhive.Domain.usecase.session.GetUserRoleUseCase
import com.example.carhive.Domain.usecase.session.IsUserAuthenticatedUseCase
import com.example.carhive.Domain.usecase.session.SaveUserRoleUseCase
import com.example.carhive.Domain.usecase.user.ClearUserPreferencesUseCase
import com.example.carhive.Domain.usecase.user.GetPasswordUseCase
import com.example.carhive.Domain.usecase.user.GetUserPreferencesUseCase
import com.example.carhive.Domain.usecase.user.SavePasswordUseCase
import com.example.carhive.Domain.usecase.user.SaveUserPreferencesUseCase
import com.example.carhive.data.repository.ChatRepository
import com.example.carhive.data.repository.NotificationsRepository
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
 * This module provides the use case instances of the application for the roles of all users.
 * These use cases will be separated by what the use case is used for, for example, if it was used
 * for user authentication, for creating a user, for creating a vehicle, etc.
 *
 * Also each subtopic will have other subtopics to divide what it was used for, if it was used for
 * user authentication, for user creation, for vehicle creation, etc.
 *
 */

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

//    <-------------------------- Use cases for Firebase Authentication -------------------------->

    //      <-------------------------------- Shared Preferences --------------------------------->

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

    //      <---------------------------------------- End ----------------------------------------->
    //      <-------------------------------- Register and Login ---------------------------------->

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
        fun provideIsVerifiedTheEmailUseCase(repository: AuthRepository): IsVerifiedTheEmailUseCase =
            IsVerifiedTheEmailUseCase(repository)

        @Provides
        @Singleton
        fun provideRegisterUseCase(repository: AuthRepository): RegisterUseCase =
            RegisterUseCase(repository)

        @Provides
        @Singleton
        fun provideLoginUserCase(repository: AuthRepository): LoginUseCase =
            LoginUseCase(repository)
    //      <---------------------------------------- End ----------------------------------------->



//    <-------------------------- Use cases for Firebase Database -------------------------------->

    //      <--------------------------------------- Roles --------------------------------------->
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
        fun provideUpdateUserRoleUseCase(repository: AuthRepository): UpdateUserRoleUseCase =
            UpdateUserRoleUseCase(repository)

    //      <---------------------------------------- End ----------------------------------------->
    //      <---------------------------------------- Cars ---------------------------------------->

        @Provides
        @Singleton
        fun provideGetAllCarsFromDatabaseUseCase(repository: AuthRepository): GetAllCarsFromDatabaseUseCase =
            GetAllCarsFromDatabaseUseCase(repository)

        @Provides
        @Singleton
        fun provideSaveCarToDatabaseUseCase(repository: AuthRepository): SaveCarToDatabaseUseCase =
            SaveCarToDatabaseUseCase(repository)

        @Provides
        @Singleton
        fun provideDeleteCarInDatabaseUseCase(repository: AuthRepository): DeleteCarInDatabaseUseCase =
            DeleteCarInDatabaseUseCase(repository)

        @Provides
        @Singleton
        fun provideUpdateCarToDatabaseUseCase(repository: AuthRepository): UpdateCarToDatabaseUseCase =
            UpdateCarToDatabaseUseCase(repository)

        @Provides
        @Singleton
        fun provideGetCarUserInDatabaseUseCase(repository: AuthRepository): GetCarUserInDatabaseUseCase =
            GetCarUserInDatabaseUseCase(repository)

    //      <---------------------------------------- End ----------------------------------------->
    //      <---------------------------------------- Users --------------------------------------->

        @Provides
        @Singleton
        fun provideUpdateTermsSellerUseCase(repository: AuthRepository): UpdateTermsSellerUseCase =
            UpdateTermsSellerUseCase(repository)

        @Provides
        @Singleton
        fun provideSaveUserToDatabaseUseCase(repository: AuthRepository): SaveUserToDatabaseUseCase =
            SaveUserToDatabaseUseCase(repository)

        @Provides
        @Singleton
        fun provideGetUserDataUseCase(repository: AuthRepository): GetUserDataUseCase =
            GetUserDataUseCase(repository)

    //      <---------------------------------------- End ----------------------------------------->
    //      <-------------------------------------- Favorites--------------------------------------->

        @Provides
        @Singleton
        fun provideAddCarToFavoritesUseCase(repository: AuthRepository): AddCarToFavoritesUseCase =
            AddCarToFavoritesUseCase(repository)

        @Provides
        @Singleton
        fun provideGetUserFavoritesUseCase(repository: AuthRepository): GetUserFavoritesUseCase =
            GetUserFavoritesUseCase(repository)

        @Provides
        @Singleton
        fun provideGetCarFavoriteCountAndUsersUseCase(repository: AuthRepository): GetCarFavoriteCountAndUsersUseCase =
            GetCarFavoriteCountAndUsersUseCase(repository)

        @Provides
        @Singleton
        fun provideRemoveCarFromFavoritesUseCase(repository: AuthRepository): RemoveCarFromFavoritesUseCase =
            RemoveCarFromFavoritesUseCase(repository)

        @Provides
        @Singleton
        fun provideGetUserFavoriteCarsUseCase(repository: AuthRepository): GetUserFavoriteCarsUseCase =
            GetUserFavoriteCarsUseCase(repository)
    //      <---------------------------------------- End ----------------------------------------->

    //      <------------------------------------- Messages --------------------------------------->

        @Provides
        @Singleton
        fun provideSendMessageUseCase(repository: ChatRepository): SendMessageUseCase =
            SendMessageUseCase(repository)

        @Provides
        @Singleton
        fun provideGetMessagesUseCase(repository: ChatRepository): GetMessagesUseCase =
            GetMessagesUseCase(repository)

        @Provides
        @Singleton
        fun provideGetUserInfoUseCase(repository: ChatRepository): GetUserInfoUseCase =
            GetUserInfoUseCase(repository)

        @Provides
        @Singleton
        fun provideGetInterestedUsersUseCase(repository: ChatRepository): GetInterestedUsersUseCase =
            GetInterestedUsersUseCase(repository)

        @Provides
        @Singleton
        fun provideSendFileMessageUseCase(repository: ChatRepository): SendFileMessageUseCase =
            SendFileMessageUseCase(repository)

        @Provides
        @Singleton
        fun provideCleanUpDatabaseUseCase(repository: ChatRepository): CleanUpDatabaseUseCase =
            CleanUpDatabaseUseCase(repository)

        @Provides
        @Singleton
        fun provideDeleteMessageForUserUseCase(repository: ChatRepository): DeleteMessageForUserUseCase =
            DeleteMessageForUserUseCase(repository)

        @Provides
        @Singleton
        fun provideUpdateMessageStatusUseCase(repository: ChatRepository): UpdateMessageStatusUseCase =
            UpdateMessageStatusUseCase(repository)

        @Provides
        @Singleton
        fun provideGetAllMessagesOnceUseCase(repository: ChatRepository): GetAllMessagesOnceUseCase =
            GetAllMessagesOnceUseCase(repository)

        @Provides
        @Singleton
        fun provideGetSupportUsersUseCase(repository: ChatRepository): GetSupportUsersUseCase =
            GetSupportUsersUseCase(repository)

    //      <---------------------------------------- End ----------------------------------------->

    //      <---------------------------------- Notifications ------------------------------------->
        @Provides
        @Singleton
        fun provideListenForChatMessagesUseCase(repository: NotificationsRepository): ListenForChatMessagesUseCase =
            ListenForChatMessagesUseCase(repository)

        @Provides
        @Singleton
        fun provideListenForNewFavoritesUseCase(repository: NotificationsRepository): ListenForNewFavoritesUseCase =
            ListenForNewFavoritesUseCase(repository)

        @Provides
        @Singleton
        fun provideListenForCarApprovalStatusUseCase(repository: NotificationsRepository): ListenForCarApprovalStatusUseCase =
            ListenForCarApprovalStatusUseCase(repository)

        @Provides
        @Singleton
        fun provideNotifyCarApprovalStatusUseCase(repository: NotificationsRepository): NotifyCarApprovalStatusUseCase =
            NotifyCarApprovalStatusUseCase(repository)

        @Provides
        @Singleton
        fun provideListenForUserVerificationUseCase(repository: NotificationsRepository): ListenForUserVerificationUseCase =
            ListenForUserVerificationUseCase(repository)

        @Provides
        @Singleton
        fun provideAddNotificationUseCase(repository: NotificationsRepository): AddNotificationUseCase =
            AddNotificationUseCase(repository)

        @Provides
        @Singleton
        fun provideMarkNotificationAsReadUseCase(repository: NotificationsRepository): MarkNotificationAsReadUseCase =
            MarkNotificationAsReadUseCase(repository)

        @Provides
        @Singleton
        fun provideShowNotificationUseCase(repository: NotificationsRepository): ShowNotificationUseCase =
            ShowNotificationUseCase(repository)

    //      <---------------------------------------- End ----------------------------------------->


//    <-------------------------- Use cases for Firebase Storage --------------------------------->

    //      <---------------------------------------- Users --------------------------------------->

        @Provides
        @Singleton
        fun provideUploadProfileImageUseCase(repository: AuthRepository): UploadToProfileImageUseCase =
            UploadToProfileImageUseCase(repository)

    //      <---------------------------------------- End ----------------------------------------->
    //      <---------------------------------------- Cars ---------------------------------------->

        @Provides
            @Singleton
            fun provideUploadToCarImageUseCase(repository: AuthRepository): UploadToCarImageUseCase =
                UploadToCarImageUseCase(repository)
    //      <---------------------------------------- End ----------------------------------------->

}