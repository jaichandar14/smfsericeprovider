package com.smf.events.ui.schedulemanagement

import android.R
import android.annotation.SuppressLint
import android.app.Application
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.liveData
import com.smf.events.base.BaseViewModel
import com.smf.events.databinding.FragmentCalendarBinding
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class ScheduleManagementViewModel @Inject constructor(
    private val scheduleManagementRepository: ScheduleManagementRepository,
    application: Application,
) : BaseViewModel(application) {

    var name: String? = null
    var allServiceposition: Int? = 0

    // 2458 Method for  Setting All Service
    @SuppressLint("ResourceType")
    fun allServices(
        mViewDataBinding: FragmentCalendarBinding?,
        allServiceList: ArrayList<String>,
    ) {
        mViewDataBinding?.spnAllServices?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long,
                ) {
                    allServiceposition = position
                    callBackInterface?.itemClick(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        val ad: ArrayAdapter<String> =
            ArrayAdapter<String>(getApplication(), R.layout.simple_spinner_item, allServiceList)
        // 2458 set simple layout resource file for each item of spinner
        ad.setDropDownViewResource(
            R.layout.simple_spinner_dropdown_item
        )
        // 2458 Set the ArrayAdapter (ad) data on the Spinner which binds data to spinner
        mViewDataBinding?.spnAllServices?.adapter = ad
    }

    // 2458 Method for  Setting Branches
    @SuppressLint("ResourceType")
    fun branches(
        mDataBinding: FragmentCalendarBinding?,
        branchData: java.util.ArrayList<String>,
        idToken: String,
        spRegId: Int,
        serviceCategoryId: Int,
        i: Int,
    ) {
        var spin = mDataBinding!!.spnBranches
        spin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long,
            ) {
                callBackInterface?.branchItemClick(position, name, allServiceposition)

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        var ad: ArrayAdapter<*> =
            ArrayAdapter<Any?>(
                getApplication(), R.layout.simple_spinner_item,
                branchData as List<Any?>
            )
        ad.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        ad.notifyDataSetChanged()
        mDataBinding.spnBranches.adapter = ad
    }

    // 2458 Method for  Setting Years
    @SuppressLint("ResourceType")
    fun year(
        mDataBinding: FragmentCalendarBinding?,
        calendarUtils: ArrayList<String>,
    ) {
        var spin = mDataBinding!!.monthYearspn
        spin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long,
            ) {
                allServiceposition = position
                // callBackInterface?.itemClick(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        var ad: ArrayAdapter<*> =
            ArrayAdapter<Any?>(
                getApplication(), R.layout.simple_spinner_item,
                calendarUtils as List<Any?>
            )
        ad.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        ad.notifyDataSetChanged()
        mDataBinding.monthYearspn.adapter = ad
    }

    // 2458 Method For Getting All Service
    fun getAllServices(idToken: String, spRegId: Int) = liveData(Dispatchers.IO) {
        emit(scheduleManagementRepository.getAllServices(idToken, spRegId))
    }

    // 2458 Method For Getting Branches
    fun getServicesBranches(idToken: String, spRegId: Int, serviceCategoryId: Int) =
        liveData(Dispatchers.IO) {
            emit(scheduleManagementRepository.getServicesBranches(idToken,
                spRegId,
                serviceCategoryId))
        }

    private var callBackInterface: CallBackInterface? = null

    // 2458 Initializing CallBack Interface Method
    fun setCallBackInterface(callback: CallBackInterface) {
        callBackInterface = callback
    }

    // 2458 CallBack Interface
    interface CallBackInterface {
        fun itemClick(msg: Int)
        fun branchItemClick(serviceVendorOnboardingId: Int, name: String?, allServiceposition: Int?)
    }
}