package com.dust.exweather.ui.fragments.bottomsheetdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dust.exweather.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_csv_save.view.*

class CsvSaveBottomSheetDialogFragment(
    private val defaultName: String,
    private val onSelectPath: (name: String) -> Unit
) :
    BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_csv_save, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi()

    }

    private fun setUpUi() {
        requireView().apply {
            fileNameEditText.hint = fileNameEditText.hint.toString().plus(defaultName)
            applyFileNameButton.setOnClickListener {
                if (!fileNameEditText.text.toString()
                        .contains(" ") && !fileNameEditText.text.toString().contains(".") &&
                    !fileNameEditText.text.toString().contains("/")
                ) {
                    onSelectPath(fileNameEditText.text.toString())
                    this@CsvSaveBottomSheetDialogFragment.dismiss()
                } else {
                    textInputLayout.error = getString(R.string.wrongFileName)
                }
            }
        }
    }
}