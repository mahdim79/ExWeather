package com.dust.exweather.ui.fragments.bottomsheetdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dust.exweather.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.choose_theme_language_dialog.view.*

class ChooseLanguageThemeBottomSheetDialog(
    private val positiveButtonChecked: Boolean,
    private val title: String,
    private val positiveText: String,
    private val negativeText: String,
    private val onRadioClicked: (Boolean) -> Unit
) :
    BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.choose_theme_language_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogRounded

    private fun setUpViews() {
        requireView().apply {
            if (positiveButtonChecked)
                positiveRadioButton.isChecked = true
            else
                negativeRadioButton.isChecked = true
            dialogTitle.text = title

            positiveRadioButton.text = positiveText
            negativeRadioButton.text = negativeText

            add_button.setOnClickListener {
                onRadioClicked(positiveRadioButton.isChecked)
                dismiss()
            }
        }
    }
}