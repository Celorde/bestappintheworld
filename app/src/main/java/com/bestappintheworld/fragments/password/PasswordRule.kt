package com.example.bestappintheworld.fragments.password


data class PasswordRule(
    val errorMessage: String,
    val validation: (String) -> Boolean
)
