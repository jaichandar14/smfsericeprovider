package com.smf.events.ui.timeslot.deselectingdialog

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseDialogFragment
import com.smf.events.databinding.FragmentDeseletingDialogBinding
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.helper.Tokens
import com.smf.events.ui.timeslot.deselectingdialog.adaptor.DeselectedDialogAdaptor
import com.smf.events.ui.timeslot.deselectingdialog.model.ListData
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

// 2803 Deselecting and modify Dialog Fragment
class DeselectingDialogFragment(
    private var purpose: String,
    var timeSlot: String,
    var currentMonth: String
) :
    BaseDialogFragment<FragmentDeseletingDialogBinding, DeselectingDialogViewModel>() {

    private lateinit var adapter: DeselectedDialogAdaptor

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreference

    @Inject
    lateinit var tokens: Tokens

    companion object {
        const val TAG = "CustomDialogFragment"
        fun newInstance(
            purpose: String,
            timeSlot: String,
            currentMonth: String
        ): DeselectingDialogFragment {
            return DeselectingDialogFragment(purpose, timeSlot, currentMonth)
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
        if (purpose == AppConstants.DESELECTED) {
            var window: Window? = dialog?.window
            var params: WindowManager.LayoutParams = window!!.attributes
            params.width = ((resources.displayMetrics.widthPixels * 0.9).toInt())
            window.attributes = params
            //dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.setBackgroundDrawableResource(R.drawable.shape_dailog_curved)
            dialog?.window?.setLayout(1000, 560)
        } else {
            var window: Window? = dialog?.window
            var params: WindowManager.LayoutParams = window!!.attributes
            params.width = ((resources.displayMetrics.widthPixels * 0.9).toInt())
            window.attributes = params
            //dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.setBackgroundDrawableResource(R.drawable.shape_dailog_curved)
            dialog?.window?.setLayout(800, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            mDataBinding?.cancelBtn?.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (purpose == AppConstants.DESELECTED) {
            deselectedDialog()
        } else if (purpose == "sele") {
            modifyDialog()
        }

        okBtnClick()
        cancelBtnClick()
    }

    // 2801 - method For OkButton
    private fun okBtnClick() {
        mDataBinding?.let {
            it.okBtn.setOnClickListener {
//    TODO OK BUTTON API CALL
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
        mDataBinding?.txTitle?.text =
            getString(R.string.event_booked_on_the_below_mentioned_dates_hence_you_can_not_modify_the_3pm_6pm_slots_availability)
        mDataBinding?.cancelBtn?.visibility = View.GONE
        mDataBinding?.listView?.visibility = View.VISIBLE
        var listData: ArrayList<ListData> = ArrayList()
        listData.add(
            ListData("AUG 19", "jaibday", "6pm - 9pm", "chennai")
        )
        listData.add(
            ListData("AUG 19", "jaibday", "9pm - 12pm", "chennai")
        )
        listData.add(
            ListData("AUG 19", "jaibday", "9pm - 12pm", "chennai")
        )
        adapter = DeselectedDialogAdaptor(listData, requireContext())
        mDataBinding?.listView?.adapter = adapter
    }

    // 2803 Method for Deselection Dialog
    private fun deselectedDialog() {
        mDataBinding?.txTitle?.text =
            getString(R.string.you_are_deselecting) + " " + timeSlot + " " + getString(R.string.you_are_deselecting_first) + " " + currentMonth + "." + " " + getString(
                R.string.you_are_deselecting_second
            )
    }

}
