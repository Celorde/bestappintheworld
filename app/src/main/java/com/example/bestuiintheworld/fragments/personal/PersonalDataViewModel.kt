package com.example.bestuiintheworld.fragments.personal

import android.icu.util.Calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bestuiintheworld.commons.StringUtils
import com.example.bestuiintheworld.commons.Utils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

sealed class PersonalDataEvent {
    data object EraseAgeLastChar : PersonalDataEvent()
    data object EraseNickname : PersonalDataEvent()
    data object BlockAgeField : PersonalDataEvent()
    data object RomanNumeralsWarning : PersonalDataEvent()
    data object ResetAge : PersonalDataEvent()
    data class ShowError(val message: String) : PersonalDataEvent()
}

data class PersonalDataState(
    val name: String = "",
    val birthdate: String = "",
    val shouldShowSex: Boolean = false
)

class PersonalDataViewModel : ViewModel() {

    private val _personalDataEvent = MutableSharedFlow<PersonalDataEvent?>()
    val personalDataEvent = _personalDataEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(PersonalDataState())
    val uiState: StateFlow<PersonalDataState> = _uiState

    private var hasValidateIfRomanNumber = false
    private var ageErrorNumber = 0
    private val ageMaxError = 3
    private var isAgeBlocked = false

    fun parseSelectedNameLetter(letters: List<String>, index: Int) {
        var nameValue = _uiState.value.name

        if (letters[index] == StringUtils.KeyboardCommands.SPACE.value) {
            nameValue += " "
        } else if (letters[index] == StringUtils.KeyboardCommands.BACKSPACE.value) {
            nameValue = ""
        } else {
            nameValue += letters[index]
        }

        _uiState.update { state ->
            state.copy(name = nameValue)
        }
    }

    fun setDateToField(dayOfYear: Int, year: Int) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        calendar.set(Calendar.DAY_OF_YEAR, dayOfYear)
        calendar.set(Calendar.YEAR, year)
        val formattedDate = dateFormat.format(calendar.time)

        _uiState.update { state ->
            state.copy(birthdate = formattedDate)
        }
    }

    fun eraseNicknameField(text: String) {
        if (text.isNotEmpty()) {
            viewModelScope.launch {
                _personalDataEvent.emit(PersonalDataEvent.EraseNickname)
            }
        }
    }

    fun validateRomanNumerals(text: String) {
        if (!text.toString().all { it in Utils.romanNumerals }) {
            viewModelScope.launch {
                _personalDataEvent.emit(PersonalDataEvent.EraseAgeLastChar)
            }

            if (ageErrorNumber == ageMaxError) {
                if (hasValidateIfRomanNumber) {
                    viewModelScope.launch {
                        _personalDataEvent.apply {
                            emit(PersonalDataEvent.ShowError("Você não digitou corretamente. Estude mais sobre algarismos romanos."))
                            emit(PersonalDataEvent.BlockAgeField)
                        }
                    }
                } else {
                    hasValidateIfRomanNumber = true

                    viewModelScope.launch {
                        _personalDataEvent.apply {
                            emit(PersonalDataEvent.ShowError("A idade precisa ser em Números Romanos, amigo."))
                            emit(PersonalDataEvent.RomanNumeralsWarning)
                        }
                    }
                }

                ageErrorNumber = 0
            } else {
                ageErrorNumber++
            }
        }
    }

    fun validateAgeText(hasFocus: Boolean, birthdate: String, age: String) {
        if (!hasFocus) {
            if (age.isNotEmpty()) {
                if (!StringUtils.isRomanValid(age)) {
                    viewModelScope.launch {
                        _personalDataEvent.apply {
                            emit(PersonalDataEvent.ShowError("Esse número não existe. Estude mais sobre algarismos romanos."))
                            emit(PersonalDataEvent.BlockAgeField)
                        }
                    }
                } else {
                    val isAgeValid = calculateCorrectAge(
                        birthdateValue = birthdate,
                        ageValue = age
                    )

                    _uiState.update { state ->
                        state.copy(shouldShowSex = isAgeValid)
                    }
                }
            }
        } else {
            if (isAgeBlocked) {
                viewModelScope.launch {
                    _personalDataEvent.apply {
                        emit(PersonalDataEvent.ShowError("Campo bloqueado até você estudar sobre números romanos no link abaixo."))
                        emit(PersonalDataEvent.BlockAgeField)
                    }
                }
            }
        }
    }

    fun calculateCorrectAge(birthdateValue: String, ageValue: String): Boolean {
        val customerBirthdateText = birthdateValue.toString()

        if (customerBirthdateText.isEmpty()) return false

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val formattedBirthdate = dateFormat.parse(customerBirthdateText)
        val customerBirthdateCalendar = Calendar.getInstance().apply { time = formattedBirthdate }

        val customerAgeValueInRomanNumbers = ageValue.toString()
        val customerAgeValue = StringUtils.romanNumeralToNumber(customerAgeValueInRomanNumbers)

        val todayCalendar = Calendar.getInstance()
        var trueAge = todayCalendar.get(Calendar.YEAR) - customerBirthdateCalendar.get(Calendar.YEAR)

        if (todayCalendar.get(Calendar.DAY_OF_YEAR) < customerBirthdateCalendar.get(Calendar.DAY_OF_YEAR)) {
            trueAge--
        }

        if (customerAgeValue != trueAge) {
            viewModelScope.launch {
                _personalDataEvent.apply {
                    emit(PersonalDataEvent.ShowError("Você não tem $customerAgeValue anos, tá querendo me enganar? Faça de novo."))
                    emit(PersonalDataEvent.ResetAge)
                }
            }

            return false
        } else if (trueAge > 18) {
            return true
        }

        return false
    }
}
