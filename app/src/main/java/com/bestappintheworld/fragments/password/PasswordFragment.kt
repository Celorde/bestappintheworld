package com.bestappintheworld.fragments.password

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bestappintheworld.R
import com.bestappintheworld.databinding.FragmentPasswordBinding
import com.bestappintheworld.commons.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PasswordFragment : Fragment() {

    private var _binding: FragmentPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PasswordViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
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
                uiState.collect { _ ->
                }
            }

            lifecycleScope.launch {
                passwordEvent.collect { event ->
                    when (event) {
                        is PasswordEvent.ShowHidePassword -> {
                            setPasswordHideOrShow(event.editText, event.isToHide)
                        }
                        is PasswordEvent.ShowHideError -> {
                            showError(event.errorTextView, event.message)
                        }
                        else -> { }
                    }
                }
            }
        }
    }

    private fun setupViews() {
        binding.apply {
            insertPasswordEditText.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                viewModel.validatePassword(insertPasswordEditText, wrongPasswordTextView, text.toString())
            })

            insertPasswordConfirmationEditText.apply {
                addTextChangedListener(onTextChanged = { text, _, _, _ ->
                    //TODO Validar de usou CTRL V, se sim, colar a palavra: COLAR
                    viewModel.validatePassword(
                        this,
                        wrongPasswordConfirmationTextView,
                        text.toString()
                    )
                })

                setPasteEvent {
                    insertPasswordConfirmationEditText.setText("Colar")
                }
            }

            showHidePasswordButton.setOnClickListener {
                toggleShowHidePassword(insertPasswordEditText, false)
            }

            showHidePasswordConfirmationButton.setOnClickListener {
                toggleShowHidePassword(insertPasswordConfirmationEditText, true)
            }
        }
    }

    private fun toggleShowHidePassword(editText: EditText, isConfirmation: Boolean) {
        binding.apply {
            //TODO Corrigir bug da senha não aparecendo
            setPasswordHideOrShow(editText, false)

            if (isConfirmation) {
                showHidePasswordConfirmationButton.setImageResource(R.drawable.ic_password_show)

                CoroutineScope(Dispatchers.IO).launch {
                    Thread.sleep(1000)

                    requireActivity().runOnUiThread {
                        showHidePasswordConfirmationButton.setImageResource(R.drawable.ic_password_hide)
                        insertPasswordConfirmationEditText.setText("")

                        showToast("A senha foi comprometida. Por segurança, faça novamente.")
                        setPasswordHideOrShow(editText, true)
                        wrongPasswordConfirmationTextView.text = ""
                    }
                }

                showHidePasswordConfirmationButton.setImageResource(R.drawable.ic_password_show)
            } else {
                showHidePasswordButton.setImageResource(R.drawable.ic_password_show)

                CoroutineScope(Dispatchers.IO).launch {
                    Thread.sleep(1000)

                    requireActivity().runOnUiThread {
                        showHidePasswordButton.setImageResource(R.drawable.ic_password_hide)
                        insertPasswordEditText.setText("")
                        insertPasswordConfirmationEditText.setText("")

                        showToast("A senha foi comprometida. Por segurança, faça novamente.")
                        setPasswordHideOrShow(editText, true)
                        wrongPasswordTextView.text = ""
                        wrongPasswordConfirmationTextView.text = ""
                    }
                }

                showHidePasswordButton.setImageResource(R.drawable.ic_password_show)
            }
        }
    }

    private fun setPasswordHideOrShow(editText: EditText, isToHide: Boolean) {
        editText.apply {
            transformationMethod = if (isToHide) PasswordTransformationMethod.getInstance() else null
            setSelection(length())
        }
    }

    private fun showError(textView: TextView, message: String) {
        textView.text = message
    }
}
