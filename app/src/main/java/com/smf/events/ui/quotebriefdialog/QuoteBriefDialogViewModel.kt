package com.smf.events.ui.quotebriefdialog

import android.app.Application
import android.graphics.Color
import android.view.View
import androidx.lifecycle.liveData
import com.smf.events.base.BaseDialogViewModel
import com.smf.events.databinding.FragmentQuoteBriefBinding
import com.smf.events.databinding.QuoteBriefDialogBinding
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class QuoteBriefDialogViewModel @Inject constructor(
    private val quoteBriefDialogRepository: QuoteBriefDialogRepository,
    application: Application,
) : BaseDialogViewModel(application) {

    fun expandableView(mDataBinding: QuoteBriefDialogBinding?, expand: Boolean) {
        var exp = false
        val isExpandable: Boolean = expand

        mDataBinding!!.expBtn.setOnClickListener {
            if (isExpandable == exp) {
                mDataBinding.expandableView.visibility = View.VISIBLE
            } else {
                mDataBinding.expandableView.visibility = View.GONE
            }
            exp = !exp
        }
    }

    fun progress2Completed(mDataBinding: FragmentQuoteBriefBinding?) {
        mDataBinding!!.check2Complete.visibility = View.VISIBLE
        mDataBinding.check3Inprogress.visibility = View.VISIBLE
        mDataBinding.processflow2.setBackgroundColor(Color.BLACK)
    }

    fun progress3Completed(mDataBinding: FragmentQuoteBriefBinding?) {
        mDataBinding!!.check3Completed.visibility = View.VISIBLE
        mDataBinding.check4Inprogress.visibility = View.VISIBLE
        mDataBinding.processflow3.setBackgroundColor(Color.BLACK)
    }

    fun progress4Completed(mDataBinding: FragmentQuoteBriefBinding?) {
        mDataBinding!!.check4Completed.visibility = View.VISIBLE
    }

    fun getQuoteBrief(idToken: String, bidRequestId: Int) = liveData(
        Dispatchers.IO
    ) {
        emit(quoteBriefDialogRepository.getQuoteBrief(idToken, bidRequestId))
    }

    fun getViewQuote(idToken: String, bidRequestId: Int) = liveData(
        Dispatchers.IO
    ) {
        emit(quoteBriefDialogRepository.getViewQuote(idToken, bidRequestId))
    }
}