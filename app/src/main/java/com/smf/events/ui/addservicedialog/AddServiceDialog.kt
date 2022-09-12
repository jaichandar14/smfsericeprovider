package com.smf.events.ui.addservicedialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.smf.events.R
import com.smf.events.ui.addservicedialog.adapter.AddServiceCustomListAdapter
import com.smf.events.ui.addservicedialog.model.Services


class AddServiceDialog(private var dataModel: ArrayList<Services>) : DialogFragment() {

    private lateinit var closeBtn: Button
    private lateinit var saveBtn: Button
    private lateinit var viewMoreText: TextView
    private lateinit var listView: ListView
    private lateinit var adapter: AddServiceCustomListAdapter
    private var finalSelectedList: ArrayList<String>? = ArrayList()

    companion object {
        const val TAG = "SimpleDialog"
        fun newInstance(dataList: ArrayList<Services>): AddServiceDialog {
            return AddServiceDialog(dataList)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_services_dialog, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById(R.id.list_view)

        adapter = AddServiceCustomListAdapter(dataModel, requireContext())
        listView.adapter = adapter

        listViewClickListener()

        setupClickListeners(view)

    }

    override fun onResume() {
        super.onResume()
        dialog!!.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // To dismiss the fragment when the back-button is pressed.
                dismiss()
                true
            } else false
        }
    }

    // Method for Dialog Fragment Listeners
    private fun setupClickListeners(view: View) {

        closeBtn = view.findViewById(R.id.close_btn)
        saveBtn = view.findViewById(R.id.save_btn)
        viewMoreText = view.findViewById(R.id.text_view_more)

        saveBtn.setOnClickListener {
            for (i in dataModel) {
                if (i.checked) {
                    finalSelectedList?.add(i.serviceName)
                }
            }

            Log.d("TAG", "setupClickListeners: $finalSelectedList")
            dismiss()
        }

        closeBtn.setOnClickListener {
            dismiss()
        }

        viewMoreText.setOnClickListener {
            viewMoreText.visibility = View.INVISIBLE
        }


    }

    // Method for ListView Listeners
    private fun listViewClickListener() {
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            val checkBox = view.findViewById<CheckBox>(R.id.check_box)
            var status = checkBox.isChecked
            status = !status
            checkBox.isChecked = status

            dataModel[i].checked = checkBox.isChecked
        }
    }

}