package com.bestappintheworld.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText


class CustomEditText(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs) {

    private var onPasteEvent: (() -> Unit)? = null

    fun setPasteEvent(block: () -> Unit) {
        onPasteEvent = block
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        if (id == android.R.id.paste) {
            onPasteEvent?.invoke()
        }

        return true
    }
}
