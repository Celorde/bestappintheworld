package com.bestappintheworld

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bestappintheworld.components.CustomDialog
import com.bestappintheworld.databinding.ActivityMainBinding
import com.bestappintheworld.databinding.DialogAcceptTermsOfUseBinding
import com.bestappintheworld.databinding.DialogTermsOfUseBinding
import com.bestappintheworld.databinding.DialogValidateTermsOfUseBinding
import com.bestappintheworld.fragments.security.CaptchaFragment
import com.bestappintheworld.commons.showToast
import com.bestappintheworld.components.OnVolumeKeyPressed
import com.bestappintheworld.fragments.address.AddressFragment
import com.bestappintheworld.fragments.contact.ContactFragment
import com.bestappintheworld.fragments.extrainfo.ExtraInfoFragment
import com.bestappintheworld.fragments.password.PasswordFragment
import com.bestappintheworld.fragments.personal.PersonalDataFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var volumeKeyListener: OnVolumeKeyPressed? = null
    private var acceptTermsOfUseDialog: CustomDialog? = null

    @SuppressLint("SourceLockedOrientationActivity")
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
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)

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

                    CustomDialog.Builder(this@MainActivity)
                        .setTitle(getString(R.string.terms_of_use_title))
                        .setView(newDialogView.root)
                        .setPositiveButton("Não") { }
                        .setNegativeButton("Sim") {
                            if (wasReadTimeEnough) {
                                dialogView.acceptTermsCheckBox.isEnabled = true
                            } else {
                                showToast("Não deu tempo de ler tudo, apressadinho. Leia novamente.")
                            }
                        }
                        .show()
                }

                override fun updateDrawState(ds: TextPaint) {
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

        acceptTermsOfUseDialog = CustomDialog.Builder(this)
            .setTitle(getString(R.string.terms_of_use_title))
            .setView(dialogView.root)
            .setPositiveButton(getString(R.string.ok_button)) {
                if (dialogView.acceptTermsCheckBox.isChecked) {
                    showToast("Você não aceitou os Termos. Por segurança, releia e aceite.")
                } else {
                    val validateTermsView = DialogValidateTermsOfUseBinding.inflate(layoutInflater)
                    CustomDialog.Builder(this@MainActivity)
                        .setTitle(getString(R.string.validate_terms_of_use))
                        .setView(validateTermsView.root)
                        .setPositiveButton("Sim") {
                            if (validateTermsView.validateTermsEditText.text.toString() == "1 cebola média picada") {
                                goToNextFragment(CaptchaFragment())
                            } else {
                                showToast("Ingrediente errado. Por segurança, releia e tente novamente.")
                            }
                        }
                        .setNegativeButton("Não") { }
                        .show()
                }
            }
            .setNegativeButton(getString(R.string.cancel_button)) { }
            .show()
    }
}
