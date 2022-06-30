package com.smf.events.ui.commoninformationdialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.ViewModelProvider
import com.smf.events.R
import com.smf.events.base.BaseDialogFragment
import com.smf.events.databinding.CommonInformationDialogBinding
import com.smf.events.helper.AppConstants
import com.smf.events.ui.actiondetails.model.ActionDetails
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

// 2401
class CommonInfoDialog(var position: ActionDetails, var status: String) :
    BaseDialogFragment<CommonInformationDialogBinding, CommonInfoDialogViewModel>(),
    View.OnClickListener {

    companion object {
        const val TAG = "CommonInfoDialog"
        fun newInstance(position: ActionDetails, status: String): CommonInfoDialog {
            return CommonInfoDialog(position, status)
        }
    }

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModel(): CommonInfoDialogViewModel =
        ViewModelProvider(this, factory).get(CommonInfoDialogViewModel::class.java)

    override fun getBindingVariable(): Int = BR.commonInfoDialogViewModel

    override fun getContentView(): Int = R.layout.common_information_dialog

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onStart() {
        super.onStart()
        //Setting the dialog size
        dialogFragmentSize()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (status == "cost") {
            mDataBinding?.btnCancel?.setOnClickListener(this)
            mDataBinding?.btnOk?.setOnClickListener(this)
        } else {
            // 2904 Changes made for Start Service flow in Won Bid
            mDataBinding?.textInformation?.text = getText(R.string.start_service_text)
            mDataBinding?.btnOk?.setOnClickListener {
                var status = true
                parentFragmentManager.setFragmentResult(
                    AppConstants.WON_BID, // Same request key ActionDetailsFragment used to register its listener
                    bundleOf(
                        "status" to status,
                    ) // The data to be passed to ActionDetailsFragment
                )
                dismiss()
            }
            mDataBinding?.btnCancel?.setOnClickListener { dismiss() }
        }
    }

    // Setting Dialog Fragment Size
    private fun dialogFragmentSize() {
        var window: Window? = dialog?.window
        var params: WindowManager.LayoutParams = window!!.attributes
        params.width = ((resources.displayMetrics.widthPixels * 0.9).toInt())
        window.attributes = params
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_ok -> {
                parentFragmentManager.setFragmentResult(
                    "fromCommonInfoDialog", // Same request key ActionDetailsFragment used to register its listener
                    bundleOf(
                        "bidRequestId" to position.bidRequestId,
                        "costingType" to position.costingType,
                        "bidStatus" to position.bidStatus,
                        "cost" to position.cost,
                        "latestBidValue" to position.latestBidValue,
                        "branchName" to position.branchName
                    ) // The data to be passed to ActionDetailsFragment
                )
                dismiss()
            }
            R.id.btn_cancel -> {
                dismiss()
            }
        }
    }
}