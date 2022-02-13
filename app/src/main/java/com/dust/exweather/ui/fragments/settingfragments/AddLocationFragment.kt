package com.dust.exweather.ui.fragments.settingfragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.dust.exweather.R
import com.dust.exweather.utils.DataStatus
import com.dust.exweather.viewmodel.factories.AddLocationFragmentViewModelFactory
import com.dust.exweather.viewmodel.fragments.AddLocationFragmentViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_add_location.*
import javax.inject.Inject

class AddLocationFragment() : DaggerFragment(), OnMapReadyCallback {

    private var marker: Marker? = null

    @Inject
    lateinit var factory: AddLocationFragmentViewModelFactory

    private lateinit var viewModel: AddLocationFragmentViewModel

    private val compositeDisposable = CompositeDisposable()

    private var handler: Handler? = null

    private lateinit var runnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        setUpUi()
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(this, factory)[AddLocationFragmentViewModel::class.java]
    }

    private fun setUpUi() {
        observeApiState()
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
    }

    private fun observeApiState() {
        viewModel.getLocationDetailsLiveData().observe(viewLifecycleOwner) { data ->
            when (data.status) {
                DataStatus.DATA_RECEIVE_LOADING -> {
                    locationEditText.setText("Loading Location Data")
                }
                DataStatus.DATA_RECEIVE_SUCCESS -> {
                    locationEditText.setText("${data.data?.location?.name ?: ""} | ${data.data?.location?.lat ?: ""},${data.data?.location?.lon ?: ""}")
                }
                DataStatus.DATA_RECEIVE_FAILURE -> {
                    locationEditText.setText("")
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        googleMap?.let { map ->
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(32.4279, 53.6880), 4.8f))

            map.setOnMapClickListener { latLng ->
                Log.i("observableLog", "done!")
                marker?.remove()
                marker = map.addMarker(
                    MarkerOptions().position(latLng)
                        .icon(
                            BitmapDescriptorFactory.fromBitmap(
                                BitmapFactory.decodeResource(
                                    resources,
                                    R.drawable.location
                                )
                            )
                        )
                )

                getLatLangDetails("${latLng.latitude},${latLng.longitude}")

            }

        }

    }

    private fun getLatLangDetails(s: String) {
        handler?.removeCallbacks(runnable)

        handler = Handler()
        runnable = Runnable {
            viewModel.getLocationDetailsData(s,requireContext())
            handler = null
        }
        handler!!.postDelayed(runnable, 1000)
    }

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
    }
}