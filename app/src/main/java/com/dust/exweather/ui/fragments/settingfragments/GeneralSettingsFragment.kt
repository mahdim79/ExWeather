package com.dust.exweather.ui.fragments.settingfragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.dust.exweather.BuildConfig
import com.dust.exweather.R
import com.dust.exweather.sharedpreferences.SharedPreferencesManager
import com.dust.exweather.ui.activities.SplashActivity
import com.dust.exweather.ui.fragments.bottomsheetdialogs.ChooseLanguageThemeBottomSheetDialog
import com.dust.exweather.utils.Settings
import com.dust.exweather.viewmodel.factories.GeneralSettingsViewModelFactory
import com.dust.exweather.viewmodel.fragments.GeneralSettingsViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_general_settings.view.*
import javax.inject.Inject

class GeneralSettingsFragment : DaggerFragment() {

    @Inject
    lateinit var generalSettingsViewModelFactory:GeneralSettingsViewModelFactory

    private lateinit var viewModel:GeneralSettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_general_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        setUpUi()
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(this, generalSettingsViewModelFactory)[GeneralSettingsViewModel::class.java]
    }

    private fun setUpUi() {
        requireView().apply {

            currentLanguageText.text = viewModel.calculateLanguageText(requireContext())
            nightModeSwitchCompat.isChecked = viewModel.calculateNightModeCheckedButton()
            currentNotificationText.text = viewModel.calculateNotificationText(requireContext())
            versionText.text = BuildConfig.VERSION_NAME

            languageSettings.setOnClickListener {
                val currentLanguageSettings = viewModel.getCurrentLanguageSetting()
                ChooseLanguageThemeBottomSheetDialog(
                    positiveButtonChecked = (currentLanguageSettings == Settings.LANGUAGE_PERSIAN),
                    title = getString(R.string.chooseLang),
                    positiveText = getString(R.string.persian),
                    negativeText = getString(R.string.english)
                ) { positiveChecked ->
                    val settings =
                        if (positiveChecked) Settings.LANGUAGE_PERSIAN else Settings.LANGUAGE_ENGLISH

                    if (!((currentLanguageSettings == Settings.LANGUAGE_ENGLISH && settings == Settings.LANGUAGE_ENGLISH) || (currentLanguageSettings == Settings.LANGUAGE_PERSIAN && settings == Settings.LANGUAGE_PERSIAN))) {
                        viewModel.setLanguageSettings(settings)
                        restartApplication()
                    }

                }.show(childFragmentManager, "chooseLangDialog")
            }

            nightModeSwitchCompat.setOnCheckedChangeListener { _, b ->
                viewModel.setThemeSettings(if (b) Settings.THEME_DARK else Settings.THEME_LIGHT)
                restartApplication()
            }

            notificationsSettings.setOnClickListener {
                val currentNotificationSettings = viewModel.getNotificationSettings()

                ChooseLanguageThemeBottomSheetDialog(
                    positiveButtonChecked = (currentNotificationSettings == Settings.NOTIFICATION_ON),
                    title = getString(R.string.notifications),
                    positiveText = getString(R.string.enable),
                    negativeText = getString(R.string.disable)
                ) { positiveChecked ->

                    viewModel.setNotificationSettings(if (positiveChecked) Settings.NOTIFICATION_ON else Settings.NOTIFICATION_OFF)

                    requireView().currentNotificationText.text =
                        getString(if (positiveChecked) R.string.enable else R.string.disable)

                }.show(childFragmentManager, "notificationDialog")

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