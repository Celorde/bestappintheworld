package com.example.bestuiintheworld.di

import com.example.bestuiintheworld.fragments.address.AddressViewModel
import com.example.bestuiintheworld.fragments.contact.ContactViewModel
import com.example.bestuiintheworld.fragments.extrainfo.ExtraInfoViewModel
import com.example.bestuiintheworld.fragments.password.PasswordViewModel
import com.example.bestuiintheworld.fragments.personal.PersonalDataViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { PersonalDataViewModel() }
    viewModel { ContactViewModel() }
    viewModel { AddressViewModel() }
    viewModel { ExtraInfoViewModel() }
    viewModel { PasswordViewModel() }
}
