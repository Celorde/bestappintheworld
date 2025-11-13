package com.bestappintheworld.fragments.security

import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow


sealed class CaptchaEvent {
    data class ShowHideError(
        val errorTextView: TextView,
        val message: String
    ) : CaptchaEvent()
    data class ShowHidePassword(
        val editText: EditText,
        val isToHide: Boolean
    ): CaptchaEvent()
}

data class CaptchaState(
    val isPasswordValid: Boolean = false
//    val errorMessage: String = ""
)

class CaptchaViewModel : ViewModel() {

    private val _passwordEvent = MutableSharedFlow<CaptchaEvent?>()
    val passwordEvent = _passwordEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(CaptchaState())
    val uiState: StateFlow<CaptchaState> = _uiState
}
