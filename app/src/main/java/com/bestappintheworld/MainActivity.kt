package com.example.bestappintheworld

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bestappintheworld.R
import com.bestappintheworld.databinding.ActivityMainBinding
import com.example.bestappintheworld.components.OnVolumeKeyPressed
import com.example.bestappintheworld.fragments.address.AddressFragment
import com.example.bestappintheworld.fragments.contact.ContactFragment
import com.example.bestappintheworld.fragments.extrainfo.ExtraInfoFragment
import com.example.bestappintheworld.fragments.password.PasswordFragment
import com.example.bestappintheworld.fragments.personal.PersonalDataFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var volumeKeyListener: OnVolumeKeyPressed? = null

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
}
