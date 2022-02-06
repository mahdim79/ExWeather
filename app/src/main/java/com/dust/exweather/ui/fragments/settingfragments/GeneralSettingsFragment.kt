package com.dust.exweather.ui.fragments.settingfragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.dust.exweather.BuildConfig
import com.dust.exweather.R
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
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
                if (sharedPreferencesManager.getLanguageSettings() == Settings.LANGUAGE_PERSIAN) "فارسی" else "انگلیسی"
            nightModeSwitchCompat.isChecked =
                sharedPreferencesManager.getThemeSettings() == Settings.THEME_DARK
            currentNotificationText.text =
                if (sharedPreferencesManager.getNotificationSettings() == Settings.NOTIFICATION_ON) "فعال" else "غیرفعال"

            versionText.text = BuildConfig.VERSION_NAME

            languageSettings.setOnClickListener {
                val currentLanguageSettings = sharedPreferencesManager.getLanguageSettings()
                Dialog(requireContext()).apply {
                    setContentView(R.layout.choose_theme_language_dialog)
                    positiveRadioButton.text = "فارسی"
                    negativeRadioButton.text = "انگلیسی"
                    dialogTitle.text = "انتخاب زبان"
                    if (currentLanguageSettings == Settings.LANGUAGE_PERSIAN)
                        positiveRadioButton.isChecked = true
                    else
                        negativeRadioButton.isChecked = true
                    add_button.setOnClickListener {
                        if (positiveRadioButton.isChecked) {
                            sharedPreferencesManager.setLanguageSettings(Settings.LANGUAGE_PERSIAN)
                            requireView().currentLanguageText.text = "فارسی"
                        } else {
                            sharedPreferencesManager.setLanguageSettings(Settings.LANGUAGE_ENGLISH)
                            requireView().currentLanguageText.text = "انگلیسی"
                        }
                        dismiss()
                    }
                }.show()
            }

            nightModeSwitchCompat.setOnCheckedChangeListener { _, b ->
                sharedPreferencesManager.setThemeSettings(if (b) Settings.THEME_DARK else Settings.THEME_LIGHT)
                requireActivity().recreate()
            }

            notificationsSettings.setOnClickListener {
                val currentNotificationSettings = sharedPreferencesManager.getNotificationSettings()
                Dialog(requireContext()).apply {
                    setContentView(R.layout.choose_theme_language_dialog)
                    positiveRadioButton.text = "فعال"
                    negativeRadioButton.text = "غیرفعال"
                    dialogTitle.text = "اعلانات"
                    if (currentNotificationSettings == Settings.NOTIFICATION_ON)
                        positiveRadioButton.isChecked = true
                    else
                        negativeRadioButton.isChecked = true
                    add_button.setOnClickListener {
                        if (positiveRadioButton.isChecked) {
                            sharedPreferencesManager.setNotificationSettings(Settings.NOTIFICATION_ON)
                            requireView().currentNotificationText.text = "فعال"
                        } else {
                            sharedPreferencesManager.setNotificationSettings(Settings.NOTIFICATION_OFF)
                            requireView().currentNotificationText.text = "غیرفعال"
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
}