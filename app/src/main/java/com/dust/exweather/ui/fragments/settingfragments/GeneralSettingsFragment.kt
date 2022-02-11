package com.dust.exweather.ui.fragments.settingfragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.dust.exweather.BuildConfig
import com.dust.exweather.R
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.ui.activities.SplashActivity
import com.dust.exweather.utils.Settings
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.choose_theme_language_dialog.*
import kotlinx.android.synthetic.main.fragment_general_settings.view.*
import javax.inject.Inject

class GeneralSettingsFragment : DaggerFragment() {

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_general_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi()
    }

    private fun setUpUi() {
        requireView().apply {

            currentLanguageText.text =
                if (sharedPreferencesManager.getLanguageSettings() == Settings.LANGUAGE_PERSIAN) getString(
                    R.string.persian
                ) else getString(R.string.english)
            nightModeSwitchCompat.isChecked =
                sharedPreferencesManager.getThemeSettings() == Settings.THEME_DARK
            currentNotificationText.text =
                if (sharedPreferencesManager.getNotificationSettings() == Settings.NOTIFICATION_ON) getString(
                    R.string.enable
                ) else getString(R.string.disable)

            versionText.text = BuildConfig.VERSION_NAME

            languageSettings.setOnClickListener {
                val currentLanguageSettings = sharedPreferencesManager.getLanguageSettings()
                Dialog(requireContext()).apply {
                    setContentView(R.layout.choose_theme_language_dialog)
                    positiveRadioButton.text = getString(R.string.persian)
                    negativeRadioButton.text = getString(R.string.english)
                    dialogTitle.text = getString(R.string.chooseLang)
                    if (currentLanguageSettings == Settings.LANGUAGE_PERSIAN)
                        positiveRadioButton.isChecked = true
                    else
                        negativeRadioButton.isChecked = true
                    add_button.setOnClickListener {
                        if ((currentLanguageSettings == Settings.LANGUAGE_ENGLISH && negativeRadioButton.isChecked) || (currentLanguageSettings == Settings.LANGUAGE_PERSIAN && positiveRadioButton.isChecked)) {
                            dismiss()
                        } else {
                            if (positiveRadioButton.isChecked)
                                sharedPreferencesManager.setLanguageSettings(Settings.LANGUAGE_PERSIAN)
                            else
                                sharedPreferencesManager.setLanguageSettings(Settings.LANGUAGE_ENGLISH)
                            restartApplication()
                        }
                    }
                }.show()
            }

            nightModeSwitchCompat.setOnCheckedChangeListener { _, b ->
                sharedPreferencesManager.setThemeSettings(if (b) Settings.THEME_DARK else Settings.THEME_LIGHT)
                restartApplication()
            }

            notificationsSettings.setOnClickListener {
                val currentNotificationSettings = sharedPreferencesManager.getNotificationSettings()
                Dialog(requireContext()).apply {
                    setContentView(R.layout.choose_theme_language_dialog)
                    positiveRadioButton.text = getString(R.string.enable)
                    negativeRadioButton.text = getString(R.string.disable)
                    dialogTitle.text = getString(R.string.notifications)
                    if (currentNotificationSettings == Settings.NOTIFICATION_ON)
                        positiveRadioButton.isChecked = true
                    else
                        negativeRadioButton.isChecked = true
                    add_button.setOnClickListener {
                        if (positiveRadioButton.isChecked) {
                            sharedPreferencesManager.setNotificationSettings(Settings.NOTIFICATION_ON)
                            requireView().currentNotificationText.text = getString(R.string.enable)
                        } else {
                            sharedPreferencesManager.setNotificationSettings(Settings.NOTIFICATION_OFF)
                            requireView().currentNotificationText.text = getString(R.string.disable)
                        }
                        dismiss()
                    }
                }.show()
            }

            aboutUsSettings.setOnClickListener {
                requireActivity().findNavController(R.id.mainFragmentContainerView)
                    .navigate(R.id.action_generalSettingsFragment_to_aboutUsFragment)
            }

        }
    }

    private fun restartApplication() {
        requireActivity().apply {
            finishAffinity()
            startActivity(Intent(this, SplashActivity::class.java))
        }
    }
}