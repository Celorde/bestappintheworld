package com.bestappintheworld.fragments.contact

import android.os.Handler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bestappintheworld.commons.Utils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class ContactEvent {
    data class ShowError(val message: String) : ContactEvent()
    data object CloseEmailInputDialog : ContactEvent()
    data object StartDiceAnimation : ContactEvent()
    data object StopDiceAnimation : ContactEvent()
}

data class ContactState(
    val selectedChar: String = "",
    val emailValue: String = "",
    val confirmEmailValue: String = "",
    val phoneNumber: String = "",
    val rolledNumber: String = "",
    val livesRemaining: String = "0",
    val diceFace: Int = 0
)

class ContactViewModel : ViewModel() {

    private val _contactEvent = MutableSharedFlow<ContactEvent?>()
    val contactEvent = _contactEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(ContactState())
    val uiState: StateFlow<ContactState> = _uiState

    private var emailCharacterCurrentList: MutableList<Char> = mutableListOf()
    private var numberOfLives = 0

    fun initializeEmailCharList() {
        emailCharacterCurrentList = Utils.regularCharacters.toMutableList()
    }

    fun setSelectedCharToEditText(
        input: String,
        isRemoveLetter: Boolean,
        isConfirmEmail: Boolean,
        emailInfo: String
    ): String {
        var newEmailValue = emailInfo

        if (isRemoveLetter) {
            if (emailInfo.contains(input)) {
                newEmailValue = emailInfo.replace(input, "")
            } else {
                viewModelScope.launch {
                    _contactEvent.emit(ContactEvent.ShowError("Essa letra não foi escolhida antes. Por segurança, vamos recomeçar."))
                }
                newEmailValue = ""
            }
        } else {
            newEmailValue += input
        }

        _uiState.update { state ->
            if (isConfirmEmail) {
                state.copy(confirmEmailValue = newEmailValue)
            } else {
                state.copy(emailValue = newEmailValue)
            }
        }

        return newEmailValue
    }

    fun updateGeneratedChar() {
        val selectedChar = emailCharacterCurrentList.joinToString(separator = "")
            .random().toString()

        _uiState.update { state ->
            state.copy(selectedChar = selectedChar)
        }

        emailCharacterCurrentList.remove(selectedChar.toCharArray().firstOrNull())

        if (emailCharacterCurrentList.isEmpty()) {
            viewModelScope.launch {
                _contactEvent.emit(ContactEvent.ShowError("Passou direto na letra, né meu parceiro? Tente de novo."))
            }
        }
    }

    fun startRoll(phoneNumber: String) {
        viewModelScope.launch {
            _contactEvent.emit(ContactEvent.StopDiceAnimation)
        }

        Handler().postDelayed({
            val result = Random.nextInt(12)
            val rolledNumber = (result - 1).toString()

            _uiState.update { state ->
                state.copy(diceFace = result)
            }

            viewModelScope.launch {
                _contactEvent.emit(ContactEvent.StartDiceAnimation)
            }

            if (rolledNumber == "-1") {
                if (numberOfLives == 0) {
                    _uiState.update { state ->
                        state.copy(phoneNumber = "")
                    }
                } else {
                    numberOfLives--
                }
            } else if(rolledNumber == "10") {
                numberOfLives++
            } else {
                val newNumber = phoneNumber + rolledNumber
                _uiState.update { state ->
                    state.copy(rolledNumber = newNumber)
                }
            }

            _uiState.update { state ->
                state.copy(livesRemaining = numberOfLives.toString())
            }
        }, 750)
    }
}
