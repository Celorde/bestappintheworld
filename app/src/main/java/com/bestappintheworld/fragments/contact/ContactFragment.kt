package com.bestappintheworld.fragments.contact

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bestappintheworld.R
import com.bestappintheworld.commons.Utils.diceFaces
import com.bestappintheworld.commons.showToast
import com.bestappintheworld.components.CustomDialog
import com.bestappintheworld.databinding.DialogEmailInputBinding
import com.bestappintheworld.databinding.DialogPhoneInputBinding
import com.bestappintheworld.databinding.FragmentContactBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ContactViewModel by viewModel()

    private var emailInputBinding: DialogEmailInputBinding? = null
    private var phoneInputBinding: DialogPhoneInputBinding? = null

    private var emailValue = ""
    private var confirmEmailValue = ""
    private lateinit var emailLetterDialog: CustomDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
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
                        emailEditText.setText(uiState.emailValue)
                        confirmEmailEditText.setText(uiState.confirmEmailValue)

                        emailInputBinding?.textViewLetter?.text = uiState.selectedChar
                        phoneInputBinding?.livesTextView?.text = getString(R.string.insert_phone_lives_remaining, uiState.livesRemaining)
                        phoneInputBinding?.phoneEditText?.setText(uiState.rolledNumber)
                        phoneInputBinding?.diceImageView?.setImageResource(diceFaces[uiState.diceFace])
                    }
                }
            }

            lifecycleScope.launch {
                contactEvent.collect { state ->
                    when (state) {
                        is ContactEvent.ShowError -> showToast(state.message)
                        is ContactEvent.CloseEmailInputDialog -> emailLetterDialog.dismiss()
                        is ContactEvent.StartDiceAnimation -> phoneInputBinding?.motionLayout?.transitionToStart()
                        is ContactEvent.StopDiceAnimation -> phoneInputBinding?.motionLayout?.transitionToEnd()
                        else -> { }
                    }
                }
            }
        }
    }

    private fun setupViews() {
        binding.apply {
            emailEditText.let { it ->
                it.inputType = InputType.TYPE_NULL
                it.keyListener = null

                it.setOnClickListener {
                    showEmailLetterInputDialog(isConfirmEmail = false, emailInfo = emailValue)
                }
            }

            confirmEmailEditText.let { it ->
                it.inputType = InputType.TYPE_NULL
                it.keyListener = null

                it.setOnClickListener {
                    showEmailLetterInputDialog(isConfirmEmail = true, emailInfo = confirmEmailValue)
                }
            }

            insertPhoneEditText.let { it ->
                it.inputType = InputType.TYPE_NULL
                it.keyListener = null

                it.setOnClickListener {
                    showInsertPhoneDialog(it as EditText)
                }
            }
        }
    }

    private fun showEmailLetterInputDialog(isConfirmEmail: Boolean, emailInfo: String) {
        if (emailInfo.isEmpty()) {
            callRandomLetterDialog(
                isRemoveLetter = false,
                isConfirmEmail = isConfirmEmail,
                emailInfo = emailInfo
            )
            return
        }

        val dialogView = DialogEmailInputBinding.inflate(layoutInflater)

        val addRemoveDialog = CustomDialog.Builder(requireContext())
            .setTitle(getString(R.string.insert_email_add_or_remove_title))
            .setView(dialogView.root)
            .setPositiveButton("Adicionar") {
                callRandomLetterDialog(
                    isRemoveLetter = false,
                    isConfirmEmail = isConfirmEmail,
                    emailInfo = emailInfo
                )
            }
            .setNegativeButton("Remover") {
                callRandomLetterDialog(
                    isRemoveLetter = true,
                    isConfirmEmail = isConfirmEmail,
                    emailInfo = emailInfo
                )
            }
            .show()
    }

    private fun callRandomLetterDialog(isRemoveLetter: Boolean, isConfirmEmail: Boolean, emailInfo: String) {
        emailInputBinding = DialogEmailInputBinding.inflate(layoutInflater)
        viewModel.initializeEmailCharList()
        viewModel.updateGeneratedChar()
        emailLetterDialog = CustomDialog.Builder(requireContext())
            .setTitle(getString(R.string.insert_email_dialog_title))
            .setView(emailInputBinding?.root ?: View(requireContext()))
            .setCancelable(false)
            .setPositiveButton("Sim") {
                val input = emailInputBinding?.textViewLetter?.text.toString()
                val newEmailValue = viewModel.setSelectedCharToEditText(
                    input = input,
                    isRemoveLetter = isRemoveLetter,
                    isConfirmEmail = isConfirmEmail,
                    emailInfo = emailInfo
                )

                if (isConfirmEmail) {
                    confirmEmailValue = newEmailValue
                } else {
                    emailValue = newEmailValue
                }
            }
            .setNegativeButton("Não")
            .show()

        val negativeButton = emailLetterDialog.getButton(CustomDialog.BUTTON_NEGATIVE)
        negativeButton.setOnClickListener {
            viewModel.updateGeneratedChar()
        }
    }

    private fun showInsertPhoneDialog(editText: EditText) {
        phoneInputBinding = DialogPhoneInputBinding.inflate(layoutInflater)
        phoneInputBinding?.apply {
            var phoneNumber = ""

            phoneEditText.apply {
                inputType = InputType.TYPE_NULL
                keyListener = null
            }

            livesTextView.text = getString(R.string.insert_phone_lives_remaining, "0")

            buttonRolada.setOnClickListener {
                viewModel.startRoll(phoneNumber)
            }

            buttonConfirm.setOnClickListener {
                phoneNumber = phoneEditText.text.toString()
            }
        }

        CustomDialog.Builder(requireContext())
            .setTitle(getString(R.string.insert_phone))
            .setView(phoneInputBinding?.root ?: View(requireContext()))
            .setPositiveButton(getString(R.string.ok_button)) {
                editText.setText(phoneInputBinding?.phoneEditText?.text)
            }
            .setNegativeButton(getString(R.string.cancel_button))
            .show()
    }
}