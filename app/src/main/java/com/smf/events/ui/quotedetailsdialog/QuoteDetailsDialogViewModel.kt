package com.smf.events.ui.quotedetailsdialog

import android.R
import android.annotation.SuppressLint
import android.app.Application
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.lifecycle.liveData
import com.smf.events.base.BaseDialogViewModel
import com.smf.events.databinding.FragmentQuoteDetailsDialogBinding
import com.smf.events.ui.quotedetailsdialog.model.BiddingQuotDto
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class QuoteDetailsDialogViewModel @Inject constructor(
    private val quoteDetailsRepository: QuoteDetailsRepository,
    application: Application,
) : BaseDialogViewModel(application) {

    var costposition: Int? = 0

    //When i have Quote is clicked
    fun iHaveQuoteClicked(view: View, mDataBinding: FragmentQuoteDetailsDialogBinding?) {
        var radioGroup: RadioGroup? = mDataBinding?.radiogroup
        onCLickOk(mDataBinding, "I have Quotes Details")
        mDataBinding?.ihavequote?.setOnClickListener {
            var selectId: Int? = radioGroup?.checkedRadioButtonId
            var quoteRadioButton: RadioButton = view.findViewById(selectId!!)
            if (selectId == -1) {
                // Toast.makeText(activity,"Nothing selected", Toast.LENGTH_SHORT).show();
            } else {
                mDataBinding.alertCost.visibility = View.INVISIBLE
                mDataBinding.constraint2.visibility = View.VISIBLE
                onCLickOk(mDataBinding, quoteRadioButton.text as String)
            }

        }
    }

    //When Quote Later is clicked
    fun quoteLaterIsClicked(view: View, mDataBinding: FragmentQuoteDetailsDialogBinding?) {
        var radioGroup: RadioGroup? = mDataBinding?.radiogroup
        mDataBinding?.quotelater?.setOnClickListener {
            var selectId: Int? = radioGroup?.checkedRadioButtonId
            var quoteRadioButton: RadioButton = view.findViewById(selectId!!)
            if (selectId == -1) {
                // Toast.makeText(activity,"Nothing selected", Toast.LENGTH_SHORT).show();
            } else {
                mDataBinding.alertCost.visibility = View.INVISIBLE
                mDataBinding.constraint2.visibility = View.GONE
                onCLickOk(mDataBinding, quoteRadioButton.text as String)
            }
        }
    }

    private fun onCLickOk(
        mDataBinding: FragmentQuoteDetailsDialogBinding?,
        quote: String,
    ) {
        mDataBinding?.btnOk?.setOnClickListener {
            if (mDataBinding.ihavequote.isChecked) {
                callBackInterface?.callBack("iHaveQuote")

            } else {
                callBackInterface?.callBack("quoteLater")
            }
        }

    }

    // Insert CurrencyType Spinner Value
    @SuppressLint("ResourceType")
    fun getCurrencyType(mDataBinding: FragmentQuoteDetailsDialogBinding?, resources: ArrayList<String>) {

        val spin = mDataBinding!!.currencyType
        // Spinner ClickListener
        spin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long,
            ) {
                costposition = position
                callBackInterface?.getCurrencyTypePosition(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val ad: ArrayAdapter<String> =
            ArrayAdapter<String>(getApplication(), R.layout.simple_spinner_item, resources)
        ad.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        // Spinner which binds data to spinner
        spin.adapter = ad

    }

    private var callBackInterface: CallBackInterface? = null

    // Initializing CallBack Interface Method
    fun setCallBackInterface(callback: CallBackInterface) {
        callBackInterface = callback
    }

    // CallBack Interface
    interface CallBackInterface {
        fun callBack(status: String)
        fun getCurrencyTypePosition(position: Int)
    }

    fun postQuoteDetails(idToken: String, bidRequestId: Int, biddingQuote: BiddingQuotDto) =
        liveData(Dispatchers.IO) {
            emit(quoteDetailsRepository.postQuoteDetails(idToken, bidRequestId, biddingQuote))
        }
}