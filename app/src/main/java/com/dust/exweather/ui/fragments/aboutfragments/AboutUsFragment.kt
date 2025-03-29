package com.dust.exweather.ui.fragments.aboutfragments

import android.content.ActivityNotFoundException
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
import androidx.core.net.toUri
import com.dust.exweather.R
import com.dust.exweather.utils.Constants
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
                openTelegramAccount()
            }

            instagramImageView.setOnClickListener {
                openInstagramAccount()
            }

            whatsAppImageView.setOnClickListener {

            }

            metaImageView.setOnClickListener {

            }

        }
    }

    private fun openInstagramAccount(){
        val instagramUserName = Constants.INSTAGRAM_USER_NAME
        val uri = "http://instagram.com/_u/$instagramUserName".toUri()
        val likeIng = Intent(Intent.ACTION_VIEW, uri)

        likeIng.setPackage("com.instagram.android")

        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "http://instagram.com/$instagramUserName".toUri()
                )
            )
        }
    }

    private fun openTelegramAccount(){
        try{
            val intent = Intent(Intent.ACTION_VIEW, "tg://resolve?domain=${Constants.TELEGRAM_USER_NAME}".toUri())
            startActivity(intent)
        }catch (e:Exception){
            val intent = Intent(Intent.ACTION_VIEW,"https://telegram.me/${Constants.TELEGRAM_USER_NAME}".toUri())
            startActivity(intent)
        }
    }
}