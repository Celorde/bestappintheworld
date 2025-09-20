package com.example.bestuiintheworld.fragments.password

import android.icu.util.Calendar
import androidx.lifecycle.ViewModel
import com.example.bestuiintheworld.commons.Utils
import com.example.bestuiintheworld.commons.containsEmoji
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update

sealed class PasswordEvent {
    data class ShowError(val message: String) : PasswordEvent()
}

data class PasswordState(
    val isPasswordValid: Boolean = false,
    val errorMessage: String = ""
)

class PasswordViewModel : ViewModel() {

    private val _passwordEvent = MutableSharedFlow<PasswordEvent?>()
    val passwordEvent = _passwordEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(PasswordState())
    val uiState: StateFlow<PasswordState> = _uiState

    private val passwordRules: MutableList<PasswordRule> = mutableListOf(
        PasswordRule("Mínimo de 5 caracteres") { it.length >= 5 },
        PasswordRule("Precisa apontar para cima") { it.contains("☝\uFE0F") },
        PasswordRule("Precisa conter ao menos 1 número") { it.any { c -> c.isDigit() } },
        PasswordRule("Precisa conter ao menos 1 letra maiúscula") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter 1 emoji") { it.containsEmoji() },
        PasswordRule("Precisa conter o dia de hoje em algarismos romanos") { it.contains(todayInRomanNumeral()) },
        PasswordRule("Precisa conter os primeiros números do número PI") { it.contains("314") },

        PasswordRule("A senha não pode conter sua data de nascimento") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter o seu Nickname") { it.any { c -> c.isUpperCase() } }, //TODO
        PasswordRule("Não pode ter dedo do meio, seu sem educação") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter a bandeira dos Países Baixos") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Não pode conter a bandeira do Brasil") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter o ano que o Brasil ganhou o Hexa") { it.contains("2026") },
        PasswordRule("Precisa conter todas as vogais maiúsculas") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter o código de telefone do seu país") { it.any { c -> c.isUpperCase() } },
        PasswordRule("O emoji precisa estar na quinta posição") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter um feitiço de Harry Potter") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter o nome do pássaro do Frajola") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter o número de caracteres que a senha possui") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter a versão do Android do seu aparelho") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter quanto de bateria restante possui no seu aparelho") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter um Pokémon da 1ª geração") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter o nome do criador do App") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Celorde não é um nome, é um Nickname") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter ao menos 1 elemento da tabela periódica") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Não pode conter Cu") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter menos que 30 caracteres") { it.any { c -> c.isUpperCase() } },
        PasswordRule("A soma dos números contidos na senha precisa ser um número primo") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter a sua idade em 2030") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter a letra C quatro vezes seguidas") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter a letra U com trema") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter um sistema operacional") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter uma princesa da Disney") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter uma onomatopeia") { it.any { c -> c.isUpperCase() } },
        PasswordRule("Precisa conter ; no final da senha") { it.any { c -> c.isUpperCase() } }
    )

    fun validatePassword(password: String) {
        val errorList = passwordRules.filter { !it.validation(password) }
            .map { it.errorMessage }

        //TODO se digitar a palavra OLHO, mostra a senha

        _uiState.update { state ->
            state.copy(
                isPasswordValid = errorList.isNotEmpty(),
                errorMessage = if (errorList.isEmpty()) "" else errorList[0]
            )
        }
    }

    private fun todayInRomanNumeral(): String {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)

        return Utils.numberToRomanNumeral(today)
    }
}
