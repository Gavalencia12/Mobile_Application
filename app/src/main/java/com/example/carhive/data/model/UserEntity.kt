package com.example.carhive.data.model

data class UserEntity(
    var id: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var voterID: String = "",
    var curp: String = "",
    var imageUrl: String? = null,
    var imageUrl2: String? = null,
    var role: Int = 2,
    var isverified: Boolean = false,
    var isBanned: Boolean = false,
    var termsUser: Boolean = false,
    var termsSeller: Boolean = false,
    var isExpanded: Boolean = false,
    var verificationTimestamp: String? = null,
)
