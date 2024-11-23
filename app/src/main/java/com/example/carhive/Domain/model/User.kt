package com.example.carhive.Domain.model

data class User(
    var id: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var voterID: String = "",
    var curp: String = "",
    var imageUrl: String? = null,
    var imageUrl2: String? = null,
    var role: UserRole = UserRole.NORMAL_USER,
    var termsUser: Boolean = false,
    var termsSeller: Boolean = false,
    var isVerified: Boolean = false,
    var verificationTimestamp: String? = null,
)