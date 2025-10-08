package com.example.bestappintheworld.fragments.password

import android.icu.text.BreakIterator
import android.icu.util.Calendar
import android.os.Build
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bestappintheworld.commons.ContextUtils
import com.bestappintheworld.commons.Emojis
import com.example.bestappintheworld.commons.StringUtils
import com.example.bestappintheworld.commons.Utils
import com.example.bestappintheworld.commons.containsEmoji
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Locale

sealed class PasswordEvent {
    data class ShowHideError(
        val errorTextView: TextView,
        val message: String
    ) : PasswordEvent()
    data class ShowHidePassword(
        val editText: EditText,
        val isToHide: Boolean
    ): PasswordEvent()
}

data class PasswordState(
    val isPasswordValid: Boolean = false
//    val errorMessage: String = ""
)

class PasswordViewModel : ViewModel() {

    private val _passwordEvent = MutableSharedFlow<PasswordEvent?>()
    val passwordEvent = _passwordEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(PasswordState())
    val uiState: StateFlow<PasswordState> = _uiState

    private val passwordRules: MutableList<PasswordRule> = mutableListOf(
        PasswordRule("Não pode ter dedo do meio, seu sem educação") { it.contains(Emojis.MIDDLE_FINGER.unicode).not() },
        PasswordRule("Mínimo de 5 caracteres") { it.length >= 5 },
        PasswordRule("Precisa conter ao menos 1 número") { it.any { c -> c.isDigit() } },
        PasswordRule("Precisa conter ao menos 1 letra maiúscula") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter 1 emoji") { it.containsEmoji() },
        PasswordRule("Precisa conter o dia de hoje em algarismos romanos") { it.contains(todayInRomanNumeral()) },
        PasswordRule("Precisa conter quanto de bateria restante possui no seu aparelho") { it.contains(ContextUtils.getInstance()?.getCurrentBatteryLevel().toString()) },
        PasswordRule("Precisa conter os primeiros números do número PI") { it.contains("314") },
        PasswordRule("Precisa apontar para cima") { it.contains(Emojis.POINT_UP.unicode) },
        PasswordRule("Precisa conter o ano que o Brasil ganhou o Hexa") { it.contains("2026") },
        PasswordRule("Precisa conter a bandeira dos Países Baixos") { it.contains(Emojis.NETHERLANDS.unicode) },
        PasswordRule("Não pode conter a bandeira do Brasil") { it.contains(Emojis.BRAZIL.unicode).not() },
        PasswordRule("Precisa conter todas as vogais maiúsculas") { it.contains("AEIOU") }, //TODO
        PasswordRule("Precisa conter o código de telefone do seu país") { it.contains("+55") },
        PasswordRule("A bandeira dos países baixos precisa estar na quinta posição") { it.indexOf(Emojis.NETHERLANDS.unicode) == 4 },
        PasswordRule("Precisa conter um feitiço de Harry Potter") { Utils.harryPotterSpellList.any { spell -> it.lowercase().contains(spell.lowercase()) } },
        PasswordRule("Precisa conter o número de caracteres que a senha possui") { it.contains(calculateCharCount(it).toString()) },
        PasswordRule("Precisa conter a versão do Android do seu aparelho") { it.contains(Build.VERSION.RELEASE) },
        PasswordRule("Celorde não é um nome, é um Nickname") { it.contains("Celorde").not() },
        PasswordRule("Precisa conter o nome do criador do App") { it.contains("Marcelo") },
        PasswordRule("Precisa conter menos que 40 caracteres") { calculateCharCount(it) <= 40 },
        PasswordRule("A soma dos números contidos na senha precisa ser um número primo") { Utils.isCousinNumber(StringUtils.selectOnlyNumbersFromText(it)) },
        PasswordRule("Precisa conter ; no final da senha") { it[it.length - 1] == ';' },

//        PasswordRule("A senha não pode conter sua data de nascimento") { it.any { c -> c.isUpperCase() } }, //TODO
//        PasswordRule("Precisa conter o seu Nickname") { it.any { c -> c.isUpperCase() } }, //TODO
//        PasswordRule("Precisa conter a sua idade em 2030") { it.any { c -> c.isUpperCase() } }, //TODO

        //        PasswordRule("Precisa conter o nome do pássaro do Frajola") { it.contains("Piupiu") },
//        PasswordRule("Precisa conter a letra C quatro vezes seguidas") { it.contains("CCCC") },
//        PasswordRule("Precisa conter um Pokémon da 1ª geração") { it.any { c -> c.isUpperCase() } }, //TODO
//        PasswordRule("Precisa conter a letra U com trema") { it.lowercase().contains("ü") },
//        PasswordRule("Precisa conter um sistema operacional") { it.any { c -> c.isUpperCase() } }, //TODO
//        PasswordRule("Precisa conter uma princesa da Disney") { it.any { c -> c.isUpperCase() } }, //TODO
//        PasswordRule("Precisa conter uma onomatopeia") { it.any { c -> c.isUpperCase() } }, //TODO

        //TODO O volume do celular precisa estar em 65
    )

    fun validatePassword(editText: EditText, errorTextView: TextView, password: String) {
        if (password.isNotEmpty()) {
            val errorList = passwordRules.filter { !it.validation(password) }
                .map { it.errorMessage }

            viewModelScope.launch {
                if (password.contains("olho".lowercase())) {
                    _passwordEvent.emit(
                        PasswordEvent.ShowHidePassword(
                            editText = editText,
                            isToHide = false
                        )
                    )
                } else {
                    _passwordEvent.emit(
                        PasswordEvent.ShowHidePassword(
                            editText = editText,
                            isToHide = true
                        )
                    )
                }

                _passwordEvent.emit(
                    PasswordEvent.ShowHideError(
                        errorTextView = errorTextView,
                        message = if (errorList.isEmpty()) "" else errorList[0]
                    )
                )
            }
        }
    }

    private fun todayInRomanNumeral(): String {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)

        return Utils.numberToRomanNumeral(today)
    }

    private fun calculateCharCount(text: String): Int {
        var charCount = 0

        splitGraphemes(text).forEach { _ ->
            charCount++
        }

        return charCount
    }

    private fun splitGraphemes(text: String): List<String> {
        val it = BreakIterator.getCharacterInstance(Locale.getDefault())
        it.setText(text)
        val graphemes = mutableListOf<String>()
        var start = it.first()
        var end = it.next()

        while (end != BreakIterator.DONE) {
            graphemes.add(text.substring(start, end))
            start = end
            end = it.next()
        }

        return graphemes
    }
}
