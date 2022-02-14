package com.dust.exweather.ui.fragments.settingfragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_add_location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddLocationFragment() : DaggerFragment(), OnMapReadyCallback {

    private var marker: Marker? = null

    @Inject
    lateinit var factory: AddLocationFragmentViewModelFactory

    @Inject
    lateinit var inputMethodManager: InputMethodManager

    private lateinit var viewModel: AddLocationFragmentViewModel

    private val compositeDisposable = CompositeDisposable()

    private var handler: Handler? = null

    private lateinit var runnable: Runnable

    private var manualLocationPickMode = false

    private var googleMap: GoogleMap? = null

    private lateinit var floatingActionButton: FloatingActionButton

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
        setupFloatingActionButton()
        observeApiState()
        setupGoogleMap()
        setupEditText()
    }

    private fun setupFloatingActionButton() {
        floatingActionButton = requireActivity().findViewById(R.id.addLocationFloatButton)
        floatingActionButton.setOnClickListener {
            try {
                val locationText = locationEditText.text.toString()
                val calculatedLat =
                    locationText.substring(locationText.indexOf("|") + 1, locationText.indexOf(","))
                val calculatedLon =
                    locationText.substring(locationText.indexOf(","), locationText.lastIndex)

                when {
                    calculatedLat.endsWith(".") -> calculatedLat.replace(".", "")
                    calculatedLat.contains(" ") -> calculatedLat.replace(" ", "")
                    calculatedLon.endsWith(".") -> calculatedLon.replace(".", "")
                    calculatedLat.contains(" ") -> calculatedLon.replace(" ", "")
                }

                var latLangString = "$calculatedLat,$calculatedLon"
                latLangString = latLangString.substring(0, latLangString.lastIndexOf(","))
                    .plus(latLangString.substring(latLangString.lastIndexOf(",") + 1))

                Log.i("addLocationLog", latLangString)

                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.insertLocationToCache(latLangString)
                    withContext(Dispatchers.Main) {
                        findNavController().popBackStack()
                    }
                }

            } catch (e: Exception) {
                Log.i("excep", e.message.toString())
                Toast.makeText(
                    requireContext(),
                    "لطفا مکان مورد نظر را از روی نقشه یا به صورت دستی از لیست انتخاب کنید",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupEditText() {
        locationEditText.setOnFocusChangeListener { _, b ->
            if (b && !manualLocationPickMode)
                startManualLocationPickMode()
        }

        locationEditText.setOnClickListener {
            if (!manualLocationPickMode)
                startManualLocationPickMode()
        }

        removeTextButton.setOnClickListener {
            locationEditText.setText("")
        }

        backButton.setOnClickListener {
            stopManualLocationPickMode()
        }

        Observable.create<String> { emitter ->
            locationEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(p0: Editable?) {
                    if (manualLocationPickMode) {
                        if (locationEditText.text.toString().isEmpty())
                            removeTextButton.visibility = View.GONE
                        else
                            removeTextButton.visibility = View.VISIBLE
                    }

                    emitter.onNext(locationEditText.text.toString())
                }

            })
        }
            .debounce(1000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<String> {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onNext(t: String) {
                    if (manualLocationPickMode)
                        viewModel.searchForLocation(t, requireContext())
                }

                override fun onError(e: Throwable) {
                }

                override fun onComplete() {
                }

            })
    }

    private fun startManualLocationPickMode() {
        manualLocationPickMode = true
        backButton.visibility = View.VISIBLE
        if (locationEditText.text.toString().isNotEmpty())
            removeTextButton.visibility = View.VISIBLE
        childFragmentManager.beginTransaction().add(
            R.id.locationFragmentContainer,
            ManualLocationPickFragment(viewModel.getManualLocationPickerFragmentLiveData(), {
                stopManualLocationPickMode()
            }) { searchLocation ->
                searchLocation?.let { locationDetails ->
                    stopManualLocationPickMode()
                    locationEditText.setText("${locationDetails.name} | ${locationDetails.lat},${locationDetails.lon}")
                    addMarkerAndZoomToLocation(lat = locationDetails.lat, lon = locationDetails.lon)
                }
            }
        )
            .addToBackStack("ManualLocationPickFragment")
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }

    private fun stopManualLocationPickMode() {
        backButton.visibility = View.GONE
        removeTextButton.visibility = View.GONE
        inputMethodManager.hideSoftInputFromWindow(
            locationEditText.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )

        manualLocationPickMode = false
        childFragmentManager.popBackStack()
    }

    private fun setupGoogleMap() {
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
    }

    private fun observeApiState() {
        viewModel.getLocationDetailsLiveData().observe(viewLifecycleOwner) { data ->
            when (data.status) {
                DataStatus.DATA_RECEIVE_LOADING -> {
                    locationEditText.setText("Loading Location Data")
                    floatingActionButton.visibility = View.GONE
                }
                DataStatus.DATA_RECEIVE_SUCCESS -> {
                    locationEditText.setText("${data.data?.location?.name ?: ""} | ${data.data?.location?.lat ?: ""},${data.data?.location?.lon ?: ""}")
                    floatingActionButton.visibility = View.VISIBLE
                }
                DataStatus.DATA_RECEIVE_FAILURE -> {
                    locationEditText.setText("")
                    floatingActionButton.visibility = View.GONE
                }
            }
        }
    }

    override fun onMapReady(p0: GoogleMap?) {

        googleMap = p0
        googleMap?.let { map ->
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(32.4279, 53.6880), 4.8f))
            map.setOnMapClickListener { latLng ->
                addMarkerAndZoomToLocation(lat = latLng.latitude, lon = latLng.longitude)
                getLatLangDetails("${latLng.latitude},${latLng.longitude}")
            }
        }
    }

    private fun addMarkerAndZoomToLocation(lat: Double, lon: Double) {
        googleMap?.let { map ->
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 10f))
            marker?.remove()
            marker = map.addMarker(
                MarkerOptions().position(LatLng(lat, lon))
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            BitmapFactory.decodeResource(
                                resources,
                                R.drawable.location
                            )
                        )
                    )
            )
        }
    }

    private fun getLatLangDetails(s: String) {
        handler?.removeCallbacks(runnable)

        handler = Handler()
        runnable = Runnable {
            viewModel.getLocationDetailsData(s, requireContext())
            handler = null
        }
        handler!!.postDelayed(runnable, 1000)
    }

    override fun onDestroyView() {
        compositeDisposable.clear()
        floatingActionButton.visibility = View.GONE
        super.onDestroyView()
    }
}