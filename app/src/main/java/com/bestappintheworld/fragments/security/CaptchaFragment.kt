package com.bestappintheworld.fragments.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bestappintheworld.R
import com.bestappintheworld.databinding.FragmentCaptchaBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CaptchaFragment : Fragment() {

    private var _binding: FragmentCaptchaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CaptchaViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaptchaBinding.inflate(inflater, container, false)
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
                }
            }

            lifecycleScope.launch {
                passwordEvent.collect { event ->
                }
            }
        }
    }

    private fun setupViews() {
        binding.apply {
            circleDraw.onCircleFinished = {
                val perfectionPercentage = circleDraw.calculatePerfection()
                circlePerfectionTextView.text = getString(
                    R.string.captcha_percentage_result,
                    String.format("%.0f", perfectionPercentage)
                )

                captchaResultTextView.text = getString(
                    if (perfectionPercentage > 90) R.string.captcha_fail_high
                    else if (perfectionPercentage > 86) R.string.captcha_success
                    else R.string.captcha_fail_low
                )
            }
        }
    }
}
