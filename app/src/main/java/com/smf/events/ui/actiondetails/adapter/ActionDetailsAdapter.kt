package com.smf.events.ui.actiondetails.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.R
import com.smf.events.helper.AppConstants
import com.smf.events.ui.actiondetails.model.ActionDetails
import com.smf.events.ui.bidrejectiondialog.BidRejectionDialogFragment
import com.smf.events.ui.quotedetailsdialog.QuoteDetailsDialog
import java.time.Month

class ActionDetailsAdapter(val context: Context, var bidStatus: String) :
    RecyclerView.Adapter<ActionDetailsAdapter.ActionDetailsViewHolder>() {

    private var myEventsList = ArrayList<ActionDetails>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ActionDetailsViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.event_details_card_view, parent, false)

        return ActionDetailsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ActionDetailsViewHolder, position: Int) {

        holder.onBind(myEventsList[position])
        holder.details(myEventsList[position], holder)
    }


    override fun getItemCount(): Int {
        return myEventsList.size
    }

    //Method For Refreshing Invoices
    @SuppressLint("NotifyDataSetChanged")
    fun refreshItems(invoice: List<ActionDetails>) {
        myEventsList.clear()
        myEventsList.addAll(invoice)
        notifyDataSetChanged()
    }

    inner class ActionDetailsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var eventName: TextView = view.findViewById(R.id.event_title_text)
        var amount: TextView = view.findViewById(R.id.amount_text)
        var code: TextView = view.findViewById(R.id.code_text)
        var eventType: TextView = view.findViewById(R.id.event_type_text)
        var eventDate: TextView = view.findViewById(R.id.event_date)
        var serviceDate: TextView = view.findViewById(R.id.service_date)
        var unlikeButton: ImageView = view.findViewById(R.id.unlike_imageView)
        var likeButton: ImageView = view.findViewById(R.id.like_imageView)
        var rightArrowButton: ImageView = view.findViewById(R.id.right_arrow_imageView)
        var cutoffMonthText: TextView = view.findViewById(R.id.cutoff_month_text)
        var progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
        var progressDateNumber: TextView = view.findViewById(R.id.progress_date_number)
        var unlikeButtonFade: ImageView = view.findViewById(R.id.unlike_imageView_fade)
        var likeButtonFade: ImageView = view.findViewById(R.id.like_imageView_fade)
        var changeOfMind: TextView = view.findViewById(R.id.change_of_mind)

        @SuppressLint("SetTextI18n")
        fun onBind(actionDetails: ActionDetails) {
            // 2354
            val currencyType = setCurrencyType(actionDetails)
            setCost(actionDetails, currencyType)
            eventName.text = actionDetails.eventName
            eventType.text = "${actionDetails.branchName} - ${actionDetails.serviceName}"
            code.text = actionDetails.eventServiceDescriptionId.toString()
            progressBar.progress = actionDetails.timeLeft.toInt()
            eventDate.text = dateFormat(actionDetails.eventDate)
            serviceDate.text = dateFormat(actionDetails.serviceDate)
            cutoffMonthText.text = dateFormat(actionDetails.serviceDate).substring(3, 6)
            progressDateNumber.text = dateFormat(actionDetails.biddingCutOffDate).substring(0, 2)
        }

        // Like and Dislike button
        fun details(position: ActionDetails, holder: ActionDetailsViewHolder) {
            if (bidStatus == AppConstants.BID_REJECTED) {
                //Button Visibility
                holder.buttonVisibility(holder)
                //Change of Mind For Submitting and quotel ater the Rejected  Bid
                holder.changeOfMind.setOnClickListener { holder.bidSubmitted(position) }

            }
            if (bidStatus == AppConstants.BID_SUBMITTED) {
                //Button Visibility
                holder.buttonVisibility(holder)
                //Change of Mind For Rejection the submitted Bid
                holder.changeOfMind.setOnClickListener { holder.bidRejection(position) }
            }
            //Like For Submitting the Bid
            holder.likeButton.setOnClickListener {
                holder.bidSubmitted(position)
            }
            //Unlike For Rejecting the Bid
            holder.unlikeButton.setOnClickListener {
                holder.bidRejection(position)
            }


        }

        //2354 - Method For Setting Quote Amount
        private fun setCost(actionDetails: ActionDetails, currencyType: String) {
            if (actionDetails.costingType == "Bidding") {
                if (actionDetails.latestBidValue.isNullOrEmpty()) {
                    amount.text = ""
                } else {
                    amount.text = "$currencyType${actionDetails.latestBidValue}"
                }
            } else {
                amount.text = "$currencyType${actionDetails.cost}"
            }
        }

        //2354 - Method For Setting CurrencyType
        private fun setCurrencyType(actionDetails: ActionDetails): String {
            val currencyType = if (actionDetails.currencyType == null) {
                "$"
            } else {
                when(actionDetails.currencyType) {
                    "USD($)" -> "$"
                    "GBP(\u00a3)" -> "\u00a3"
                    "INR(\u20B9)" -> "₹"
                    else -> {
                        "$"
                    }
                }
            }
            return currencyType
        }

        //Rejecting the Bids
        private fun bidRejection(position: ActionDetails) {
            var bidRequestId: Int = position.bidRequestId
            position.branchName
            BidRejectionDialogFragment.newInstance(
                bidRequestId,
                position.serviceName,
                position.eventServiceDescriptionId.toString()
            )
                .show(
                    (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                    BidRejectionDialogFragment.TAG
                )

        }

        //Submitting Bids
        private fun bidSubmitted(position: ActionDetails) {
            var bidRequestId: Int = position.bidRequestId
            var costingType: String = position.costingType
            var bidStatus: String = position.bidStatus
            var cost: String? = position.cost
            var latestBidValue: String? = position.latestBidValue
            var branchName: String = position.branchName
            var serviceName: String = position.serviceName

            val sharedPreferences =
                context.applicationContext.getSharedPreferences("MyUser", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putInt("bidRequestId", bidRequestId)
            editor.apply()

            if (costingType != "Bidding") {
                callBackInterface?.callBack(
                    "Bidding",
                    bidRequestId,
                    costingType,
                    bidStatus,
                    cost,
                    latestBidValue,
                    branchName
                )
            } else {
                QuoteDetailsDialog.newInstance(
                    bidRequestId,
                    costingType,
                    bidStatus,
                    cost,
                    latestBidValue,
                    branchName,
                    serviceName
                )
                    .show(
                        (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                        QuoteDetailsDialog.TAG
                    )
            }
        }

        //Button visibility
        private fun buttonVisibility(holder: ActionDetailsViewHolder) {
            holder.likeButton.visibility = View.INVISIBLE
            holder.unlikeButton.visibility = View.INVISIBLE
            holder.likeButtonFade.visibility = View.VISIBLE
            holder.unlikeButtonFade.visibility = View.VISIBLE
            holder.changeOfMind.visibility = View.VISIBLE
        }
    }

    // Method For Date And Month Arrangement To Display UI
    private fun dateFormat(input: String): String {
        var monthCount = input.substring(0, 2)
        val date = input.substring(3, 5)
        if (monthCount[0].digitToInt() == 0) {
            monthCount = monthCount[1].toString()
        }
        val month = Month.of(monthCount.toInt()).toString().substring(0, 3)
        return "$date $month"
    }

    private var callBackInterface: CallBackInterface? = null

    // Initializing CallBack Interface Method
    fun setCallBackInterface(callback: CallBackInterface) {
        callBackInterface = callback
    }

    // CallBack Interface
    interface CallBackInterface {
        fun callBack(
            status: String,
            bidRequestId: Int,
            costingType: String,
            bidStatus: String,
            cost: String?,
            latestBidValue: String?,
            branchName: String,
        )
    }

}