package com.example.bestuiintheworld.fragments.extrainfo

import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badlogic.gdx.math.MathUtils
import com.example.bestuiintheworld.commons.Utils
import com.example.bestuiintheworld.commons.formatDecimals
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val FOOT_IN_CENTIMETER = 30.48
private const val BASE_SHOW_SIZE = 46

sealed class ExtraInfoEvent {
    data object WrongStreamerSelected : ExtraInfoEvent()
    data class ShowError(val message: String) : ExtraInfoEvent()
}

data class ExtraInfoState(
    val streamerName: String = "",
    val favoriteColor: String = "",
    val confirmFavoriteColor: String = "",
    val favoritePlate: String = "",
    val favoriteDish: String = "",
    val answers: FootSizeAnswer? = null,
    val baseFootInCentimeters: String = "",
    val footErrorCount: Int = 1,
    val footSizeText: String = "",
    val shoeSizeInCentimeters: String = ""
)

class ExtraInfoViewModel : ViewModel() {

    private val _extraInfoEvent = MutableSharedFlow<ExtraInfoEvent?>()
    val extraInfoEvent = _extraInfoEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(ExtraInfoState())
    val uiState: StateFlow<ExtraInfoState> = _uiState

    private var answerList: MutableList<Answer> = mutableListOf<Answer>()

    fun validateSelectedStreamer(streamerName: String) {
        if (streamerName != "Celorde") {
            viewModelScope.launch {
                _extraInfoEvent.apply {
                    emit(ExtraInfoEvent.WrongStreamerSelected)
                    emit(ExtraInfoEvent.ShowError("Você escolheu o streamer errado. Faça de novo."))
                }
            }
        }

        _uiState.update { state ->
            state.copy(streamerName = if (streamerName != "Celorde") "" else streamerName)
        }
    }

    fun calculatePointerX(values: FloatArray): Float {
        val roll = calculateAxis(values, 2)
        val nx = (roll / 45f).coerceIn(-1f, 1f)
        return nx
    }

    fun calculatePointerY(values: FloatArray): Float {
        val pitch = calculateAxis(values, 1)
        val ny = (-pitch / 45f).coerceIn(-1f, 1f)
        return ny
    }

    fun updateSelectedColor(favoriteColor: String, isConfirm: Boolean) {
        _uiState.update { state ->
            if (isConfirm.not()) {
                state.copy(favoriteColor = favoriteColor)
            } else {
                state.copy(confirmFavoriteColor = favoriteColor)
            }
        }

        if (isConfirm) {
            val confirmColorInHex = Utils.hsvToHex(uiState.value.confirmFavoriteColor)

            if (uiState.value.favoriteColor != confirmColorInHex) {
                _uiState.update { state ->
                    state.copy(
                        favoriteColor = "",
                        confirmFavoriteColor = ""
                    )
                }

                viewModelScope.launch {
                    _extraInfoEvent.apply {
                        emit(ExtraInfoEvent.ShowError("As cores não são iguais. Por segurança, escolha novamente"))
                    }
                }
            }
        }
    }

    fun setFavoritePlate(plate: String?) {
        _uiState.update { state ->
            state.copy(
                favoritePlate = plate ?: ""
            )
        }
    }

    fun calculateFootSizeInCentimeters(shoeSize: String) {
        val shoeSizeInCentimeters = ((FOOT_IN_CENTIMETER * shoeSize.toInt()) / BASE_SHOW_SIZE)
        val footInFeet = (shoeSizeInCentimeters / FOOT_IN_CENTIMETER)

        val closeAnswer = footInFeet + generateRandomAnswer(-0.06f, 0.06f)
        val mediumWrongAnswer = footInFeet + generateRandomAnswer(-0.6f, 0.6f)
        val reallyWrongAnswer = 2.0

        answerList = mutableListOf(
            Answer(footInFeet.formatDecimals().replace(".", ","), true),
            Answer(closeAnswer.formatDecimals().replace(".", ",")),
            Answer(mediumWrongAnswer.formatDecimals().replace(".", ",")),
            Answer(reallyWrongAnswer.formatDecimals().replace(".", ","))
        )

        shuffleFootAnswers()

        _uiState.update { state ->
            state.copy(
                baseFootInCentimeters = FOOT_IN_CENTIMETER.times(
                    if (uiState.value.footErrorCount > 1) 2
                    else 1
                ).formatDecimals().replace(".", ","),
                shoeSizeInCentimeters = shoeSizeInCentimeters.times(
                    if (uiState.value.footErrorCount > 1) 2
                    else 1
                ).formatDecimals().replace(".", ",")
            )
        }
    }

    fun updateFootError() {
        _uiState.update { state ->
            state.copy(
                footSizeText = "",
                footErrorCount = uiState.value.footErrorCount + 1
            )
        }
    }

    fun setFootValue(footSize: String) {
        _uiState.update { state ->
            state.copy(
                footErrorCount = 0,
                footSizeText = footSize
            )
        }
    }

    fun shuffleFootAnswers() {
        answerList = answerList.shuffled().toMutableList()

        _uiState.update { state ->
            state.copy(
                answers = FootSizeAnswer(
                    answer1 = answerList[0],
                    answer2 = answerList[1],
                    answer3 = answerList[2],
                    answer4 = answerList[3]
                )
            )
        }
    }

    private fun calculateAxis(values: FloatArray, index: Int): Float {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, values)

        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)

        return Math.toDegrees(orientation[index].toDouble()).toFloat()
    }

    private fun generateRandomAnswer(min: Float, max: Float): Float {
        var delta = 0f
        do {
            delta = MathUtils.random(min, max)
        } while (delta == 0.0f)

        return delta
    }
}
