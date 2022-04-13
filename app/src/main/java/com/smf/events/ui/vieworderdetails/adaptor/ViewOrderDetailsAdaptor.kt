package com.smf.events.ui.vieworderdetails.adaptor

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.smf.events.R

//2402 View order details  Adaptor
class ViewOrderDetailsAdaptor(
    private val context: Activity,
    private val title: ArrayList<String>,
    private val answers: ArrayList<String>,
) : ArrayAdapter<String>(context, R.layout.questions_custom_listview, title) {
    var ques = 1

    //2402 GetView for customList
    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.questions_custom_listview, null, true)
        val titleText = rowView.findViewById(R.id.questions) as TextView
        val answer = rowView.findViewById(R.id.answer) as TextView
        if (position == 0) {
            ques = 1
        } else {
            ques++
        }
        titleText.text = "Q$ques . ${title[position]}"
        answer.text = "Answer. ${answers[position]}"
        return rowView
    }
}