package com.example.carhive.Data.admin.models

data class User (var id : String, var name: String, var email: String){
    constructor() : this("", "", "")
}