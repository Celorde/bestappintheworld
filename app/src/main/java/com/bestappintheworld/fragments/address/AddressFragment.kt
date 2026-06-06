package com.bestappintheworld.fragments.address

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bestappintheworld.R
import com.bestappintheworld.commons.Utils
import com.bestappintheworld.commons.hideKeyboard
import com.bestappintheworld.commons.showToast
import com.bestappintheworld.components.OnItemClickListener
import com.bestappintheworld.databinding.DialogAddressInputBinding
import com.bestappintheworld.databinding.DialogNumberInputBinding
import com.bestappintheworld.databinding.DialogReferencePointInputBinding
import com.bestappintheworld.databinding.DialogSelectCityBinding
import com.bestappintheworld.databinding.DialogStateInputBinding
import com.bestappintheworld.databinding.FragmentAddressBinding
import com.bestappintheworld.ui.T9Keyboard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class AddressFragment : Fragment() {

    private var _binding: FragmentAddressBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddressViewModel by viewModel()

    private var keyboardController : T9Keyboard? = null
    private var stateBinding: DialogStateInputBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupStateListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupStateListeners() {
        viewModel.apply {
            lifecycleScope.launch {
                uiState.collect { uiState ->
                    binding.apply {
                        insertAddressCodeEditText.setText(uiState.zipCode)
                        stateBinding?.stateEditText?.setText(uiState.state)
                    }
                }
            }

            lifecycleScope.launch {
                addressEvent.collect { state ->
                    when (state) {
                        is AddressEvent.ShowError -> showToast(state.message)
                        else -> { }
                    }
                }
            }
        }
    }

    private fun setupViews() {
        binding.apply {
            insertAddressCodeEditText.setOnClickListener {
                viewModel.startAddressCodeRoulette()
            }

            applyAddressCodeDigitButton.setOnClickListener {
                viewModel.confirmZipCodeDigit()
            }

            doNotKnowAdressCodeTextView.setOnClickListener {
                showToast("Eu também não sei seu CEP.")
            }

            insertStreetEditText.let {
                it.inputType = InputType.TYPE_NULL
                it.keyListener = null

                it.setOnClickListener {
                    showInsertStreetDialog()
                }
            }

            insertStateEditText.let {
                it.inputType = InputType.TYPE_NULL
                it.keyListener = null

                it.setOnClickListener {
                    showBrazilianMap()
                }
            }

            insertCityEditText.let {
                it.inputType = InputType.TYPE_NULL
                it.keyListener = null

                it.setOnClickListener {
                    showCitiesDropdown()
                }
            }

            insertNumberEditText.let {
                it.inputType = InputType.TYPE_NULL
                it.keyListener = null

                it.setOnClickListener {
                    showNumberInputDialog()
                }
            }

            insertComplementEditText.let {
                //TODO não deixar colocar apenas CASA
                //TODO não deixar o campo vazio, se não tiver complemento, tem que escrever: Não tem Complemento
                var oldValue = ""
                var isProcessingChar = false
                val letters = ('a'..'z').toList()
                val numbers = ('0'..'9').toList()

                it.addTextChangedListener(
                    onTextChanged = {text, _, before, count ->
                        if (text?.isNotEmpty() == true) {
                            val writtenChar = text[text.length - 1]

                            if (!isProcessingChar) {
                                isProcessingChar = true

                                var selectedChar: Char? = null

                                if (before > count) {
                                    selectedChar = ' '
                                } else if (writtenChar == ' ') {
                                    oldValue = text.toString()
                                        .substring(0, insertComplementEditText.text.length - 2)
                                } else if (writtenChar.isDigit() == true) {
                                    val indexOfChar = numbers.indexOf(writtenChar.lowercaseChar())
                                    selectedChar = numbers[numbers.size - 1 - indexOfChar]
                                } else {
                                    val indexOfChar = letters.indexOf(writtenChar.lowercaseChar())
                                    selectedChar = letters[letters.size - 1 - indexOfChar]
                                }

                                val parsedText =
                                    if ((selectedChar != null && selectedChar == ' ') || writtenChar == ' ') oldValue else text.toString()
                                        .substring(0, insertComplementEditText.text.length - 1)
                                val newText = if (selectedChar != null) {
                                    parsedText + if (writtenChar.isLowerCase()) {
                                        selectedChar
                                    } else {
                                        selectedChar.uppercaseChar()
                                    }
                                } else {
                                    parsedText
                                }

                                oldValue = newText

                                insertComplementEditText.apply {
                                    setText(newText)
                                    setSelection(insertComplementEditText.text.length)
                                    isProcessingChar = false
                                }
                            }
                        }
                    })

                it.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        hideKeyboard()
                    }
                }
            }

            insertNeighborhoodEditText.let {
                var isSelectionAtTheEnd = false

                it.setOnClickListener {
                    insertNeighborhoodEditText.setSelection(
                        if (isSelectionAtTheEnd.not()) 0 else insertNeighborhoodEditText.text.length
                    )
                }

                it.addTextChangedListener(
                    onTextChanged = { _, _, before, count ->
                        if (before > count) {
                            //TODO Loading -> Processando - esperar 7 segundos
                            it.setText("")
                            return@addTextChangedListener
                        }

                        it.setSelection(if (isSelectionAtTheEnd) 0 else it.text.length)
                        isSelectionAtTheEnd = !isSelectionAtTheEnd
                    }
                )
            }

            insertReferencePointEditText.let {
                it.inputType = InputType.TYPE_NULL
                it.keyListener = null

                it.setOnClickListener {
                    showReferencePointInputDialog()
                }
            }
        }
    }

    private fun showInsertStreetDialog() {
        val dialogView = DialogAddressInputBinding.inflate(layoutInflater)
        dialogView.addressEditText.apply {
            inputType = InputType.TYPE_NULL
            keyListener = null
        }

        startT9Keyboard(dialogView)

        val insertStreetNameDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.insert_address))
            .setView(dialogView.root)
            .setPositiveButton(getString(R.string.ok_button)) { _, _ ->
                binding.insertStreetEditText.setText(dialogView.addressEditText.text)
            }
            .setNegativeButton(getString(R.string.cancel_button)) { _, _ -> }
            .create()
        insertStreetNameDialog.show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showBrazilianMap() {
        stateBinding = DialogStateInputBinding.inflate(layoutInflater)

        stateBinding?.apply {
            val stateBaseMap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.brazilian_colored_states)

            stateEditText.apply {
                inputType = InputType.TYPE_NULL
                keyListener = null
            }

            stateBaseImageView.setOnTouchListener { _, event ->
                viewModel.getSelectedState(event, stateBaseMap, stateBaseImageView.imageMatrix)
                true
            }
        }

        val insertStateDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.insert_state))
            .setView(stateBinding?.root)
            .setPositiveButton(getString(R.string.ok_button)) { _, _ ->
                binding.insertStateEditText.setText(stateBinding?.stateEditText?.text)
            }
            .setNegativeButton(getString(R.string.cancel_button)) { _, _ -> }
            .create()
        insertStateDialog.show()
    }

    private fun showCitiesDropdown() {
        val dialogView = DialogSelectCityBinding.inflate(layoutInflater)
        var insertCityDialog: AlertDialog? = null

        dialogView.apply {
            val adapter = CityAdapter(object : OnItemClickListener {
                override fun onItemClick(item: String) {
                    binding.insertCityEditText.setText(item)
                    insertCityDialog?.dismiss()
                }
            })

            cityListRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                this.adapter = adapter
            }

            cityFilterEditText.apply {
                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(text: Editable?) {
                        val textToFilter = text.toString()
                        adapter.filter(textToFilter)
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }
        }

        insertCityDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.insert_city))
            .setView(dialogView.root)
            .setPositiveButton(getString(R.string.ok_button)) { _, _ -> }
            .setNegativeButton(getString(R.string.cancel_button)) { _, _ -> }
            .show()
    }

    private fun showNumberInputDialog() {
        var number = 0
        val dialogView = DialogNumberInputBinding.inflate(layoutInflater)

        dialogView.cannonBallView.onDistanceMeasured = { distance ->
            if (distance < 0) {
                showToast("Jogou pro lado errado, animal.")
            } else {
                number = ("%.0f".format(distance)).toInt()

                activity?.runOnUiThread {
                    dialogView.numberTextView.text = getString(R.string.insert_number_value, number.toString())
                }
            }
        }

        val insertNumberDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.insert_number))
            .setView(dialogView.root)
            .setPositiveButton(getString(R.string.ok_button)) { _, _ ->
                binding.insertNumberEditText.setText(number.toString())
            }
            .setNegativeButton(getString(R.string.cancel_button)) { _, _ -> }
            .create()
        insertNumberDialog.show()
    }

    private fun showReferencePointInputDialog() {
        val referencePoints: MutableList<String> = Utils.referencePlaces.toMutableList()
        val dialogView = DialogReferencePointInputBinding.inflate(layoutInflater)

        var okCount = 0
        var selectedPlace = referencePoints.random()

        referencePoints.remove(selectedPlace)

        dialogView.insertReferencePointTextView.text = getString(
            R.string.insert_reference_point_suggestion,
            selectedPlace
        )

        val insertReferencePointDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.insert_reference_point))
            .setView(dialogView.root)
            .setPositiveButton("Sim", null)
            .setNegativeButton("Não", null)
            .create()
        insertReferencePointDialog.show()

        val negativeButton = insertReferencePointDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton.setOnClickListener {
            okCount = 0
            selectedPlace = referencePoints.random()
            referencePoints.remove(selectedPlace)

            dialogView.insertReferencePointTextView.text = getString(
                R.string.insert_reference_point_suggestion,
                selectedPlace
            )
        }

        val positiveButton = insertReferencePointDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            okCount++

            if (okCount > 3) {
                insertReferencePointDialog.dismiss()
                binding.insertReferencePointEditText.setText(selectedPlace.substringAfter(" ")
                    .replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()) else it.toString()
                    }
                )
            }

            val nextQuestion = when (okCount) {
                1 -> R.string.insert_reference_point_suggestion_2
                2 -> R.string.insert_reference_point_suggestion_3
                3 -> R.string.insert_reference_point_suggestion_4
                else -> R.string.insert_reference_point_suggestion
            }

            dialogView.insertReferencePointTextView.text = getString(
                nextQuestion,
                selectedPlace
            )
        }
    }

    private fun startT9Keyboard(dialogView: DialogAddressInputBinding) {
        val t9KeyboardLayout = dialogView.keyboardLayout.t9Keyboard
        keyboardController = T9Keyboard(requireContext(), dialogView.keyboardLayout, dialogView.addressEditText)

        for (i in 0 until t9KeyboardLayout.childCount) {
            val child = t9KeyboardLayout.getChildAt(i)
            if (child is Button) {
                child.setOnClickListener {
                    keyboardController?.handleKeyPress(child)
                }
            }
        }
    }
}
