package com.dust.exweather.ui.fragments.others

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dust.exweather.R
import dagger.android.support.DaggerFragment

class ForecastDetailsFragment : DaggerFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forecast_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i(
            "MainLog",
            "location :${requireArguments().getString("location")} date : ${
                requireArguments().getString(
                    "date"
                )
            }"
        )

    }
}