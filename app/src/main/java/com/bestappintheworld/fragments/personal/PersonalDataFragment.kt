package com.example.bestappintheworld.fragments.personal

import android.annotation.SuppressLint
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bestappintheworld.R
import com.bestappintheworld.commons.SchoolDegree
import com.bestappintheworld.databinding.DialogBirthdateInputBinding
import com.bestappintheworld.databinding.DialogProfessionInputBinding
import com.bestappintheworld.databinding.DialogSchoolDegreeInputBinding
import com.bestappintheworld.databinding.DialogSchoolDegreeTermoBinding
import com.bestappintheworld.databinding.FragmentPersonalDataBinding
import com.example.bestappintheworld.MainActivity
import com.example.bestappintheworld.commons.Utils
import com.example.bestappintheworld.commons.hideKeyboard
import com.example.bestappintheworld.commons.openUrl
import com.example.bestappintheworld.commons.showKeyboard
import com.example.bestappintheworld.commons.showToast
import com.example.bestappintheworld.components.OnVolumeKeyPressed
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

class PersonalDataFragment : Fragment(), OnVolumeKeyPressed {

    private var _binding: FragmentPersonalDataBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PersonalDataViewModel by viewModel()

    private var initialYear = 1900
    private var isAgeBlocked = false
    private var hasValidateIfRomanNumber = false

    private var currentLetterIndex = 0
    private val alphabet = Utils.regularCharacters.toList()
    private val confirmedText = StringBuilder()
    private var selectionTimer: Handler? = null
    private val delayMillis = 1000L
    private var shouldConfirmLetter = false

