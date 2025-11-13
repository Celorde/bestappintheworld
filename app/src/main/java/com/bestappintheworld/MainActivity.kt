package com.example.bestappintheworld

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bestappintheworld.R
import com.bestappintheworld.databinding.ActivityMainBinding
import com.bestappintheworld.databinding.DialogAcceptTermsOfUseBinding
import com.bestappintheworld.databinding.DialogTermsOfUseBinding
import com.bestappintheworld.databinding.DialogValidateTermsOfUseBinding
import com.bestappintheworld.fragments.security.CaptchaFragment
import com.example.bestappintheworld.commons.showToast
import com.example.bestappintheworld.components.OnVolumeKeyPressed
import com.example.bestappintheworld.fragments.address.AddressFragment
import com.example.bestappintheworld.fragments.contact.ContactFragment
import com.example.bestappintheworld.fragments.extrainfo.ExtraInfoFragment
import com.example.bestappintheworld.fragments.password.PasswordFragment
import com.example.bestappintheworld.fragments.personal.PersonalDataFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var volumeKeyListener: OnVolumeKeyPressed? = null
    private lateinit var acceptTermsOfUseDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, PersonalDataFragment())
            .commit()

        binding.apply {
            nextButton.setOnClickListener {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView);

                when(currentFragment) {
                    is PersonalDataFragment -> goToNextFragment(ContactFragment())
                    is ContactFragment -> goToNextFragment(AddressFragment())
                    is AddressFragment -> goToNextFragment(ExtraInfoFragment())
                    is ExtraInfoFragment -> goToNextFragment(PasswordFragment())
                    is PasswordFragment -> goToTermsOfUse()
                    else -> { }
                }
            }
        }
    }

    fun setVolumeKeyListener(listener: OnVolumeKeyPressed?) {
        this.volumeKeyListener = listener
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        volumeKeyListener?.onVolumeKeyPressed(keyCode)
        return super.onKeyDown(keyCode, event)
    }

    private fun goToNextFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .commit()
    }

    private fun goToTermsOfUse() {
        val dialogView = DialogAcceptTermsOfUseBinding.inflate(layoutInflater)

        val titleText = "Para continuar você precisa aceitar os Termos de Condição do Melhor Aplicativo do Mundo"
        val clickableWords = "Termos de Condição"

        val subtitleText = "Clique nos Termos para ler as Condições"
        val underlineWord = "Clique"
        val blueWord = "Termos"


        val titleSpannable = SpannableString(titleText)
        val subtitleSpannable = SpannableString(subtitleText)

        val start = titleText.indexOf(clickableWords)
        val end = start + clickableWords.length

        if (start != -1) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val newDialogView = DialogTermsOfUseBinding.inflate(layoutInflater)

                    var wasReadTimeEnough = false

                    object : CountDownTimer(20000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                        }

                        override fun onFinish() {
                            wasReadTimeEnough = true
                        }
                    }.start()

                    val termsOfUseDialog = AlertDialog.Builder(this@MainActivity)
                        .setTitle(getString(R.string.terms_of_use_title))
                        .setView(newDialogView.root)
                        .setPositiveButton("Não", null)
                        .setNegativeButton("Sim", { _, _ ->
                            if (wasReadTimeEnough) {
                                dialogView.acceptTermsCheckBox.isEnabled = true
                            } else {
                                showToast("Não deu tempo de ler tudo, apressadinho. Leia novamente.")
                            }
                        })
                        .create()
                    termsOfUseDialog.show()
                }

                override fun updateDrawState(ds: android.text.TextPaint) {
                    ds.isUnderlineText = false
                }
            }

            titleSpannable.setSpan(
                clickableSpan,
                start,
                end,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val startSub = subtitleText.indexOf(underlineWord)
            val endSub = startSub + underlineWord.length
            subtitleSpannable.setSpan(
                UnderlineSpan(),
                startSub,
                endSub,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val startLink = subtitleText.indexOf(blueWord)
            val endLink = startLink + blueWord.length
            subtitleSpannable.setSpan(
                ForegroundColorSpan(Color.BLUE),
                startLink,
                endLink,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        dialogView.apply {
            titleAcceptTerms.apply {
                text = titleSpannable
                movementMethod = LinkMovementMethod.getInstance()
            }
            subTitleAcceptTerms.apply {
                text = subtitleSpannable
                movementMethod = LinkMovementMethod.getInstance()
            }
        }

        acceptTermsOfUseDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.terms_of_use_title))
            .setView(dialogView.root)
            .setPositiveButton(getString(R.string.ok_button)) { _, _ ->
                if (dialogView.acceptTermsCheckBox.isChecked) {
                    showToast("Você não aceitou os Termos. Por segurança, releia e aceite.")
                } else {
                    val validateTermsView = DialogValidateTermsOfUseBinding.inflate(layoutInflater)
                    val validateTermsDialog = AlertDialog.Builder(this@MainActivity)
                        .setTitle(getString(R.string.validate_terms_of_use))
                        .setView(validateTermsView.root)
                        .setPositiveButton("Sim", ({ _, _ ->
                            if (validateTermsView.validateTermsEditText.text.toString() == "1 cebola média picada") {
                                goToNextFragment(CaptchaFragment())
                            } else {
                                showToast("Ingrediente errado. Por segurança, releia e tente novamente.")
                            }
                        }))
                        .setNegativeButton("Não", null)
                        .create()
                    validateTermsDialog.show()
                }
            }
            .setNegativeButton(getString(R.string.cancel_button)) { _, _ -> }
            .create()

        acceptTermsOfUseDialog.show()
    }
}
