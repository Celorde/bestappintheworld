package com.bestappintheworld.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.bestappintheworld.commons.SchoolDegree
import com.bestappintheworld.databinding.DialogTermoGameBinding
import androidx.core.graphics.toColorInt

enum class FinalDegree { CORRECT, BELOW, ABOVE }

data class FinalResult (val schoolDegree: SchoolDegree, val finalDegree: FinalDegree)

class TermoGame @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    val binding = DialogTermoGameBinding.inflate(LayoutInflater.from(context), this, true)

    lateinit var editText: EditText
    lateinit var selectedSchoolDegree: SchoolDegree
    lateinit var callback: () -> Unit

    private val termoGameViewModel = TermoGameViewModel()

    init {
        binding.apply {
            btnSubmit.setOnClickListener { handleGuess() }
            etGuess.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    handleGuess()
                    true
                } else false
            }
        }
    }

    fun setData(editText: EditText, schoolDegree: SchoolDegree, callback: () -> Unit) {
        this.editText = editText
        this.selectedSchoolDegree = schoolDegree
        this.callback = callback
    }

    private fun handleGuess() {
        binding.apply {
            val guess = etGuess.text.toString().uppercase().trim()
            val result = termoGameViewModel.checkGuess(guess)
            etGuess.text.clear()

            result?.let {
                addGuessToGrid(it)
            }

            tvMessage.text = termoGameViewModel.gameResult.message

            when(termoGameViewModel.gameResult.status) {
                GameStatus.WIN -> {
                    val finalResult = calculateFinalResult(
                        termoGameViewModel.gameResult.attempts
                    )

                    Toast.makeText(
                        context,
                        if (finalResult.finalDegree == FinalDegree.ABOVE) {
                            "Caramba! Você é mais inteligente do que pensa. Sua escolaridade na verdade é: ${finalResult.schoolDegree.value}"
                        } else if(finalResult.finalDegree == FinalDegree.BELOW) {
                            "Mentiroso! Você é mais burro do que pensa. Sua escolaridade na verdade é: ${finalResult.schoolDegree.value}"
                        } else {
                            "Parabéns! Você realmente possui a escolaridade ${finalResult.schoolDegree.value}"
                        },
                        Toast.LENGTH_LONG
                    ).show()

                    editText.setText(finalResult.schoolDegree.value)

                    callback.invoke()

                }
                GameStatus.LOSE -> {
                    editText.setText(
                        SchoolDegree.DUMB.value
                    )

                    callback.invoke()
                }
                else -> { } //do nothing
            }
        }
    }

    private fun addGuessToGrid(result: List<LetterResult>) {
        for (letterResult in result) {
            val textView = TextView(context)
            textView.text = letterResult.letter.toString()
            textView.textSize = 22f
            textView.setTextColor(Color.WHITE)
            textView.textAlignment = TEXT_ALIGNMENT_CENTER
            textView.setPadding(8, 8, 8, 8)
            textView.width = 100
            textView.height = 100
            textView.setBackgroundColor(getColorForStatus(letterResult.status))
            val params = GridLayout.LayoutParams()
            params.setMargins(4, 4, 4, 4)
            binding.gridGuesses.addView(textView, params)
        }
    }

    private fun getColorForStatus(status: LetterStatus): Int {
        return when (status) {
            LetterStatus.CORRECT -> "#6AAA64".toColorInt() // Verde
            LetterStatus.PRESENT -> "#C9B458".toColorInt() // Amarelo
            LetterStatus.ABSENT -> "#787C7E".toColorInt() // Cinza
        }
    }

    private fun calculateFinalResult(guesses: Int): FinalResult {
        val schoolDegree = when(guesses) {
            1 -> SchoolDegree.POST_GRADUATE
            2 -> SchoolDegree.COLLEGE
            3 -> SchoolDegree.HIGH_SCHOOL
            4 -> SchoolDegree.MIDDLE_SCHOOL
            5 -> SchoolDegree.KINDER_GARDEN
            else -> SchoolDegree.DUMB
        }

        return FinalResult(
            schoolDegree = schoolDegree,
            finalDegree = verifyIfUserIsSmarterOrDumber(schoolDegree)
        )
    }

    private fun verifyIfUserIsSmarterOrDumber(realSchoolDegree: SchoolDegree): FinalDegree {
        return when {
            selectedSchoolDegree.importanceOrder < realSchoolDegree.importanceOrder -> FinalDegree.ABOVE
            selectedSchoolDegree.importanceOrder > realSchoolDegree.importanceOrder -> FinalDegree.BELOW
            else -> FinalDegree.CORRECT
        }
    }
}
