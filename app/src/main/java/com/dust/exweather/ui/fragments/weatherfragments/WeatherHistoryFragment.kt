package com.dust.exweather.ui.fragments.weatherfragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.historyweather.Forecastday
import com.dust.exweather.sharedpreferences.UnitManager
import com.dust.exweather.ui.fragments.bottomsheetdialogs.CsvSaveBottomSheetDialogFragment
import com.dust.exweather.utils.Constants
import com.dust.exweather.utils.DataStatus
import com.dust.exweather.utils.UtilityFunctions
import com.dust.exweather.viewmodel.factories.HistoryFragmentViewModelFactory
import com.dust.exweather.viewmodel.fragments.HistoryFragmentViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_history_weather.*
import kotlinx.android.synthetic.main.fragment_history_weather.view.*
import java.util.*
import javax.inject.Inject

class WeatherHistoryFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: HistoryFragmentViewModelFactory

    @Inject
    lateinit var unitManager: UnitManager

    private lateinit var viewModel: HistoryFragmentViewModel

    private var locationIndex = 0

    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        observeCacheData()
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[HistoryFragmentViewModel::class.java]
    }

    private fun observeCacheData() {
        viewModel.getLiveWeatherDataFromCache().observe(viewLifecycleOwner) { rawData ->
            rawData?.let {
                viewModel.addHistoryDataToDataList(rawData)
                setUpPrimaryUi()
            }
        }
    }

    private fun setUpPrimaryUi() {
        viewModel.getHistoryDataList().also {
            if (!it.isNullOrEmpty()) {
                updateLocationData()
                setUpLocationSwitcherButtons(it.lastIndex)
                setUpPrimaryCalendarView()
                setUpFirstData()
            }
        }
    }

    private fun setUpFirstData() {
        val dataList =
            UtilityFunctions.calculateDayOfMonth(viewModel.getHistoryDataList()[locationIndex].forecast.forecastday.first().date_epoch)

        // dataList is an int list: int year , int month, int dayOfMonth
        changeDate(dataList[0], dataList[1], dataList[2])
    }

    private fun setUpPrimaryCalendarView() {
        updateCalendarViewTime()
        requireView().historyCalendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            changeDate(year, month, dayOfMonth)
        }
    }

    private fun changeDate(year: Int, month: Int, dayOfMonth: Int) {
        updateNoDataAvailable()
        viewModel.getHistoryDataList()[locationIndex].also { weatherHistory ->
            weatherHistory.forecast.forecastday.forEach { forecastDay ->
                calendar.time = Date((forecastDay.date_epoch.toLong()) * 1000)
                if (year == calendar.get(Calendar.YEAR) &&
                    month == calendar.get(Calendar.MONTH) &&
                    dayOfMonth == calendar.get(Calendar.DAY_OF_MONTH)
                ) {
                    updateDayDetailsData(
                        forecastDay,
                        weatherHistory.location.name,
                        UtilityFunctions.createLatLongPattern(weatherHistory.location)
                    )
                }
            }
        }
    }

    private fun updateCalendarViewTime() {
        viewModel.getHistoryDataList()[locationIndex].also { weatherHistory ->
            requireView().historyCalendarView.apply {
                date = weatherHistory.forecast.forecastday.first().date_epoch.toLong() * 1000
            }
        }
        updateNoDataAvailable()
    }

    private fun updateNoDataAvailable() {
        requireView().apply {
            historyDetailsMainContainer.visibility = View.GONE
            noDataTextView.visibility = View.VISIBLE
        }
    }

    private fun updateDayDetailsData(
        forecastDay: Forecastday,
        locationName: String,
        latlong: String
    ) {
        requireView().apply {
            historyDetailsMainContainer.visibility = View.VISIBLE
            noDataTextView.visibility = View.GONE
            dateTextView.text = "${forecastDay.day.dayOfWeek}\n" +
                    "${UtilityFunctions.calculateCurrentDateByTimeEpoch(forecastDay.date_epoch)}"
            weatherStateText.text = forecastDay.day.condition.text
            Glide.with(requireContext()).load(forecastDay.day.condition.icon).into(cloudImage)
            weatherCityNameText.text = locationName
            visibilityTextView.text = unitManager.getVisibilityUnit(forecastDay.day.avgvis_km.toString(),forecastDay.day.avgvis_miles.toString())
            uvIndexTextView.text = forecastDay.day.uv.toString()
            weatherHumidityText.text = requireContext().resources.getString(
                R.string.humidityText,
                forecastDay.day.avghumidity.toString()
            )
            precipText.text =unitManager.getPrecipitationUnit(forecastDay.day.totalprecip_mm.toString(),forecastDay.day.totalprecip_in.toString())
            windSpeedText.text = unitManager.getWindSpeedUnit(forecastDay.day.maxwind_kph.toString(),forecastDay.day.maxwind_mph.toString())
            minTemperatureText.text = unitManager.getTemperatureUnit(forecastDay.day.mintemp_c.toString(),forecastDay.day.mintemp_f.toString())
            avgWeatherTempText.text = unitManager.getTemperatureUnit(forecastDay.day.avgtemp_c.toString(),forecastDay.day.avgtemp_f.toString())
            maxTemperatureText.text = unitManager.getTemperatureUnit(forecastDay.day.maxtemp_c.toString(),forecastDay.day.maxtemp_f.toString())

            hourlyDetailsTextView.setOnClickListener {
                navigateToDetailsFragment(latlong, forecastDay.date, locationName)
            }

            hourlyDetailsImageView.setOnClickListener {
                navigateToDetailsFragment(latlong, forecastDay.date, locationName)
            }

            cvsExportTextView.setOnClickListener {
                if (checkStoragePermission())
                    exportToCsvFile(forecastDay, locationName)
            }

            exportImageView.setOnClickListener {
                if (checkStoragePermission())
                    exportToCsvFile(forecastDay, locationName)
            }

            shareImageView.setOnClickListener {
                Intent(Intent.ACTION_SEND).apply {
                    type = "plain/text"
                    putExtra(
                        Intent.EXTRA_TITLE,
                        getString(
                            R.string.shareTitle,
                            locationName,
                            forecastDay.day.dayOfWeek,
                            forecastDay.date
                        )
                    )
                    putExtra(
                        Intent.EXTRA_TEXT,
                        viewModel.createShareSample(requireContext(), forecastDay, locationName, unitManager)
                    )
                    requireActivity().startActivity(
                        Intent.createChooser(
                            this,
                            getString(R.string.sendWith)
                        )
                    )
                }
            }

        }
    }

    private fun exportToCsvFile(forecastDay: Forecastday, locationName: String) {
        CsvSaveBottomSheetDialogFragment(defaultName = "$locationName-${forecastDay.date}") { fileName ->
            if (checkStoragePermission()) {
                val result = viewModel.exportToCsvFile(
                    forecastDay,
                    locationName,
                    if (fileName.isEmpty()) "$locationName-${forecastDay.date}" else fileName
                )

                if (result.status == DataStatus.DATA_SAVED_SUCCESS
                ) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.fileSuccessfullySaved, result.data),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (result.data == "duplicateFile") {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.fileStoreError).plus(getString(R.string.duplicateFile)),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.fileStoreError).plus(result.data),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.allowPermission),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
            .show(childFragmentManager, "csvNameFileSelectFragment")
    }

    private fun checkStoragePermission(): Boolean {
        requireActivity().apply {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || checkSelfPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            ) {
                requireActivity().requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), Constants.STORAGE_PERMISSION_REQUEST_CODE
                )
                return false
            }
        }
        return true
    }

    private fun navigateToDetailsFragment(latlong: String, date: String, location: String) {
        val bundleArgs = Bundle()
        bundleArgs.apply {
            putString("latlong", latlong)
            putString("date", date)
            putString("location", location)
        }
        requireActivity().findNavController(R.id.mainFragmentContainerView).navigate(
            R.id.action_weatherHistoryFragment_to_historyDetailsFragment,
            bundleArgs
        )
    }

    private fun setUpLocationSwitcherButtons(lastIndex: Int) {
        arrowRightIcon.setOnClickListener {
            if (locationIndex != 0) {
                locationIndex--
                updateCalendarViewTime()
                updateLocationData()
                setUpFirstData()
            }
        }

        arrowLeftIcon.setOnClickListener {
            if (locationIndex != lastIndex) {
                locationIndex++
                updateCalendarViewTime()
                updateLocationData()
                setUpFirstData()
            }
        }
    }

    private fun updateLocationData() {
        viewModel.getHistoryDataList()[locationIndex].apply {
            locationTextView.text = location.name

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == Constants.STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] != -1)
                requireView().cvsExportTextView.performClick()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}