package com.example.bestuiintheworld.fragments.password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bestuiintheworld.databinding.FragmentPasswordBinding
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
    ): View? {
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
                uiState.collect { uiState ->
                    binding.apply {
                        wrongPasswordTextView.text = uiState.errorMessage
                    }
                }
            }

            lifecycleScope.launch {
                passwordEvent.collect { state ->
                    when (state) {
                        else -> { }
                    }
                }
            }
        }
    }

    private fun setupViews() {
        binding.apply {
            insertPasswordEditText.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                viewModel.validatePassword(text.toString())
            })
        }
    }
}
