package com.smf.events.ui.dashboard

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.smf.events.base.BaseViewModel
import com.smf.events.databinding.FragmentDashBoardBinding
import com.smf.events.helper.AppConstants
import com.smf.events.ui.dashboard.model.Datas
import com.smf.events.ui.dashboard.model.MyEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class DashBoardViewModel @Inject constructor(
    private val dashBoardRepository: DashBoardRepository,
    application: Application,
) : BaseViewModel(application), AdapterView.OnItemSelectedListener {

    var name: String? = null
    var allServiceposition: Int? = 0

    @SuppressLint("ResourceType")
    fun allServices(mDataBinding: FragmentDashBoardBinding?, resources: ArrayList<String>) {

        val spin = mDataBinding!!.spnAllServices
        spin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long,
            ) {
                Log.d(
                    "TAG",
                    "onItemSelected method listener: $position, $name, $allServiceposition"
                )
                allServiceposition = position
                callBackInterface?.itemClick(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val ad: ArrayAdapter<String> =
            ArrayAdapter<String>(getApplication(), com.smf.events.R.layout.spinners_list, resources)
        // set simple layout resource file
        // for each item of spinner
        ad.setDropDownViewResource(
            com.smf.events.R.layout.spinners_list
        )

        // Set the ArrayAdapter (ad) data on the
        // Spinner which binds data to spinner
        spin.adapter = ad
    }

    @SuppressLint("ResourceType")
    fun branches(
        mDataBinding: FragmentDashBoardBinding?,
        resources: ArrayList<String>,
        idToken: String,
        spRegId: Int, serviceCategoryId: Int, serviceVendorOnboardingId: Int,
    ) {
        resources.forEach {
            name = it
        }

        var spin = mDataBinding!!.spnBranches

        spin.onItemSelectedListener = this

        var ad: ArrayAdapter<*> =
            ArrayAdapter<Any?>(
                getApplication(), com.smf.events.R.layout.spinners_list,
                resources as List<Any?>
            )
        ad.setDropDownViewResource(com.smf.events.R.layout.spinners_list)
        ad.notifyDataSetChanged()
        mDataBinding.spnBranches.adapter = ad
    }

    private var callBackInterface: CallBackInterface? = null

    // Initializing CallBack Interface Method
    fun setCallBackInterface(callback: CallBackInterface) {
        callBackInterface = callback
    }

    // CallBack Interface
    interface CallBackInterface {
        fun itemClick(msg: Int)
        fun branchItemClick(serviceVendorOnboardingId: Int, name: String?, allServiceposition: Int?)
        fun internetError(exception: String)
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position1: Int, p3: Long) {
        Log.d("TAG", "onItemSelected method: $position1, $name, $allServiceposition")
        callBackInterface?.branchItemClick(position1, name, allServiceposition)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    // Getting Service Provider Service List Counts
    fun getServiceCountList(data: Datas): ArrayList<MyEvents> {
        var list = ArrayList<MyEvents>()
        list.add(MyEvents("${data.activeServiceCount}", "Active"))
        list.add(MyEvents("${data.approvalPendingServiceCount}", "Pending"))
        list.add(MyEvents("${data.draftServiceCount}", "Draft"))
        list.add(MyEvents("${data.inactiveServiceCount}", "Inactive"))
        list.add(MyEvents("${data.rejectedServiceCount}", "Rejected"))

        return list
    }

    // Method For Getting Service Counts
    fun getServiceCount(idToken: String, spRegId: Int) = liveData(Dispatchers.IO) {
        try {
            emit(dashBoardRepository.getServiceCount(idToken, spRegId))
        } catch (e: Exception) {
            when (e) {
                is UnknownHostException -> {
                    Log.d("TAG", "getActionAndStatus: UnknownHostException $e")
                    viewModelScope.launch {
                        callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                    }
                }
                is ConnectException ->{
                    viewModelScope.launch {
                        callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                    }
                }
                else -> {}
            }
        }
    }

    // Method For Getting All Service
    fun getAllServices(idToken: String, spRegId: Int) = liveData(Dispatchers.IO) {
        try {
            emit(dashBoardRepository.getAllServices(idToken, spRegId))
        } catch (e: Exception) {
            when (e) {
                is UnknownHostException -> {
                    Log.d("TAG", "getActionAndStatus: UnknownHostException $e")
                    viewModelScope.launch {
                        callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                    }
                }
                is ConnectException ->{
                    viewModelScope.launch {
                        callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                    }
                }
                else -> {}
            }
        }
    }

    // Method For Getting Branches
    fun getServicesBranches(idToken: String, spRegId: Int, serviceCategoryId: Int) =
        liveData(Dispatchers.IO) {
            try {
                emit(dashBoardRepository.getServicesBranches(idToken, spRegId, serviceCategoryId))
            } catch (e: Exception) {
                when (e) {
                    is UnknownHostException -> {
                        Log.d("TAG", "getActionAndStatus: UnknownHostException $e")
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                    is ConnectException ->{
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                    else -> {}
                }
            }
        }


}