package com.example.bestuiintheworld.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.bestuiintheworld.R
import com.example.bestuiintheworld.databinding.IngredientViewBinding

class IngredientView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    ingredientIndex: Int = 0
) : ConstraintLayout(context, attrs) {

    val binding = IngredientViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.ingredientTextView.text = context.getString(R.string.ingredient_index, ingredientIndex)
    }
}
