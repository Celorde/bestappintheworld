package com.example.bestuiintheworld.fragments.password


data class PasswordRule(
    val errorMessage: String,
    val validation: (String) -> Boolean
)
