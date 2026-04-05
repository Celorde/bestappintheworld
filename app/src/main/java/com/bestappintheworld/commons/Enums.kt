package com.bestappintheworld.commons

import com.bestappintheworld.R

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

enum class ItemsCategory {
    GLASSES,
    CLOTHES,
    HAT,
    BEARD,
    ACCESSORY
}

enum class ProfessionItems(val value: String, val drawableId: Int) {
    COAT("casaco", R.drawable.ic_blue_coat),
    BEARD("barba", R.drawable.ic_beard),
    GLASSES("oculos", R.drawable.ic_glasses),
    HAT("chapeu", R.drawable.ic_red_hat),
    ACCESSORY("acessorio", R.drawable.ic_purse),
    JOYSTICK("controle", R.drawable.ic_joystick),
    HEADSET("headset", R.drawable.ic_headset);

    companion object {
        fun getProfessionByValue(value: String): ProfessionItems =
            ProfessionItems.entries.find { it.value == value } ?: COAT
    }
}
