package com.example.bestappintheworld.fragments.address

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bestappintheworld.commons.BrazilUtils.BrazilianMapInteraction
import com.example.bestappintheworld.commons.BrazilUtils.BrazilianStates
import com.example.bestappintheworld.commons.Utils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AddressEvent {
    data class ShowError(val message: String) : AddressEvent()
}

data class AddressState(
    val zipCode: String = "",
    val state: String = ""
)

class AddressViewModel : ViewModel() {

    private val _addressEvent = MutableSharedFlow<AddressEvent?>()
    val addressEvent = _addressEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(AddressState())
    val uiState: StateFlow<AddressState> = _uiState

    private var addressCodeHasClickedOk = false

    fun startAddressCodeRoulette() {
        addressCodeHasClickedOk = false
        var index = 0
        var rouletteNumberDelay = 1000L
        val delayReductionRatio = 0.9
        val handler = Handler(Looper.getMainLooper())
        var addressCodeCharacters: MutableList<Char> = Utils.addressCodeCharacters.toMutableList()

        val loopRunnable = object : Runnable {
            override fun run() {
                if (!addressCodeHasClickedOk) {
                    val currentValue = uiState.value.zipCode
                    var text = ""
                    index++

                    if (index <= 11) {
                        val selectedCharacter = addressCodeCharacters.joinToString(separator = "")
                            .random().toString()

                        addressCodeCharacters.remove(selectedCharacter.toCharArray().firstOrNull())

                        text = if (index - 1 == 0) selectedCharacter + currentValue
                        else selectedCharacter + currentValue.substring(1, currentValue.length)

                        _uiState.update { state ->
                            state.copy(zipCode = text)
                        }

                        handler.postDelayed(this, rouletteNumberDelay)
                        rouletteNumberDelay =
                            (rouletteNumberDelay * delayReductionRatio).toLong()
                    } else {
                        _uiState.update { state ->
                            state.copy(zipCode = "")
                        }
                    }
                } else {
                    val currentValue = uiState.value.zipCode

                    if (currentValue.first() == '-' && currentValue.length != 4) {
                        _uiState.update { state ->
                            state.copy(zipCode = "")
                        }
                        viewModelScope.launch {
                            _addressEvent.emit(AddressEvent.ShowError("Você errou o lugar do hífen. Por segurança, repita o valor."))
                        }
                    } else {
                        if (currentValue.length != 9) {
                            startAddressCodeRoulette()
                        } else if (!currentValue.contains('-')) {
                            _uiState.update { state ->
                                state.copy(zipCode = "")
                            }
                            viewModelScope.launch {
                                _addressEvent.emit(AddressEvent.ShowError("Você esqueceu o hífen. Por segurança, repita o valor."))
                            }
                        }
                    }
                }
            }
        }

        handler.post(loopRunnable)
    }

    fun confirmZipCodeDigit() {
        addressCodeHasClickedOk = true
    }

    fun getSelectedState(event: MotionEvent, mapBitmap: Bitmap, mapMatrix: Matrix) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x.toInt()
            val y = event.y.toInt()

            val clickedState = BrazilianMapInteraction
                .calculateStateClick(x, y, mapBitmap, mapMatrix)

            val textToBeShowed = when(clickedState) {
                BrazilianStates.NONE -> "Que pasa manito! País errado."
                BrazilianStates.DIVIDER -> "Você clicou em uma divisa. Tente novamente."
                else -> clickedState.value
            }

            if (clickedState != BrazilianStates.NONE && clickedState != BrazilianStates.DIVIDER) {
                _uiState.update { state ->
                    state.copy(state = textToBeShowed)
                }
            } else {
                val textToBeShowed = if (clickedState == BrazilianStates.NONE) {
                    "Que pasa manito! País errado."
                } else {
                    "Você clicou em uma divisa. Tente novamente."
                }

                viewModelScope.launch {
                    _addressEvent.emit(AddressEvent.ShowError(textToBeShowed))
                }
            }
        }
        true
    }
}
