package com.smf.events.ui.businessregistration.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.R
import com.smf.events.ui.addservicedialog.AddServiceDialog
import com.smf.events.ui.addservicedialog.model.Services
import com.smf.events.ui.businessregistration.model.SelectedServices

class AddServiceAdapter(val context: Context) :
    RecyclerView.Adapter<AddServiceAdapter.AddServiceViewHolder>() {

    private lateinit var recyclerViewAdapter: SelectedServicesRecyclerViewAdapter
    private var serviceList = ArrayList<SelectedServices>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): AddServiceAdapter.AddServiceViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.add_services_card_view, parent, false)
        // User Selected Service List
        serviceList = selectedDataList()
        return AddServiceViewHolder(itemView)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: AddServiceAdapter.AddServiceViewHolder, position: Int) {

        with(holder) {
            val isExpandable: Boolean = expand
            expandablelayout.visibility = if (isExpandable) View.VISIBLE else View.GONE

            addServiceLayout.setOnClickListener {
                expand = !expand
                notifyDataSetChanged()
            }

            addService.setOnClickListener {
                //Calling Add Services Dialog Frgment
                AddServiceDialog.newInstance(getDataModelList()).show(
                    (context as FragmentActivity).supportFragmentManager,
                    AddServiceDialog.TAG
                )
            }

            selectedServiceRecyclerViewSetUp(this)
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    inner class AddServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var addServiceBtn: ImageView = view.findViewById(R.id.add_btn)
        var addService: Button = view.findViewById(R.id.add_services_btn)
        var expand: Boolean = false
        var addServiceLayout: ConstraintLayout = view.findViewById(R.id.add_services_layout)
        var expandablelayout: ConstraintLayout = view.findViewById(R.id.add_service_btn_layout)
        var selectedServiceRecyclerView: RecyclerView =
            view.findViewById(R.id.selected_service_recycler_view)

    }

    // Method for selected Services RecyclerView Initialization
    private fun selectedServiceRecyclerViewSetUp(addServiceViewHolder: AddServiceViewHolder) {
        addServiceViewHolder.selectedServiceRecyclerView.layoutManager =
            GridLayoutManager(context, 3)
        recyclerViewAdapter = SelectedServicesRecyclerViewAdapter(context, serviceList)
        addServiceViewHolder.selectedServiceRecyclerView.adapter = recyclerViewAdapter
    }

    // Method for ListView Data
    private fun getDataModelList(): ArrayList<Services> {

        var dataModel = ArrayList<Services>()

        dataModel.add(Services("Venue", "gvhjsa", false))
        dataModel.add(Services("Beauty", "gvhjsa", false))
        dataModel.add(Services("Catering", "gvhjsa", false))
        dataModel.add(Services("Balloons", "gvhjsa", false))
        dataModel.add(Services("Cakes", "gvhjsa", false))
        dataModel.add(Services("Snakes", "gvhjsa", false))

        return dataModel
    }

    // Method for User Selected List
    private fun selectedDataList(): ArrayList<SelectedServices> {
        var list = ArrayList<SelectedServices>()

        list.add(SelectedServices("Venue"))
        list.add(SelectedServices("Beauty"))
        list.add(SelectedServices("Catering"))
        list.add(SelectedServices("Balloons"))
        list.add(SelectedServices("Cakes"))
        list.add(SelectedServices("Snakes"))

        return list
    }
}