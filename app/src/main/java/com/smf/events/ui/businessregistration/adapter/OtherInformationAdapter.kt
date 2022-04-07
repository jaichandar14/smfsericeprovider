package com.smf.events.ui.businessregistration.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.R


class OtherInformationAdapter :
    RecyclerView.Adapter<OtherInformationAdapter.OtherInfoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtherInfoViewHolder {

        var itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.other_info_cardview, parent, false)
        return OtherInfoViewHolder(itemView)

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: OtherInfoViewHolder, position: Int) {
        with(holder) {

            val isExpandable: Boolean = expand
            expandableLayout.visibility = if (isExpandable) View.VISIBLE else View.GONE

            otherInfoLayout.setOnClickListener {
                expand = !expand
                notifyDataSetChanged()

                addMore.setOnClickListener {
                    addMorelayout3.visibility = View.VISIBLE
                    addMore.visibility = View.GONE
                    notifyDataSetChanged()

                }

                addMore4.setOnClickListener {
                    addMorelayout4.visibility = View.VISIBLE
                    addMore4.visibility = View.GONE
                    notifyDataSetChanged()

                }
                addMore5.setOnClickListener {
                    addMorelayout5.visibility = View.VISIBLE
                    addMore5.visibility = View.GONE
                    notifyDataSetChanged()

                }
                addMore6.setOnClickListener {
                    addMorelayout6.visibility = View.VISIBLE
                    addMore6.visibility = View.GONE
                    notifyDataSetChanged()

                }
                addMore7.setOnClickListener {
                    addMorelayout7.visibility = View.VISIBLE
                    addMore7.visibility = View.GONE
                    notifyDataSetChanged()

                }
                addMoreFinal.setOnClickListener {
                    addMoreFinal.visibility = View.GONE
//                  callBackInterface!!.callBack("7 rows only  be added ")

                }


            }
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    inner class OtherInfoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var expand: Boolean = false
        var expandableLayout: LinearLayout = view.findViewById(R.id.expandable_layout_other_info)
        var otherInfoLayout: ConstraintLayout = view.findViewById(R.id.other_info_layout)
        var addMore = view.findViewById<LinearLayout>(R.id.addmore_linerlayout)
        var addMore4: LinearLayout = view.findViewById(R.id.addmore_linerlayout3)
        var addMore5: LinearLayout = view.findViewById(R.id.addmore_linerlayout4)
        var addMore6: LinearLayout = view.findViewById(R.id.addmore_linerlayout5)
        var addMore7: LinearLayout = view.findViewById(R.id.addmore_linerlayout6)
        var addMoreFinal: LinearLayout = view.findViewById(R.id.addmore_linerlayout7)
        var addMorelayout3 = view.findViewById<ConstraintLayout>(R.id.add_more_layout3)
        var addMorelayout4 = view.findViewById<ConstraintLayout>(R.id.add_more_layout4)
        var addMorelayout5 = view.findViewById<ConstraintLayout>(R.id.add_more_layout5)
        var addMorelayout6 = view.findViewById<ConstraintLayout>(R.id.add_more_layout6)
        var addMorelayout7 = view.findViewById<ConstraintLayout>(R.id.add_more_layout7)
    }


    private var callBackInterface: CallBackInterface? = null

    // Initializing CallBack Interface Method
    fun setCallBackInterface(callback: CallBackInterface) {
        callBackInterface = callback
    }

    // CallBackInterface
    interface CallBackInterface {
        fun callBack(status: String)

    }


}