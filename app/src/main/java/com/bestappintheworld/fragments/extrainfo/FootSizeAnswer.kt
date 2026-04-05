package com.bestappintheworld.fragments.extrainfo

data class FootSizeAnswer(
    val answer1: Answer?,
    val answer2: Answer?,
    val answer3: Answer?,
    val answer4: Answer?
)

data class Answer(
    val text: String = "",
    val isCorrect: Boolean = false
)
