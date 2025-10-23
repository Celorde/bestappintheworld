package com.bestappintheworld.commons

enum class SchoolDegree(val importanceOrder: Int, val value: String) {
    POST_GRADUATE(importanceOrder = 5, value = "Pós Graduação"),
    COLLEGE(importanceOrder = 4, value = "Ensino Superior"),
    HIGH_SCHOOL(importanceOrder = 3, value = "Ensino Médio"),
    MIDDLE_SCHOOL(importanceOrder = 2, value = "Ensino Fundamental"),
    KINDER_GARDEN(importanceOrder = 1, value = "Jardim de Infância"),
    DUMB(importanceOrder = 0, value = "Imune ao Conhecimento");

    companion object {
        fun getDegreeByValue(value: String): SchoolDegree =
            SchoolDegree.entries.find { it.value == value } ?: MIDDLE_SCHOOL
    }
}
