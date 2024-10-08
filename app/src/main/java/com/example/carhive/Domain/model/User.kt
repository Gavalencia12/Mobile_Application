package com.example.carhive.Domain.model

data class User(
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var voterID: String = "",
    var curp: String = "",
    var imageUrl: String? = null,
    var role: UserRole = UserRole.NORMAL_USER,
    var terms: Boolean = false,
    var isVerified: Boolean = false
)