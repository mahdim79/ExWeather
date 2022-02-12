package com.dust.exweather.ui.fragments.bottomsheetdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.location.LocationData
import com.dust.exweather.ui.adapters.LocationsRecyclerViewAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_locations.*
import kotlinx.android.synthetic.main.bottom_sheet_locations.view.*
import java.util.*

class LocationsBottomSheetDialog(
    private val locationList: ArrayList<LocationData>,
    private val onLocationRemoved: (String) -> Unit,
    private val onDefaultLocationChanged: (String) -> Unit,
    private val onAddLocationButtonClicked: () -> Unit
) :
    BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_locations, container, false)
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogRounded

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }

    private fun setUpViews() {
        setUpLocationRecyclerView()
    }

    private fun setUpLocationRecyclerView() {
        requireView().apply {
            locationsRecyclerView.apply {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = LocationsRecyclerViewAdapter(locationList, onDefaultLocationChanged) { latLong ->
                    onLocationRemoved(latLong)
                }
            }
        }

        addLocationButton.setOnClickListener {
            dismiss()
            onAddLocationButtonClicked()
        }
    }
}