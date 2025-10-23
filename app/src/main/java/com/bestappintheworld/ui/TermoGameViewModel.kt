package com.bestappintheworld.ui

import androidx.lifecycle.ViewModel

data class LetterResult(val letter: Char, val status: LetterStatus)
enum class GameStatus { WIN, LOSE, PLAYING }
data class GameResult(var message: String, var status: GameStatus, var attempts: Int = 0)
enum class LetterStatus { CORRECT, PRESENT, ABSENT }

val wordList = listOf(
    "CASAS", "BOLAS", "PAPEL", "CARRO", "FESTA",
    "TEMPO", "FLORE", "GRATO", "VIDRO", "SORTE"
)

class TermoGameViewModel : ViewModel() {

    private val targetWord = wordList.random()
    val guesses = mutableListOf<List<LetterResult>>()
    //    var message: String = ""
    val gameResult = GameResult("", GameStatus.PLAYING)

    fun checkGuess(guess: String): List<LetterResult>? {
        if (guess.length != 5) {
            gameResult.message = "A palavra deve ter 5 letras!"
            return null
        }

        if (!wordList.contains(guess)) {
            gameResult.message = "Palavra não reconhecida!"
            return null
        }

        val result = evaluateGuess(guess, targetWord)
        guesses.add(result)
        gameResult.attempts = guesses.size

        when {
            guess == targetWord -> {
                gameResult.apply {
                    message = "🎉 Você acertou! A palavra era $targetWord"
                    status = GameStatus.WIN
                }
            }
            guesses.size >= 5 -> {
                gameResult.apply {
                    message = "😢 Fim de jogo! A palavra era $targetWord"
                    status = GameStatus.LOSE
                }
                "😢 Fim de jogo! A palavra era $targetWord"
            }
            else -> {
                gameResult.apply {
                    message = ""
                    status = GameStatus.PLAYING
                }
            }
        }

        gameResult.message = when {
            guess == targetWord -> "🎉 Você acertou! A palavra era $targetWord"
            guesses.size >= 6 -> "😢 Fim de jogo! A palavra era $targetWord"
            else -> ""
        }

        return result
    }

    private fun evaluateGuess(guess: String, target: String): List<LetterResult> {
        val result = mutableListOf<LetterResult>()
        for (i in guess.indices) {
            val letter = guess[i]
            val status = when (letter) {
                target[i] -> LetterStatus.CORRECT
                in target -> LetterStatus.PRESENT
                else -> LetterStatus.ABSENT
            }
            result.add(LetterResult(letter, status))
        }
        return result
    }
}
