package com.bestappintheworld.di

import com.bestappintheworld.fragments.security.CaptchaViewModel
import com.bestappintheworld.fragments.address.AddressViewModel
import com.bestappintheworld.fragments.contact.ContactViewModel
import com.bestappintheworld.fragments.extrainfo.ExtraInfoViewModel
import com.bestappintheworld.fragments.password.PasswordViewModel
import com.bestappintheworld.fragments.personal.PersonalDataViewModel
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
