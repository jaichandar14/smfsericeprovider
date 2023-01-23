package com.smf.events.ui.commoninformationdialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.smf.events.MainActivity
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.base.BaseDialogFragment
import com.smf.events.databinding.CommonInformationDialogBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.helper.SharedPreference.Companion.isDialogShown
import com.smf.events.helper.Tokens
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.actiondetails.model.ActionDetails
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

// 2401
class CommonInfoDialog(
    var position: ActionDetails,
    var status: String
) : BaseDialogFragment<CommonInformationDialogBinding, CommonInfoDialogViewModel>(),
    View.OnClickListener, Tokens.IdTokenCallBackInterface {

    companion object {
        const val TAG = "CommonInfoDialog"
        fun newInstance(
            position: ActionDetails,
            status: String
        ): CommonInfoDialog {
            return CommonInfoDialog(position, status)
        }
    }

    lateinit var idToken: String
    private lateinit var dialogDisposable: Disposable

    @Inject
    lateinit var tokens: Tokens

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreference
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
        isDialogShown = true
        // 2904 SharedPreference token
        setIdTokenAndBidReqId()
        // 2904 Setting the dialog size
        dialogFragmentSize()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // token CallBackInterface
        tokens.setCallBackInterface(this)

        dialogDisposable = RxBus.listen(RxEvent.InternetStatus::class.java).subscribe {
            Log.d(TAG, "onViewCreated: observer Common dialog")
        }

        init()
    }

    private fun init() {
        if (status == "cost") {
            mDataBinding?.btnCancel?.setOnClickListener(this)
            mDataBinding?.btnOk?.setOnClickListener(this)
        } else if (status == AppConstants.SERVICE_DONE) {
            // 2904 Changes made for Intitate closer flow in Won Bid
            mDataBinding?.textInformation?.text = getText(R.string.initiate_closer_text)
            mDataBinding?.btnOk?.setOnClickListener {
//                if (internetErrorDialogOld.checkInternetAvailable(requireContext())) {
                apiTokenValidationBidActions(AppConstants.SERVICE_DONE)
                Log.d(TAG, "onViewCreated: ${position.eventServiceDescriptionId}")
//                }
            }
            mDataBinding?.btnCancel?.setOnClickListener { dismiss() }
        } else {
            // 2904 Changes made for Start Service flow in Won Bid
            mDataBinding?.textInformation?.text = getText(R.string.start_service_text)
            mDataBinding?.btnOk?.setOnClickListener {
//                if (internetErrorDialogOld.checkInternetAvailable(requireContext())) {
                apiTokenValidationBidActions(AppConstants.SERVICE_IN_PROGRESS)
                Log.d(TAG, "onViewCreated: ${position.eventServiceDescriptionId}")
//                }
            }
            mDataBinding?.btnCancel?.setOnClickListener { dismiss() }
        }
    }

    // 2904 Method For AWS Token Validation
    private fun apiTokenValidationBidActions(s: String) {
        tokens.checkTokenExpiry(
            requireActivity().applicationContext as SMFApp,
            s, idToken
        )
    }

    // 2904 Callback From Token Class
    override suspend fun tokenCallBack(idToken: String, caller: String) {
        withContext(Dispatchers.Main) {
            when (caller) {
                // 2904
                AppConstants.SERVICE_DONE -> updateServiceStatus(idToken, AppConstants.SERVICE_DONE)
                AppConstants.SERVICE_IN_PROGRESS -> updateServiceStatus(
                    idToken,
                    AppConstants.SERVICE_IN_PROGRESS
                )
            }
        }
    }

    // 2904 Start service and Initiate Closer Api implementation
    private fun updateServiceStatus(idToken: String, services: String) {
        Log.d(TAG, "updateServiceStatus: $services")
        getViewModel().updateServiceStatus(
            idToken,
            position.bidRequestId,
            position.eventId,
            position.eventServiceDescriptionId,
            services
        ).observe(viewLifecycleOwner, Observer { apiResponse ->
            when (apiResponse) {
                is ApisResponse.Success -> {
                    actionDetailsFragmentListUpdate()
                    dismiss()
                    Log.d(TAG, "updateServiceStatus: updated")
                }
                is ApisResponse.CustomError -> {
                    Log.d("TAG", "check token result: ${apiResponse.message}")
                }
                is ApisResponse.InternetError -> {
                    (requireActivity() as MainActivity).showInternetDialog(apiResponse.message)
                }
                else -> {}
            }
        })
    }

    // Method For Send Data To actionDetails Fragment
    private fun actionDetailsFragmentListUpdate() {
        // Result to Send ActionDetails Fragment
        parentFragmentManager.setFragmentResult(
            "2", // Same request key ActionDetailsFragment used to register its listener
            bundleOf("key" to "value") // The data to be passed to ActionDetailsFragment
        )
    }

    // Setting Dialog Fragment Size
    private fun dialogFragmentSize() {
        val window: Window? = dialog?.window
        val params: WindowManager.LayoutParams = window!!.attributes
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

    // Setting IDToken
    private fun setIdTokenAndBidReqId() {
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: called Common dialog")
        isDialogShown = false
        if (!dialogDisposable.isDisposed) dialogDisposable.dispose()
    }
}