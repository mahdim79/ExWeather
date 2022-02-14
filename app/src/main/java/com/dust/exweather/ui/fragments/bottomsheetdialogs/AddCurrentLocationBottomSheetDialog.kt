package com.dust.exweather.ui.fragments.bottomsheetdialogs

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.location.locationserverdata.LocationServerData
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.utils.DataStatus
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_add_current_location.*
import kotlinx.android.synthetic.main.bottom_sheet_add_current_location.view.*

class AddCurrentLocationBottomSheetDialog(
    private val locationDetailsLiveData: LiveData<DataWrapper<LocationServerData>>,
    private val onRequestCurrentLocation: () -> Boolean,
    private val onLocationAddButtonClicked: (String?) -> Unit
) :
    BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_current_location, container, false)
    }

    private lateinit var infiniteRotateAnimation: RotateAnimation

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogRounded

    private fun setup() {
        instantiateInfiniteAlphaAnimation()
        observeLocationData()
        setUpViews()
        handlePermissions()
    }

    private fun instantiateInfiniteAlphaAnimation() {
        infiniteRotateAnimation = RotateAnimation(
            0f,
            360f,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f
        )
        infiniteRotateAnimation.apply {
            duration = 750
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = AlphaAnimation.INFINITE
            repeatMode = AlphaAnimation.RESTART
        }
    }

    private fun observeLocationData() {
        locationDetailsLiveData.observe(viewLifecycleOwner) { data ->
            when (data.status) {
                DataStatus.DATA_RECEIVE_SUCCESS -> {
                    myLocationImage.clearAnimation()
                    addLocationButton.visibility = View.VISIBLE
                    data.data?.location?.let { location ->
                        locationSearchState.text =
                            "${location.name} | ${location.lat},${location.lon}"
                    }
                }
                DataStatus.DATA_RECEIVE_LOADING -> {
                    myLocationImage.startAnimation(infiniteRotateAnimation)
                }
                DataStatus.DATA_RECEIVE_FAILURE -> {
                    addLocationButton.visibility = View.GONE
                    locationSearchState.text = ""
                    myLocationImage.clearAnimation()
                }
            }
        }
    }

    private fun setUpViews() {
        requireView().apply {
            addLocationButton.setOnClickListener {
                if (locationDetailsLiveData.value?.status == DataStatus.DATA_RECEIVE_SUCCESS) {
                    val location = locationDetailsLiveData.value?.data?.location
                    onLocationAddButtonClicked("${location?.lat},${location?.lon}")
                } else {
                    Toast.makeText(
                        requireContext(),
                        "مشکلی پیش آمده است لطفا دوباره تلاش کنید",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                dismiss()
            }

            myLocationImage.startAnimation(infiniteRotateAnimation)

        }
    }

    private fun handlePermissions() {
        if (checkLocationsPermission()) {
            startLocationFetch()
        } else {
            requireActivity().requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1002
            )
        }
    }

    private fun checkLocationsPermission(): Boolean {
        if (requireActivity().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED || requireActivity().checkSelfPermission(
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        )
            return false
        return true
    }

    private fun startLocationFetch() {
        if (!onRequestCurrentLocation())
            dismiss()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1002) {
            if (grantResults[0] != -1) {
                startLocationFetch()
            } else {
                dismiss()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}