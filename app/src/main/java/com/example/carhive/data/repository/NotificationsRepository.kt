package com.example.carhive.data.repository

interface NotificationsRepository {

    suspend fun listenForChatMessages(groupId: String, onNewMessage: (String, String, String) -> Unit)
    suspend fun listenForNewFavorites(carId: String, onFavoriteAdded: (String, String, String) -> Unit)
    suspend fun listenForCarApprovalStatus(ownerId: String, carId: String, onStatusChanged: (Boolean, String) -> Unit)
    suspend fun listenForUserVerification(userId: String, isVerified: Boolean, fullName: String)
    suspend fun notifyCarApprovalStatus(userId: String, carModel: String, isApproved: Boolean)
    suspend fun addNotification(userId: String, title: String, message: String)
    suspend fun markNotificationAsRead(userId: String, notificationId: String)
    suspend fun showNotification(title: String, message: String)

}