    private lateinit var schoolDegreeInputDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPersonalDataBinding.inflate(inflater, container, false)
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

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setVolumeKeyListener(this)
    }

    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.setVolumeKeyListener(null)
    }

    override fun onVolumeKeyPressed(keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                shouldConfirmLetter = true
                currentLetterIndex = (currentLetterIndex + 1) % alphabet.size
                resetSelectionTimer()
                updateEditText()
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                shouldConfirmLetter = false

                binding.apply {
                    val currentText = nicknameEditText.text

                    if (currentText.isNotEmpty()) {
                        nicknameEditText.setText(currentText.dropLast(1))
                        confirmedText.deleteAt(confirmedText.length - 1)
                    }
                }
                return true
            }
        }
        return false
    }

    private fun setupStateListeners() {
        viewModel.apply {
            lifecycleScope.launch {
                uiState.collect { uiState ->
                    binding.apply {
//                textView.text = formattedDate
                        nameEditText.setText(uiState.name)
                        insertSexTextView.isVisible = uiState.shouldShowSex
                        insertSexEditText.isVisible = uiState.shouldShowSex
                    }
                }
            }

            lifecycleScope.launch {
                personalDataEvent.collect { state ->
                    when (state) {
                        is PersonalDataEvent.EraseAgeLastChar -> eraseAgeLastChar()
                        is PersonalDataEvent.EraseNickname -> binding.nicknameEditText.setText("")
                        is PersonalDataEvent.BlockAgeField -> blockUserAgeInput()
                        is PersonalDataEvent.RomanNumeralsWarning -> showRomanNumeralsWarning()
                        is PersonalDataEvent.ResetAge -> resetAge()
                        is PersonalDataEvent.ShowError -> showToast(state.message)
                        else -> { }
                    }
                }
            }
        }
    }

    private fun setupViews() {
        binding.apply {
            nameEditText.apply {
                keyListener = null

                setOnClickListener {
                    showNameLetterInputDialog()
                }
            }

            nicknameEditText.apply {
                setOnFocusChangeListener { view, hasFocus ->
                    nicknameSoundImage.isVisible = hasFocus
                    //TODO Colocar uma música incrível.
                    // Notificação do WhatsApp
                    // Notificação do Teams
                    // Pernilongo
                    // Aqui é o LuanGameplays
                    // Shrek do Urro
                    // Procurar no myinstants
                    // Uba Uba Uba Ê
                    // Tamo competindo, tamo competindo? Ou tamo competindo tamo competindo

                    //Colocar uma música. Se não terminar antes da música acabar, reseta o campo, reseta a música
                }

                keyListener = null
                setOnTouchListener { v, event ->
                    v.performClick()
                    v.requestFocus()
                    requireContext().showKeyboard(v)
                    true
                }
            }

            insertBirthdateEditText.apply {
                keyListener = null

                setOnClickListener {
                    if (isAgeBlocked) {
                        showToast("Campo bloqueado até você estudar sobre números romanos no link abaixo.")
                        return@setOnClickListener
                    }

                    showBirthdateInputDialog(it as EditText)
                }
            }

            insertAgeEditText.apply {
                addTextChangedListener(onTextChanged = { text, _, _, _ ->
                    viewModel.validateRomanNumerals(text = text.toString())
                })

                setOnFocusChangeListener { view, hasFocus ->
                    if (!hasFocus) hideKeyboard()

                    viewModel.validateAgeText(
                        hasFocus = hasFocus,
                        birthdate = binding.insertBirthdateEditText.text.toString(),
                        age = binding.insertAgeEditText.text.toString()
                    )
                }
            }

            romanNumbersLinkTextView.setOnClickListener {
                requireContext().openUrl(getString(R.string.roman_numbers_link))
                isAgeBlocked = false
                binding.romanNumbersLinkTextView.isVisible = false
            }

            insertSchoolDegreeEditText.apply {
                keyListener = null

                setOnClickListener {
                    showSchoolDegreeInputDialog()
                }
            }

            insertProfessionEditText.apply {
                keyListener = null

                setOnClickListener {
                    showProfessionInputDialog()
                }
            }
        }
    }

    private fun showNameLetterInputDialog() {
        val shuffledLetters = Utils.letters.shuffled()

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.insert_name_dialog_title))
        builder.setItems(shuffledLetters.toTypedArray()) { _, index ->
            viewModel.parseSelectedNameLetter(shuffledLetters, index)
        }
        builder.show()
    }

    private fun showBirthdateInputDialog(editText: EditText) {
        val dialogView = DialogBirthdateInputBinding.inflate(layoutInflater)
        val birthdateInputDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.insert_birthdate))
            .setView(dialogView.root)
            .setPositiveButton(getString(R.string.ok_button)) { _, _ ->
                editText.setText(dialogView.insertBirthdateTextView.text)
            }
            .setNegativeButton(getString(R.string.cancel_button)) { _, _ -> }
            .create()

        dialogView.birthdateSelectorSeekBar.let {
            //TODO Change to ViewModel
            setDateToField(
                textView = dialogView.insertBirthdateTextView,
                dayOfYear = 1,
                year = initialYear
            )

            it.initialYear = initialYear

            it.onProgressChanged = { currentYear, dayOfYear, hasReleasedFinger ->
                setDateToField(
                    textView = dialogView.insertBirthdateTextView,
                    dayOfYear = dayOfYear,
                    year = currentYear
                )

                if (hasReleasedFinger) {
                    editText.setText(dialogView.insertBirthdateTextView.text)
                    birthdateInputDialog.dismiss()
                    initialYear = initialYear - 10
                }
            }
        }

        birthdateInputDialog.show()
    }

    private fun showSchoolDegreeInputDialog() {
        val dialogView = DialogSchoolDegreeInputBinding.inflate(layoutInflater)
        schoolDegreeInputDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.insert_school_degree))
            .setView(dialogView.root)
            .setPositiveButton(getString(R.string.ok_button)) { _, _ ->
                val radioButtonId = dialogView.radioGroup.checkedRadioButtonId
                val selectedRadioButton =
                    dialogView.radioGroup.children.find { it.id == radioButtonId } as RadioButton

                val newDialogView = DialogSchoolDegreeTermoBinding.inflate(layoutInflater)
                val termoDialog = AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.termo_title, selectedRadioButton.text))
                    .setView(newDialogView.root)
                    .setPositiveButton("Sim", null)
                    .setNegativeButton("Não", null)
                    .create()

                newDialogView.termoGame.setData(
                    editText = binding.insertSchoolDegreeEditText,
                    schoolDegree = SchoolDegree.getDegreeByValue(
                        value = selectedRadioButton.text.toString()
                    ), {
                        schoolDegreeInputDialog.dismiss()
                    }
                )

                termoDialog.show()
            }
            .setNegativeButton(getString(R.string.cancel_button)) { _, _ -> }
            .create()

        schoolDegreeInputDialog.show()
    }

    private fun showProfessionInputDialog() {
        val dialogView = DialogProfessionInputBinding.inflate(layoutInflater)
        schoolDegreeInputDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.create_profession_avatar_title))
            .setView(dialogView.root)
            .setPositiveButton(getString(R.string.ok_button)) { _, _ ->
                val selectedProfession = dialogView.avatarGame.parseSelectedProfession()
                binding.insertProfessionEditText.setText(selectedProfession)
            }
            .setNegativeButton(getString(R.string.cancel_button)) { _, _ -> }
            .create()

        schoolDegreeInputDialog.show()
    }

    private fun setDateToField(textView: TextView, dayOfYear: Int, year: Int) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        calendar.set(Calendar.DAY_OF_YEAR, dayOfYear)
        calendar.set(Calendar.YEAR, year)
        val formattedDate = dateFormat.format(calendar.time)
        textView.text = formattedDate
    }

    private fun eraseAgeLastChar() {
        binding.apply {
            insertAgeEditText.setText(insertAgeEditText.text.dropLast(1))
            insertAgeEditText.setSelection(insertAgeEditText.text.length)
        }
    }

    private fun blockUserAgeInput() {
        isAgeBlocked = true
        hideKeyboard()

        binding.apply {
            rootView.requestFocus()
            romanNumbersLinkTextView.isVisible = true
            insertAgeEditText.setText("")
        }
    }

    private fun showRomanNumeralsWarning() {
        hideKeyboard()
        hasValidateIfRomanNumber = true
    }

    private fun resetAge() {
        binding.apply {
            insertBirthdateEditText.setText("")
            insertAgeEditText.setText("")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateEditText() {
        val currentLetter = alphabet[currentLetterIndex]
        binding.nicknameEditText.setText(confirmedText.toString() + currentLetter)
    }

    private fun resetSelectionTimer() {
        selectionTimer?.removeCallbacksAndMessages(null)
        selectionTimer = Handler(Looper.getMainLooper())
        selectionTimer?.postDelayed({
            if (shouldConfirmLetter) confirmLetter()
        }, delayMillis)
    }

    private fun confirmLetter() {
        val currentLetter = alphabet[currentLetterIndex]
        confirmedText.append(currentLetter)
        currentLetterIndex = 0
    }
}
