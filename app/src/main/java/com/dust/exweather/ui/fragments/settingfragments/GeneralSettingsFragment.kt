package com.dust.exweather.ui.fragments.settingfragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dust.exweather.R
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.utils.Settings
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.choose_theme_language_dialog.*
import kotlinx.android.synthetic.main.fragment_general_settings.*
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
            val currentLanguageSettings = sharedPreferencesManager.getLanguageSettings()
            val currentThemeSettings = sharedPreferencesManager.getThemeSettings()
            val currentNotificationSettings = sharedPreferencesManager.getNotificationSettings()

            languageSettings.setOnClickListener {
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
                        sharedPreferencesManager.setLanguageSettings(if (positiveRadioButton.isChecked) Settings.LANGUAGE_PERSIAN else Settings.LANGUAGE_ENGLISH)
                        dismiss()
                    }
                }.show()
            }

            themeSettings.setOnClickListener {
                Dialog(requireContext()).apply {
                    setContentView(R.layout.choose_theme_language_dialog)
                    positiveRadioButton.text = "روشن"
                    negativeRadioButton.text = "تیره"
                    dialogTitle.text = "انتخاب تم"
                    if (currentThemeSettings == Settings.THEME_LIGHT)
                        positiveRadioButton.isChecked = true
                    else
                        negativeRadioButton.isChecked = true
                    add_button.setOnClickListener {
                        sharedPreferencesManager.setThemeSettings(if (positiveRadioButton.isChecked) Settings.THEME_LIGHT else Settings.THEME_DARK)
                        dismiss()
                    }
                }.show()
            }

            notificationsSettings.setOnClickListener {
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
                        sharedPreferencesManager.setNotificationSettings(if (positiveRadioButton.isChecked) Settings.NOTIFICATION_ON else Settings.NOTIFICATION_OFF)
                        dismiss()
                    }
                }.show()
            }

            versionSettings.setOnClickListener {

            }

            sourceSettings.setOnClickListener {

            }

        }
    }
}