package com.bestappintheworld.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.DragEvent
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.bestappintheworld.R
import com.bestappintheworld.commons.ItemsCategory
import com.bestappintheworld.commons.ProfessionItems
import com.bestappintheworld.databinding.AvatarDragAndDropBinding


class AvatarDragAndDrop @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    val binding = AvatarDragAndDropBinding.inflate(LayoutInflater.from(context), this, true)

    val selectedItems: ArrayList<ProfessionItems> = arrayListOf()

    init {
        addItems()
        configureDropZone()
    }

    private fun addItems() {
        val items = listOf(
            ProfessionItem(icon = ProfessionItems.COAT, category = ItemsCategory.CLOTHES),
            ProfessionItem(icon = ProfessionItems.BEARD, category = ItemsCategory.BEARD),
            ProfessionItem(icon = ProfessionItems.GLASSES, category = ItemsCategory.GLASSES),
            ProfessionItem(icon = ProfessionItems.HAT, category = ItemsCategory.HAT),
            ProfessionItem(icon = ProfessionItems.ACCESSORY, category = ItemsCategory.ACCESSORY)
        )

        for (item in items) {
            val itemView = ImageView(context).apply {
                setImageResource(item.icon.drawableId)
                layoutParams = LinearLayout.LayoutParams(150, 150).apply {
                    setMargins(16, 0, 16, 0)
                }

                setOnLongClickListener {
                    val data = item.icon.value
                    val shadow = DragShadowBuilder(this)
                    startDragAndDrop(null, shadow, data, 0)
                    true
                }
            }
            binding.layoutItens.addView(itemView)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configureDropZone() {
        binding.frameAvatar.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val itemType = event.localState as String
                    applyItemToAvatar(itemType)
                }
            }
            true
        }
    }

    private fun applyItemToAvatar(itemType: String) {
        val imageMap = mapOf(
            "casaco" to R.id.imgCasaco,
            "barba" to R.id.imgBarba,
            "oculos" to R.id.imgOculos,
            "chapeu" to R.id.imgChapeu,
            "acessorio" to R.id.imgAcessorio
        )

        val selectedItem = ProfessionItems.getProfessionByValue(itemType)
        selectedItems.add(selectedItem)

        val imgView = findViewById<ImageView>(imageMap[itemType] ?: return)
        imgView.setImageResource(selectedItem.drawableId)
    }

    fun parseSelectedProfession(): String {
        val itemCombination: Map<List<ProfessionItems>, String> = mapOf(
            listOf(ProfessionItems.COAT, ProfessionItems.GLASSES) to "Faria Limer",
            listOf(ProfessionItems.BEARD, ProfessionItems.GLASSES) to "Dentista"
        )

        val itemKey = selectedItems.sorted()

        return itemCombination[itemKey] ?: "Combinação desconhecida"
    }
}
