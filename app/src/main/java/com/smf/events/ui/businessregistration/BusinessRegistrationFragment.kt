package com.smf.events.ui.businessregistration

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.FragmentBusinessRegistrationBinding
import com.smf.events.ui.addservicedialog.AddServiceDialog
import com.smf.events.ui.approvaldialog.ApprovalDialog
import com.smf.events.ui.businessregistration.adapter.AddServiceAdapter
import com.smf.events.ui.businessregistration.adapter.BusinessInformationAdaptor
import com.smf.events.ui.businessregistration.adapter.BusinessOwnerInformationAdapter
import com.smf.events.ui.businessregistration.adapter.OtherInformationAdapter
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class BusinessRegistrationFragment :
    BaseFragment<FragmentBusinessRegistrationBinding, BusinessRegistrationViewModel>() {

    private lateinit var businessInformationAdaptor: BusinessInformationAdaptor
    private lateinit var businessInformationRecyclerView: RecyclerView

    private lateinit var otherInformationAdaptor: OtherInformationAdapter
    private lateinit var otherInfoRecyclerView: RecyclerView

    private lateinit var businessOwnerInformationAdapter: BusinessOwnerInformationAdapter
    private lateinit var businessRegRecyclerView: RecyclerView

    private lateinit var addServiceAdapter: AddServiceAdapter
    private lateinit var addServiceAdapterRecyclerView: RecyclerView


    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModel(): BusinessRegistrationViewModel =
        ViewModelProvider(this, factory).get(BusinessRegistrationViewModel::class.java)

    override fun getBindingVariable(): Int = BR.businessRegistrationViewModel

    override fun getContentView(): Int = R.layout.fragment_business_registration

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
//        if (checkInternetAvailable()) {
            //Business Registration
            businessRegistrationRecyclerview()
            //Business information
            businessInformationRecyclerview()
            //Other Information
            otherInformationRecyclerview()
            //Add Services
            addServicesRecyclerview()

            mDataBinding?.approvalBtn?.setOnClickListener {
                //Calling Approval Dialog Frgment
                ApprovalDialog.newInstance().show(
                    (context as FragmentActivity).supportFragmentManager,
                    ApprovalDialog.TAG
                )
            }
//        }
    }

    private fun businessInformationRecyclerview() {
        businessInformationRecyclerView = mDataBinding?.businessInfoRecycleview!!
        businessInformationAdaptor = BusinessInformationAdaptor()
        businessInformationRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        businessInformationRecyclerView.adapter = businessInformationAdaptor
        businessInformationAdaptor.notifyDataSetChanged()
    }

    private fun otherInformationRecyclerview() {
        otherInfoRecyclerView = mDataBinding?.otherInfoRecycleview!!
        otherInformationAdaptor = OtherInformationAdapter()
        otherInfoRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        otherInfoRecyclerView.adapter = otherInformationAdaptor
        otherInformationAdaptor.notifyDataSetChanged()
    }

    private fun businessRegistrationRecyclerview() {
        businessRegRecyclerView = mDataBinding?.businessOwnerInfoRecycleview!!
        businessOwnerInformationAdapter = BusinessOwnerInformationAdapter()
        businessRegRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        businessRegRecyclerView.adapter = businessOwnerInformationAdapter
        businessOwnerInformationAdapter.notifyDataSetChanged()
    }

    private fun addServicesRecyclerview() {
        addServiceAdapterRecyclerView = mDataBinding?.addServicesRecycleview!!
        addServiceAdapter = AddServiceAdapter(requireContext())
        addServiceAdapterRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        addServiceAdapterRecyclerView.adapter = addServiceAdapter
        addServiceAdapter.notifyDataSetChanged()
    }

}