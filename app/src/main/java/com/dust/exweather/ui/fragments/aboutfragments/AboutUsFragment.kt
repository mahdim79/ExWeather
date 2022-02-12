package com.dust.exweather.ui.fragments.aboutfragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.dust.exweather.R
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_aboutus.view.*

class AboutUsFragment : DaggerFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_aboutus, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }

    private fun setUpViews() {
        requireView().apply {

            val details = StringBuilder()
            details.append(getString(R.string.companyEmailAddress).plus("\n"))
            details.append(getString(R.string.companyLocation).plus("\n"))
            details.append(getString(R.string.companyPhoneNumber).plus("\n"))

            SpannableString(details.toString()).apply {
                setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.standardUiBlue
                        )
                    ), 0, details.lastIndex, SpannableString.SPAN_INCLUSIVE_INCLUSIVE
                )

                setSpan(
                    UnderlineSpan(), 0, details.lastIndex, SpannableString.SPAN_INCLUSIVE_INCLUSIVE
                )

                contactusText.text = this

            }

            shareImageView.setOnClickListener {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                    requireActivity().startActivity(
                        Intent.createChooser(
                            this,
                            getString(R.string.openWith)
                        )
                    )
                }
            }

            telegramImageView.setOnClickListener {

            }

            instagramImageView.setOnClickListener {

            }

            whatsAppImageView.setOnClickListener {

            }

            metaImageView.setOnClickListener {

            }

        }
    }
}