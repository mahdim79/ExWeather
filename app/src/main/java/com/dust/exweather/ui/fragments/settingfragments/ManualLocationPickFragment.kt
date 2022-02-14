package com.dust.exweather.ui.fragments.settingfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import com.dust.exweather.R
import com.dust.exweather.model.dataclasses.location.SearchLocation
import com.dust.exweather.model.dataclasswrapper.DataWrapper
import com.dust.exweather.utils.DataStatus
import kotlinx.android.synthetic.main.fragment_manual_location_pick.*
import java.util.*

class ManualLocationPickFragment(
    private val dataLiveData: LiveData<DataWrapper<ArrayList<SearchLocation>>>,
    private val onManualLocationPickFragmentDestroyed:() -> Unit,
    private val onLocationSelected: (SearchLocation?) -> Unit
) :
    Fragment() {

    private lateinit var arrayAdapter: ArrayAdapter<String>
    private val dataList = arrayListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manual_location_pick, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListView()
        observeDataLiveData()
    }

    private fun observeDataLiveData() {
        dataLiveData.observe(viewLifecycleOwner) { newData ->
            dataList.clear()
            arrayAdapter.notifyDataSetChanged()
            when (newData.status) {
                DataStatus.DATA_RECEIVE_LOADING -> {
                    searchProgressBar.visibility = View.VISIBLE
                }
                DataStatus.DATA_RECEIVE_SUCCESS -> {
                    searchProgressBar.visibility = View.INVISIBLE
                    newData.data?.let { data ->
                        for (i in data.indices)
                            if (i < 9)
                                dataList.add("${data[i].name}, ${data[i].country}, ${data[i].region}")
                    }
                    arrayAdapter.notifyDataSetChanged()
                }
                DataStatus.DATA_RECEIVE_FAILURE -> {
                    searchProgressBar.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(), "خطایی پیش آمده است", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun setupListView() {
        arrayAdapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, dataList)
        locationsListView.adapter = arrayAdapter
        locationsListView.setOnItemClickListener { _, _, i, _ ->

            dataLiveData.value?.data?.let { data ->
                onLocationSelected(data[i])
            }

        }
    }

    override fun onDestroyView() {
        onManualLocationPickFragmentDestroyed()
        super.onDestroyView()
    }

}