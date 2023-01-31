package com.smf.events.ui.timeslot.deselectingdialog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.base.BaseDialogFragment
import com.smf.events.databinding.FragmentDeseletingDialogBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.helper.Tokens
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.schedulemanagement.ScheduleManagementActivity
import com.smf.events.ui.timeslot.deselectingdialog.adaptor.DeselectedDialogAdaptor
import com.smf.events.ui.timeslot.deselectingdialog.model.ListData
import com.smf.events.ui.timeslotmodifyexpanablelist.model.BookedEventServiceDtoModify
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Month
import java.util.*
import javax.inject.Inject

// 2803 Deselecting and modify Dialog Fragment
class DeselectingDialogFragment(
    private var classTag: String,
    private var purpose: String,
    var timeSlot: String,
    var currentMonth: String,
    private var serviceVendorOnBoardingId: Int,
    private var fromDate: String,
    private var toDate: String,
    private var statusList: List<BookedEventServiceDtoModify>?
) : BaseDialogFragment<FragmentDeseletingDialogBinding, DeselectingDialogViewModel>(),
    Tokens.IdTokenCallBackInterface {

    private lateinit var adapter: DeselectedDialogAdaptor
    var spRegId: Int = 0
    lateinit var idToken: String
    private var roleId: Int = 0
    private var isAvailable: Boolean = false
    private lateinit var dialogDisposable: Disposable

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreference

    @Inject
    lateinit var tokens: Tokens

    companion object {
        const val TAG = "CustomDialogFragment"
        fun newInstance(
            classTag: String,
            purpose: String,
            timeSlot: String,
            currentMonth: String,
            serviceVendorOnBoardingId: Int,
            fromDate: String,
            toDate: String,
            statusList: List<BookedEventServiceDtoModify>?
        ): DeselectingDialogFragment {
            return DeselectingDialogFragment(
                classTag,
                purpose,
                timeSlot,
                currentMonth,
                serviceVendorOnBoardingId,
                fromDate,
                toDate,
                statusList
            )
        }
    }

    override fun getViewModel(): DeselectingDialogViewModel =
        ViewModelProvider(this, factory).get(
            DeselectingDialogViewModel::class.java
        )

    override fun getBindingVariable(): Int = BR.deselectingViewModel

    override fun getContentView(): Int = R.layout.fragment_deseleting_dialog

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onStart() {
        super.onStart()
        if (purpose == AppConstants.DESELECTED || purpose == AppConstants.NULL_TO_SELECT) {
            val window: Window? = dialog?.window
            val params: WindowManager.LayoutParams = window!!.attributes
            params.width = ((resources.displayMetrics.widthPixels * 0.9).toInt())
            window.attributes = params
            //dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.setBackgroundDrawableResource(R.drawable.shape_dailog_curved)
            // dialog?.window?.setLayout(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        } else {
            val window: Window? = dialog?.window
            val params: WindowManager.LayoutParams = window!!.attributes
            params.width = ((resources.displayMetrics.widthPixels * 0.9).toInt())
            window.attributes = params
            //dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.setBackgroundDrawableResource(R.drawable.shape_dailog_curved)
            //  dialog?.window?.setLayout(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            mDataBinding?.cancelBtn?.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 2670 - Initialize Local Variables
        setIdTokenAndSpRegId()
        // 2670 - Token Class CallBack Initialization
        tokens.setCallBackInterface(this)

        dialogDisposable = RxBus.listen(RxEvent.InternetStatus::class.java).subscribe {
            Log.d(TAG, "onViewCreated: observer Deselecting")
//            internetErrorDialogOld.dismissDialog()
        }

        if (purpose == AppConstants.DESELECTED) {
            isAvailable = false
            deselectedDialog()
        } else if (purpose == AppConstants.NULL_TO_SELECT) {
            isAvailable = true
            nullToSelectDialog()
        } else if (purpose == AppConstants.SELECTED) {
            modifyDialog()
        } else if (purpose == AppConstants.DENY) {
            mDataBinding?.txTitle?.text = getString(R.string.deny_message)
            mDataBinding?.cancelBtn?.visibility = View.GONE
        } else if (purpose == AppConstants.EXPWeek) {
            mDataBinding?.txTitle?.text =
                getString(R.string.week_validtity_message) + getString(
                    R.string.try_week
                )
            mDataBinding?.cancelBtn?.visibility = View.GONE
        } else if (purpose == AppConstants.EXPMonth) {
            mDataBinding?.txTitle?.text =
                getString(R.string.month_validity_msd) + getString(
                    R.string.try_month
                )
            mDataBinding?.cancelBtn?.visibility = View.GONE
        }

        // Ok Button Click listener
        okBtnClick()
        // Cancel Button Click listener
        cancelBtnClick()
    }

    // 2814 - method For OkButton
    private fun okBtnClick() {
//        if (internetErrorDialogOld.checkInternetAvailable(requireContext())) {
        mDataBinding?.let {
            it.okBtn.setOnClickListener {
                if (purpose == AppConstants.DESELECTED) {
//                        if (internetErrorDialogOld.checkInternetAvailable(requireContext())) {
                    apiTokenValidation(AppConstants.DESELECTED)
//                        }
                } else if (purpose == AppConstants.NULL_TO_SELECT) {
//                        if (internetErrorDialogOld.checkInternetAvailable(requireContext())) {
                    apiTokenValidation(AppConstants.NULL_TO_SELECT)
//                        }
                } else if (purpose == AppConstants.SELECTED) {
                    dismiss()
                } else if (purpose == AppConstants.DENY) {
                    RxBus.publish(RxEvent.DenyStorage(true))
                    dismiss()
                } else if (purpose == AppConstants.EXPWeek) {
                    dismiss()
                } else if (purpose == AppConstants.EXPMonth) {
                    dismiss()
                }
            }
        }
//        }
    }

    private fun getModifyDaySlot(
        idToken: String,
        spRegId: Int,
        fromDate: String,
        isAvailable: Boolean,
        modifiedSlot: String,
        serviceVendorOnBoardingId: Int,
        toDate: String,
    ) {
        getViewModel().getModifyDaySlot(
            idToken, spRegId, fromDate, isAvailable, modifiedSlot,
            serviceVendorOnBoardingId,
            toDate
        ).observe(viewLifecycleOwner, androidx.lifecycle.Observer { apiResponse ->
            when (apiResponse) {
                is ApisResponse.Success -> {
                    Log.d(TAG, "success ModifyBookedEvent: ${apiResponse.response.data}")
                    // Passing Updated Status To DayModifyExpandableListFragment
                    callBackToModifyFragments()
                    dismiss()
                }
                is ApisResponse.CustomError -> {
                    Log.d(
                        TAG,
                        "check token result success ModifyBookedEvent exp: ${apiResponse.message}"
                    )
                }
                is ApisResponse.InternetError -> {
                    (requireActivity() as ScheduleManagementActivity).showInternetDialog(
                        apiResponse.message
                    )
                }
                else -> {}
            }
        })
    }

    private fun getModifyWeekSlot(
        idToken: String,
        spRegId: Int,
        fromDate: String,
        isAvailable: Boolean,
        modifiedSlot: String,
        serviceVendorOnBoardingId: Int,
        toDate: String,
    ) {
        getViewModel().getModifyWeekSlot(
            idToken, spRegId, fromDate, isAvailable, modifiedSlot,
            serviceVendorOnBoardingId,
            toDate
        ).observe(viewLifecycleOwner, androidx.lifecycle.Observer { apiResponse ->
            when (apiResponse) {
                is ApisResponse.Success -> {
                    Log.d(TAG, "success ModifyBookedEvent: ${apiResponse.response.data}")
                    // Passing Updated Status To DayModifyExpandableListFragment
                    callBackToModifyFragments()
                    dismiss()
                }
                is ApisResponse.CustomError -> {
                    Log.d(
                        TAG,
                        "check token result success ModifyBookedEvent exp: ${apiResponse.message}"
                    )
                }
                is ApisResponse.InternetError -> {
                    (requireActivity() as ScheduleManagementActivity).showInternetDialog(
                        apiResponse.message
                    )
                }
                else -> {
                }
            }
        })
    }

    private fun getModifyMonthSlot(
        idToken: String,
        spRegId: Int,
        fromDate: String,
        isAvailable: Boolean,
        modifiedSlot: String,
        serviceVendorOnBoardingId: Int,
        toDate: String,
    ) {
        getViewModel().getModifyMonthSlot(
            idToken, spRegId, fromDate, isAvailable, modifiedSlot,
            serviceVendorOnBoardingId,
            toDate
        ).observe(viewLifecycleOwner, androidx.lifecycle.Observer { apiResponse ->
            when (apiResponse) {
                is ApisResponse.Success -> {
                    Log.d(TAG, "success ModifyBookedEvent: ${apiResponse.response.data}")
                    // Passing Updated Status To DayModifyExpandableListFragment
                    callBackToModifyFragments()
                    dismiss()
                }
                is ApisResponse.CustomError -> {
                    Log.d(
                        TAG,
                        "check token result success ModifyBookedEvent exp: ${apiResponse.message}"
                    )
                }
                is ApisResponse.InternetError -> {
                    (requireActivity() as ScheduleManagementActivity).showInternetDialog(
                        apiResponse.message
                    )
                }
                else -> {}
            }
        })
    }

    private fun callBackToModifyFragments() {
        when (classTag) {
            AppConstants.DAY -> {
                RxBus.publish(RxEvent.ModifyDialog(AppConstants.DAY))
            }
            AppConstants.WEEK -> {
                RxBus.publish(RxEvent.ModifyDialog(AppConstants.WEEK))
            }
            AppConstants.MONTH -> {
                RxBus.publish(RxEvent.ModifyDialog(AppConstants.MONTH))
            }
        }
    }

    // 2801 - method For CancelButton
    private fun cancelBtnClick() {
        mDataBinding?.let {
            it.cancelBtn.setOnClickListener {
                dismiss()
            }
        }
    }

    // 2803  Modify Dialog Method
    private fun modifyDialog() {
        Log.d(TAG, "modifyDialog: $statusList")
        if (!statusList.isNullOrEmpty()) {
            mDataBinding?.txTitle?.text =
                getString(R.string.event_booked_on) + " " + timeSlot + " " + getString(R.string.slots_availability)
            mDataBinding?.cancelBtn?.visibility = View.GONE
            mDataBinding?.listView?.visibility = View.VISIBLE
            val listData: ArrayList<ListData> = ArrayList()

            statusList?.let { data ->
                data.forEach {
                    listData.add(
                        ListData(dateFormat(it.eventDate), it.eventName, timeSlot, it.branchName)
                    )
                }
            }
            adapter = DeselectedDialogAdaptor(listData, requireContext())
            mDataBinding?.listView?.adapter = adapter
        } else {
            mDataBinding?.txTitle?.text =
                getString(R.string.quote_sent_on) + " " + timeSlot + " " + getString(R.string.slots_availability)
        }

    }

    // 2803 Method for Deselection Dialog
    private fun deselectedDialog() {
        when (classTag) {
            AppConstants.DAY -> {
                mDataBinding?.txTitle?.text =
                    getString(R.string.you_are_deselecting) + " " + timeSlot + " " + getString(R.string.you_are_deselecting_first) + " " + fromDate + "." + " " + getString(
                        R.string.you_are_deselecting_second
                    )
            }
            AppConstants.WEEK -> {
                mDataBinding?.txTitle?.text =
                    getString(R.string.you_are_deselecting) + " " + timeSlot + " " + getString(R.string.entire_week) + " " + fromDate + " to " + toDate + "." + " " + getString(
                        R.string.you_are_deselecting_second
                    )
            }
            AppConstants.MONTH -> {
                mDataBinding?.txTitle?.text =
                    getString(R.string.you_are_deselecting) + " " + timeSlot + " " + getString(R.string.you_are_deselecting_month) + " " + currentMonth + "." + " " + getString(
                        R.string.you_are_deselecting_second
                    )
            }
        }

    }

    private fun nullToSelectDialog() {
        when (classTag) {
            AppConstants.DAY -> {
                mDataBinding?.txTitle?.text =
                    getString(R.string.you_are_selecting) + " " + timeSlot + " " + getString(R.string.you_are_deselecting_first) + " " + fromDate + "." + " " + getString(
                        R.string.you_are_deselecting_second
                    )
            }
            AppConstants.WEEK -> {
                mDataBinding?.txTitle?.text =
                    getString(R.string.you_are_selecting) + " " + timeSlot + " " + getString(R.string.entire_week) + " " + fromDate + " to " + toDate + "." + " " + getString(
                        R.string.you_are_deselecting_second
                    )
            }
            AppConstants.MONTH -> {
                mDataBinding?.txTitle?.text =
                    getString(R.string.you_are_selecting) + " " + timeSlot + " " + getString(R.string.you_are_deselecting_month) + " " + currentMonth + "." + " " + getString(
                        R.string.you_are_deselecting_second
                    )
            }
        }
    }

    // 2814 - Method For AWS Token Validation
    private fun apiTokenValidation(caller: String) {
        if (idToken.isNotEmpty()) {
            tokens.checkTokenExpiry(
                requireActivity().applicationContext as SMFApp,
                caller, idToken
            )
        }
    }

    // 2814 - Callback From Token Class
    override suspend fun tokenCallBack(idToken: String, caller: String) {
        view?.let {
            withContext(Dispatchers.Main) {
                if (caller == AppConstants.DESELECTED) {
                    callDeSelectedApi()
                } else if (caller == AppConstants.NULL_TO_SELECT) {
                    callNullToSelectApi()
                }
            }
        }
    }

    private fun callDeSelectedApi() {
        when (classTag) {
            AppConstants.DAY -> {
                getModifyDaySlot(
                    idToken,
                    spRegId,
                    fromDate,
                    isAvailable,
                    timeSlot,
                    serviceVendorOnBoardingId,
                    toDate
                )
            }
            AppConstants.WEEK -> {
                getModifyWeekSlot(
                    idToken,
                    spRegId,
                    fromDate,
                    isAvailable,
                    timeSlot,
                    serviceVendorOnBoardingId,
                    toDate
                )
            }
            AppConstants.MONTH -> {
                getModifyMonthSlot(
                    idToken,
                    spRegId,
                    fromDate,
                    isAvailable,
                    timeSlot,
                    serviceVendorOnBoardingId,
                    toDate
                )
            }
        }
    }

    private fun callNullToSelectApi() {
        when (classTag) {
            AppConstants.DAY -> {
                getModifyDaySlot(
                    idToken,
                    spRegId,
                    fromDate,
                    isAvailable,
                    timeSlot,
                    serviceVendorOnBoardingId,
                    toDate
                )
            }
            AppConstants.WEEK -> {
                getModifyWeekSlot(
                    idToken,
                    spRegId,
                    fromDate,
                    isAvailable,
                    timeSlot,
                    serviceVendorOnBoardingId,
                    toDate
                )
            }
            AppConstants.MONTH -> {
                getModifyMonthSlot(
                    idToken,
                    spRegId,
                    fromDate,
                    isAvailable,
                    timeSlot,
                    serviceVendorOnBoardingId,
                    toDate
                )
            }
        }
    }

    // 2814 - Method For Date And Month Arrangement To Display UI
    private fun dateFormat(input: String): String {
        var monthCount = input.substring(0, 2)
        val date = input.substring(3, 5)
        if (monthCount[0].digitToInt() == 0) {
            monthCount = monthCount[1].toString()
        }
        val month = Month.of(monthCount.toInt()).toString().substring(0, 3).let { month ->
            month.substring(0, 1) + month.substring(1, 2)
                .lowercase(Locale.getDefault()) + month.substring(2, 3)
                .lowercase(Locale.getDefault())
        }
        return "$month $date"
    }

    // 2814 - Method For Set IdToken And SpRegId From SharedPreferences
    private fun setIdTokenAndSpRegId() {
        spRegId = sharedPreference.getInt(SharedPreference.SP_REG_ID)
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
        roleId = sharedPreference.getInt(SharedPreference.ROLE_ID)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: called Deselecting")
        if (!dialogDisposable.isDisposed) dialogDisposable.dispose()
    }
}
