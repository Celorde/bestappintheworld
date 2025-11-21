package com.example.bestappintheworld.di

import com.bestappintheworld.fragments.security.CaptchaViewModel
import com.example.bestappintheworld.fragments.address.AddressViewModel
import com.example.bestappintheworld.fragments.contact.ContactViewModel
import com.example.bestappintheworld.fragments.extrainfo.ExtraInfoViewModel
import com.example.bestappintheworld.fragments.password.PasswordViewModel
import com.example.bestappintheworld.fragments.personal.PersonalDataViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { PersonalDataViewModel() }
    viewModel { ContactViewModel() }
    viewModel { AddressViewModel() }
    viewModel { ExtraInfoViewModel() }
    viewModel { PasswordViewModel() }
    viewModel { CaptchaViewModel() }
}
