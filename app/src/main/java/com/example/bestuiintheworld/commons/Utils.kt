package com.example.bestuiintheworld.commons

import android.graphics.Color
import com.example.bestuiintheworld.R

object Utils {
    val regularCharacters = ('a'..'z') + ('0'..'9') + '@' + '_' + '-' + '.'

    val letters =
        (('a'..'z') + ('A'..'Z') + listOf(
            'á', 'à', 'â', 'ã', 'ä', 'é', 'è', 'ê', 'ë',
            'í', 'ì', 'î', 'ï', 'ó', 'ò', 'ô', 'õ', 'ö',
            'ú', 'ù', 'û', 'ü', 'ç', 'ñ', 'Á', 'À', 'Â', 'Ã', 'Ä',
            'É', 'È', 'Ê', 'Ë', 'Í', 'Ì', 'Î', 'Ï', 'Ó', 'Ò', 'Ô', 'Õ', 'Ö',
            'Ú', 'Ù', 'Û', 'Ü', 'Ç', 'Ñ'
        )).map { it.toString() } + (StringUtils.KeyboardCommands.SPACE.value) + (StringUtils.KeyboardCommands.BACKSPACE.value)

    val romanNumerals = listOf('I', 'V', 'X', 'L', 'C', 'D', 'M')

    val addressCodeCharacters = ('0'..'9') + '-'

    val bestStreamersOfBrazil = listOf("alanzoka", "Celorde")

    val sexFrequency = listOf("1 vez", "2 vezes", "7 vezes", "45 vezes", "Nenhuma")

    val maritalStatusOptions = listOf("Solteiro", "Casado", "Divorciado", "Viúvo", "Corno", "Ficando", "Curti o Story")

    val referencePlaces = listOf("um supermercado", "uma igreja", "uma caixa d'água", "uma escola", "um prédio abandonado", "um ponto de ônibus", "uma lojinha", "uma pracinha", "uma floresta", "um zoológico", "um castelo", "uma biqueira", "uma borracharia", "uma casa assombrada", "uma frutaria do japonês", "um bar do Zé", "um posto Ipiranga", "uma padoca", "um cemitério", "um coiso lá", "uma árvore grandona", "um OXXO")

    val diceFaces = arrayOf(
        R.drawable.dice_1,
        R.drawable.dice_2,
        R.drawable.dice_3,
        R.drawable.dice_4,
        R.drawable.dice_5,
        R.drawable.dice_6,
        R.drawable.dice_7,
        R.drawable.dice_8,
        R.drawable.dice_9,
        R.drawable.dice_10,
        R.drawable.dice_11,
        R.drawable.dice_12
    )

    val plateList = mapOf<Int, String>(
        R.drawable.plate_1 to "Pratinho de aniversário",
        R.drawable.plate_2 to "Prato de microondas",
        R.drawable.plate_3 to "Prato de bateria",
        R.drawable.plate_4 to "Prato padrão",
        R.drawable.plate_5 to "Prato de merenda da escola",
        R.drawable.plate_6 to "Queijo prato",
        R.drawable.plate_7 to "Prato de pedra",
        R.drawable.plate_8 to "Prato de cadeia",
        R.drawable.plate_9 to "Prato de tia",
        R.drawable.plate_10 to "Prato de papelão",
        R.drawable.plate_11 to "Prato marrom da Duralex",
        R.drawable.plate_12 to "Prato de madeira",
        R.drawable.platte_13 to "Lucas Pratto",
        R.drawable.plate_14 to "Cidade de Prato"
    )

    val ingredientBlacklist = listOf(
        "mostarda",
        "chuchu",
        "abacate",
        "berinjela",
        "jiló",
        "beterraba",
        "fígado",
        "alcaparra",
        "sardinha",
        "anchova",
        "pistache",
        "adoçante",
        "whey",
        "favo de baunilha",
        "abobrinha"
    )

    val spiceList = listOf(
        "sal",
        "pimenta",
        "cominho",
        "alho",
        "cebola",
        "coentro",
        "açúcar",
        "cheiro verde",
        "orégano",
        "manjericão",
        "açafrão",
        "curry",
        "canela",
        "cravo",
        "baunilha",
        "noz moscada",
        "coco ralado"
    )

    fun hsvToHex(hsvString: String): String {
        val regex = Regex("""H: (\d+(?:\.\d+)?)[, ]*S: (\d+(?:\.\d+)?)%[, ]*V: (\d+(?:\.\d+)?)%""", RegexOption.IGNORE_CASE)
        val match = regex.find(hsvString)

        if (match != null) {
            val (hStr, sStr, vStr) = match.destructured

            val h = hStr.toFloat().coerceIn(0f, 360f)
            val s = (sStr.toFloat() / 100f).coerceIn(0f, 1f)
            val v = (vStr.toFloat() / 100f).coerceIn(0f, 1f)

            val colorInt = Color.HSVToColor(floatArrayOf(h, s, v))
            val r = Color.red(colorInt)
            val g = Color.green(colorInt)
            val b = Color.blue(colorInt)

            return String.format("#%02X%02X%02X", r, g, b)
        } else {
            throw IllegalArgumentException("Formato HSV inválido. Use: H:120, S:66.5%, V:74.9%")
        }
    }

    fun numberToRomanNumeral(number: Int): String {
        val romanNumerals = listOf(
            1000 to "M",
            900 to "CM",
            500 to "D",
            400 to "CD",
            100 to "C",
            90 to "XC",
            50 to "L",
            40 to "XL",
            10 to "X",
            9 to "IX",
            5 to "V",
            4 to "IV",
            1 to "I"
        )

        var mutableNumber = number
        val result = StringBuilder()

        for ((value, romanNumeral) in romanNumerals) {
            while (mutableNumber >= value) {
                result.append(romanNumeral)
                mutableNumber -= value
            }
        }

        return result.toString()
    }
}
