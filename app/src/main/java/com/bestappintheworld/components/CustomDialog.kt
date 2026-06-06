package com.bestappintheworld.components

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.view.isVisible
import com.bestappintheworld.databinding.LayoutCustomDialogBinding

class CustomDialog(context: Context) : Dialog(context) {

    private lateinit var binding: LayoutCustomDialogBinding

    private var title: String? = null
    private var message: String? = null
    private var customView: View? = null
    
    private var items: Array<String>? = null
    private var onItemClickListener: ((Int) -> Unit)? = null
    
    private var positiveButtonText: String? = null
    private var positiveButtonAction: (() -> Unit)? = null
    
    private var negativeButtonText: String? = null
    private var negativeButtonAction: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = LayoutCustomDialogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        setupUI()
    }

    private fun setupUI() {
        binding.apply {
            title?.let {
                dialogTitle.text = it
                dialogTitle.isVisible = true
            }

            message?.let {
                dialogMessage.text = it
                dialogMessage.isVisible = true
            }

            items?.let { itemList ->
                val listView = ListView(context)
                listView.adapter = object : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, itemList) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getView(position, convertView, parent) as TextView
                        view.setTextColor(context.getColor(android.R.color.white))
                        return view
                    }
                }
                listView.setOnItemClickListener { _, _, position, _ ->
                    onItemClickListener?.invoke(position)
                    dismiss()
                }
                customViewContainer.addView(listView)
                customViewContainer.isVisible = true
            }

            customView?.let {
                applyWhiteToRadioButtons(it)
                customViewContainer.addView(it)
                customViewContainer.isVisible = true
            }

            positiveButtonText?.let { text ->
                positiveButton.text = text
                positiveButton.isVisible = true
                positiveButton.setOnClickListener {
                    positiveButtonAction?.invoke()
                    dismiss()
                }
            }

            negativeButtonText?.let { text ->
                negativeButton.text = text
                negativeButton.isVisible = true
                negativeButton.setOnClickListener {
                    negativeButtonAction?.invoke()
                    dismiss()
                }
            }
            
            buttonContainer.isVisible = positiveButton.isVisible || negativeButton.isVisible
        }
    }

    private fun applyWhiteToRadioButtons(view: View) {
        if (view is RadioButton) {
            view.setTextColor(context.getColor(android.R.color.white))
            view.buttonTintList = ColorStateList.valueOf(context.getColor(android.R.color.white))
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyWhiteToRadioButtons(view.getChildAt(i))
            }
        }
    }

    class Builder(private val context: Context) {
        private val dialog = CustomDialog(context)

        fun setTitle(title: String) = apply { dialog.title = title }
        fun setMessage(message: String) = apply { dialog.message = message }
        fun setView(view: View) = apply { dialog.customView = view }
        
        fun setItems(items: Array<String>, listener: (Int) -> Unit) = apply {
            dialog.items = items
            dialog.onItemClickListener = listener
        }
        
        fun setPositiveButton(text: String, action: (() -> Unit)? = null) = apply {
            dialog.positiveButtonText = text
            dialog.positiveButtonAction = action
        }

        fun setNegativeButton(text: String, action: (() -> Unit)? = null) = apply {
            dialog.negativeButtonText = text
            dialog.negativeButtonAction = action
        }

        fun setCancelable(cancelable: Boolean) = apply {
            dialog.setCancelable(cancelable)
        }

        fun create(): CustomDialog = dialog
        
        fun show(): CustomDialog {
            val d = create()
            d.show()
            return d
        }
    }
}