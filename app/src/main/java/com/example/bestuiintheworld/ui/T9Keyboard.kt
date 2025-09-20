package com.example.bestuiintheworld.ui

import android.content.Context
import android.widget.Button
import android.widget.EditText
import com.example.bestuiintheworld.R
import com.example.bestuiintheworld.databinding.T9KeyboardLayoutBinding

class T9Keyboard(
    private val context: Context,
    private val keyboardLayoutBinding: T9KeyboardLayoutBinding,
    private val editText: EditText
) {

    private var lastKeyPressed: Int? = null
    private var pressIndex = 0
    private var lastPressTime = 0L

    private var keyMapGroups = mapOf(
        0 to "ABC",
        1 to "DEF",
        2 to "GHI",
        3 to "JKL",
        4 to "MNO",
        5 to "PQRS",
        6 to "TUV",
        7 to "WXYZ"
    )

    private var keyMap = mapOf(
        R.id.key2 to keyMapGroups[0],
        R.id.key3 to keyMapGroups[1],
        R.id.key4 to keyMapGroups[2],
        R.id.key5 to keyMapGroups[3],
        R.id.key6 to keyMapGroups[4],
        R.id.key7 to keyMapGroups[5],
        R.id.key8 to keyMapGroups[6],
        R.id.key9 to keyMapGroups[7],
        R.id.key0 to " "
    )

    init {
        fillKeysTexts(true)
    }

    fun handleKeyPress(keyButton: Button) {
        val now = System.currentTimeMillis()

        if (keyButton.id == R.id.key_delete) {
            val text = editText.text
            if (text.isNotEmpty()) {
                text.delete(text.length - 1, text.length)
            }
            return
        }

        val chars = keyMap[keyButton.id]
        if (chars == null) {
            val char = when (keyButton.id) {
                R.id.key1 -> "1"
                else -> ""
            }
            editText.append(char)
            return
        }

        if (lastKeyPressed == keyButton.id && now - lastPressTime < 1000) {
            pressIndex = (pressIndex + 1) % chars.length
            val text = editText.text
            if (text.isNotEmpty()) {
                text.delete(text.length - 1, text.length)
            }
        } else {
            pressIndex = 0
            fillKeysTexts(false, keyButton.id)
        }

        editText.append(chars[pressIndex].toString())

        lastKeyPressed = keyButton.id
        lastPressTime = now
    }

    private fun fillKeysTexts(isFirstTime: Boolean, buttonId: Int = 0) {
        var newMappedKeys: Map<Int, String>? = null

        if (!isFirstTime) {
            val keyMapIndexList = keyMapGroups.keys.toMutableList()
            val keyMappedIndex = keyMap[buttonId]

            val keyIndex = keyMapGroups.entries.find { it.value == keyMappedIndex }?.key ?: 0
            val valueToBeAdded = keyMapIndexList.indexOf(keyIndex)

            var newShuffledList = keyMapIndexList.apply { removeAt(valueToBeAdded) }.shuffled()
            newShuffledList = newShuffledList.toMutableList().apply { add(valueToBeAdded, keyIndex) }
            keyMapIndexList.clear()
            keyMapIndexList.addAll(newShuffledList)

            newMappedKeys = applyNewKeySort(newShuffledList)
        }

        newMappedKeys?.let { keyMapGroups = it }

        keyboardLayoutBinding.key2.text = context.getString(R.string.key2, keyMapGroups[0])
        keyboardLayoutBinding.key3.text = context.getString(R.string.key3, keyMapGroups[1])
        keyboardLayoutBinding.key4.text = context.getString(R.string.key4, keyMapGroups[2])
        keyboardLayoutBinding.key5.text = context.getString(R.string.key5, keyMapGroups[3])
        keyboardLayoutBinding.key6.text = context.getString(R.string.key6, keyMapGroups[4])
        keyboardLayoutBinding.key7.text = context.getString(R.string.key7, keyMapGroups[5])
        keyboardLayoutBinding.key8.text = context.getString(R.string.key8, keyMapGroups[6])
        keyboardLayoutBinding.key9.text = context.getString(R.string.key9, keyMapGroups[7])

        keyMap = mapOf(
            R.id.key2 to keyMapGroups[0],
            R.id.key3 to keyMapGroups[1],
            R.id.key4 to keyMapGroups[2],
            R.id.key5 to keyMapGroups[3],
            R.id.key6 to keyMapGroups[4],
            R.id.key7 to keyMapGroups[5],
            R.id.key8 to keyMapGroups[6],
            R.id.key9 to keyMapGroups[7],
            R.id.key0 to " "
        )
    }

    private fun applyNewKeySort(newSortedList: List<Int>): Map<Int, String> {
        val newMapValues = keyMapGroups.mapValues { (_, value) -> value.toList().shuffled().joinToString("") }
        keyMapGroups = newMapValues

        val keyItems = keyMapGroups.values.toList()
        return newSortedList.zip(keyItems).toMap()
    }
}