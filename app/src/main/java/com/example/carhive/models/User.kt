package com.example.carhive.models

data class User (var id : String, var name: String, var email: String){
    constructor() : this("", "", "")
}