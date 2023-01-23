package com.smf.events.ui.actiondetails.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.R
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.ui.actiondetails.model.ActionDetails
import com.smf.events.ui.bidrejectiondialog.BidRejectionDialogFragment
import com.smf.events.ui.commoninformationdialog.CommonInfoDialog
import com.smf.events.ui.quotedetailsdialog.QuoteDetailsDialog
import com.smf.events.ui.vieworderdetails.ViewOrderDetailsDialogFragment
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter

class ActionDetailsAdapter(
    val context: Context,
    var bidStatus: String,
    val sharedPreference: SharedPreference,
    var status: Boolean?,
) : RecyclerView.Adapter<ActionDetailsAdapter.ActionDetailsViewHolder>() {
    private var myEventsList = ArrayList<ActionDetails>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ActionDetailsViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.detailscardview, parent, false)
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
        var rightArrowButton: ImageView = view.findViewById(R.id.imageView)
        var cutoffMonthText: TextView = view.findViewById(R.id.cutoff_month_text)
        var progressBar: ProgressBar = view.findViewById(R.id.time_left_progress_bar)
        var progressDateNumber: TextView = view.findViewById(R.id.progress_date_number)
        var changeOfMind: TextView = view.findViewById(R.id.change_of_mind)
        var startserviceBtn: TextView = view.findViewById(R.id.btn_start_service)
        var quote_status_tx: TextView = view.findViewById(R.id.quote_status)

        @SuppressLint("SetTextI18n")
        fun onBind(actionDetails: ActionDetails) {
            // 2354
            val currencyType = setCurrencyType(actionDetails)
            setCost(actionDetails, currencyType)
            eventName.text = actionDetails.eventName
            eventType.text = "${actionDetails.branchName} - ${actionDetails.serviceName}"
            code.text = actionDetails.eventServiceDescriptionId.toString()
            progressBar.progress = actionDetails.timeLeft.toInt()
            eventDate.text = (actionDetails.eventDate)
            serviceDate.text = (actionDetails.serviceDate)
            cutoffMonthText.text = dateFormat(actionDetails.serviceDate).substring(3, 6)
            progressDateNumber.text = dateFormat(actionDetails.biddingCutOffDate).substring(0, 2)
        }

        // Like and Dislike button
        fun details(position: ActionDetails, holder: ActionDetailsViewHolder) {
            if (bidStatus == AppConstants.BID_REJECTED) {
                //Button Visibility
                holder.buttonVisibility(holder)
                //Change in Mind For Submitting and quote after the Rejected Bid
                holder.changeOfMind.setOnClickListener { costingType(position, holder) }
            }
            if (bidStatus == AppConstants.BID_SUBMITTED) {
                // 2904
                widgetQuoteSent(holder, position)
            }
            // 2884 for won Bid flow
            if (bidStatus == AppConstants.WON_BID) {
                // 2904
                widgetWonBid(holder, position)
            }
            // 2885 for Lost Bid flow
            if (bidStatus == AppConstants.LOST_BID) {
                holder.likeButton.visibility = View.INVISIBLE
                holder.unlikeButton.visibility = View.INVISIBLE
            }
            // 2922 for Timed Out flow
            if (bidStatus == AppConstants.BID_TIMED_OUT) {
                holder.likeButton.visibility = View.INVISIBLE
                holder.unlikeButton.visibility = View.INVISIBLE
            }
            // 2885 for Lost Bid flow
            if (bidStatus == AppConstants.SERVICE_IN_PROGRESS) {
                // 2904
                widgetServiceProgress(holder, position)
            }
            if (bidStatus == AppConstants.SERVICE_DONE) {
                // 2904
                widgetServiceCloser(holder, position)
            }

            // Like For Submitting the Bid
            holder.likeButton.setOnClickListener {
                holder.bidSubmitted(position)
            }
            // Unlike For Rejecting the Bid
            holder.unlikeButton.setOnClickListener {
                holder.bidRejection(position)
            }
            // 2904
            // 2940 showing Order Deatils for Timed out,Lostbid and Oending for review
            if (bidStatus == AppConstants.BID_REQUESTED || bidStatus == AppConstants.BID_REJECTED || bidStatus == AppConstants.BID_TIMED_OUT || bidStatus == AppConstants.LOST_BID || bidStatus == AppConstants.PENDING_FOR_QUOTE) {
                // 2402 View Order details onCLickArrow Button
                holder.rightArrowButton.setOnClickListener {
                    // 2904 Method to show the Quote Details Status Dialog
                    orderDetailsDialog(position)
                }
                holder.quote_status_tx.setOnClickListener { orderDetailsDialog(position) }
            }
        }

        // 2904
        private fun widgetWonBid(holder: ActionDetailsViewHolder, position: ActionDetails) {
            holder.likeButton.visibility = View.INVISIBLE
            holder.unlikeButton.visibility = View.INVISIBLE
            val currentDateValue = LocalDateTime.now()
            val formatterDay = DateTimeFormatter.ofPattern("MM/dd/yyyy")
            val formattedDay: String = currentDateValue.format(formatterDay)
            // Restricting the start service flow based previous and today service Date
            if (position.serviceDate <= formattedDay) {
                holder.startserviceBtn.visibility = View.VISIBLE
            }
            holder.quote_status_tx.text = AppConstants.WON_BID_SMALL
            holder.quote_status_tx.setOnClickListener {
                callBackInterface?.showDialog(position)
            }
            holder.startserviceBtn.setOnClickListener {
                // 2904 Dialog to For confirmation of Start service
                CommonInfoDialog.newInstance(position, AppConstants.WON_BID).show(
                    (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                    CommonInfoDialog.TAG
                )
            }
            holder.rightArrowButton.setOnClickListener { callBackInterface?.showDialog(position) }
        }

        // 2922
        private fun widgetServiceCloser(
            holder: ActionDetailsViewHolder,
            position: ActionDetails,
        ) {
            holder.likeButton.visibility = View.INVISIBLE
            holder.unlikeButton.visibility = View.INVISIBLE
            holder.quote_status_tx.text = AppConstants.SERVICE_COMPLETED
            holder.quote_status_tx.setOnClickListener {
                callBackInterface?.showDialog(position)
            }
            holder.rightArrowButton.setOnClickListener { callBackInterface?.showDialog(position) }
        }


        // 2904
        private fun widgetServiceProgress(
            holder: ActionDetailsViewHolder,
            position: ActionDetails,
        ) {
            holder.likeButton.visibility = View.INVISIBLE
            holder.unlikeButton.visibility = View.INVISIBLE
            holder.quote_status_tx.text = AppConstants.SERVICE_IN_PROGRESS_SMALL
            holder.quote_status_tx.setOnClickListener {
                callBackInterface?.showDialog(position)
            }
            holder.startserviceBtn.visibility = View.VISIBLE
            holder.startserviceBtn.text = AppConstants.INITIATE_CLOSER
            holder.startserviceBtn.setOnClickListener {
                // 2904 Dialog to For confirmation of Start service
                CommonInfoDialog.newInstance(position, AppConstants.SERVICE_DONE).show(
                    (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                    CommonInfoDialog.TAG
                )
            }
            holder.rightArrowButton.setOnClickListener { callBackInterface?.showDialog(position) }
        }

        // 2904
        private fun widgetQuoteSent(holder: ActionDetailsViewHolder, position: ActionDetails) {
            //Button Visibility
            holder.buttonVisibility(holder)
            holder.quote_status_tx.text = AppConstants.BIDDING_IN_PROGRESS
            holder.quote_status_tx.setOnClickListener {
                // 2904 Dialog Fragement Which show the Status for the Bid Submitted
                callBackInterface?.showDialog(position)
            }
            //Change of Mind For Rejection the submitted Bid
            holder.changeOfMind.setOnClickListener { holder.bidRejection(position) }
        }

        // 2904  Method for order Details
        private fun orderDetailsDialog(position: ActionDetails) {
            ViewOrderDetailsDialogFragment.newInstance(
                position.eventId,
                position.eventServiceDescriptionId,
                position.eventDate,
                position.eventName
            ).show(
                (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                ViewOrderDetailsDialogFragment.TAG
            )
        }

        // 2401 - Based On Costing Type Redirection To Other Dialogs
        private fun costingType(position: ActionDetails, holder: ActionDetailsViewHolder) {
            if (position.costingType != "Bidding") {
                // Update Latest bidRequestId To Shared Preference
                updateBidRequestId(position.bidRequestId)
                // Create Common Info Dialog
                CommonInfoDialog.newInstance(position, "cost").show(
                    (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                    CommonInfoDialog.TAG
                )
            } else {
                holder.bidSubmitted(position)
            }
        }

        // 2354 - Method For Setting Quote Amount
        private fun setCost(actionDetails: ActionDetails, currencyType: String) {
            if (actionDetails.costingType == "Bidding") {
                if (actionDetails.latestBidValue.isNullOrEmpty()) {
                    amount.text = ""
                } else {
                    amount.text = currencyType + actionDetails.latestBidValue
                }
            } else {
                amount.text = "$currencyType${actionDetails.cost}"
            }
        }

        // 2354 - Method For Setting CurrencyType
        private fun setCurrencyType(actionDetails: ActionDetails): String {
            val currencyType = if (actionDetails.currencyType == null) {
                "$"
            } else {
                when (actionDetails.currencyType) {
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

        // Rejecting the Bids
        private fun bidRejection(position: ActionDetails) {
            BidRejectionDialogFragment.newInstance(
                position.bidRequestId,
                position.serviceName,
                position.eventServiceDescriptionId.toString(),
                // 2405 - Passing bidStatus to BidRejectionDialogFragment
                bidStatus
            ).show(
                (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                BidRejectionDialogFragment.TAG
            )
        }

        // Submitting Bids
        private fun bidSubmitted(position: ActionDetails) {
            val bidRequestId: Int = position.bidRequestId
            val costingType: String = position.costingType
            val bidStatus: String = position.bidStatus
            val cost: String? = position.cost
            val latestBidValue: String? = position.latestBidValue
            val branchName: String = position.branchName
            val serviceName: String = position.serviceName
            // Update Latest bidRequestId To Shared Preference
            updateBidRequestId(bidRequestId)

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
                ).show(
                    (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                    QuoteDetailsDialog.TAG
                )
            }
        }

        // 2401 - Method For Update Request Id
        private fun updateBidRequestId(bidRequestId: Int) {
            sharedPreference.putInt(SharedPreference.BID_REQUEST_ID, bidRequestId)
        }

        // Button visibility
        private fun buttonVisibility(holder: ActionDetailsViewHolder) {
            holder.likeButton.visibility = View.INVISIBLE
            holder.unlikeButton.visibility = View.INVISIBLE
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

        fun showDialog(status: ActionDetails)
    }
}