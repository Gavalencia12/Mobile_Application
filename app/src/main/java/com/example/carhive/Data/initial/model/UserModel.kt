package com.example.carhive.Data.initial.model

data class UserModel(
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var voterID: String = "",
    var curp: String = "",
    var imageUrl: String? = null

) {
    fun isValid(): Boolean {
        return firstName.isNotBlank() && lastName.isNotBlank() && isValidEmail(email) && phoneNumber.isNotBlank()
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
