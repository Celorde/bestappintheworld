package com.bestappintheworld.commons

import java.text.Normalizer
import kotlin.text.iterator

object StringUtils {
    enum class KeyboardCommands(val value: String) {
        SPACE("Espaço"),
        BACKSPACE("Backspace")
    }

    fun String.normalizeWord(): String {
        val normalizer = Normalizer.normalize(this, Normalizer.Form.NFD)
        return normalizer.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }

    fun isRomanValid(romanNumber: String): Boolean {
        val regex = Regex("^M{0,3}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$")
        return regex.matches(romanNumber.uppercase())
    }

    fun romanNumeralToNumber(romanNumber: String): Int {
        val romanValues = mapOf(
            'I' to 1,
            'V' to 5,
            'X' to 10,
            'L' to 50,
            'C' to 100,
            'D' to 500,
            'M' to 1000
        )

        var result = 0
        var lastValue = 0

        for (romanChar in romanNumber.uppercase().reversed()) {
            val romanValue = romanValues[romanChar] ?: 0

            if (romanValue < lastValue) {
                result -= romanValue
            } else {
                result += romanValue
                lastValue = romanValue
            }
        }

        return result
    }

    fun selectOnlyNumbersFromText(text: String): Int {
        return text.filter { it.isDigit() }
            .map { it.digitToInt() }
            .sum()
    }
}
