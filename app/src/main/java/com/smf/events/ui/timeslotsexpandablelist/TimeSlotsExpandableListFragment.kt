package com.smf.events.ui.timeslotsexpandablelist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.FragmentTimeSlotsExpandableListBinding
import com.smf.events.ui.timeslotsexpandablelist.adapter.CustomExpandableListAdapter
import com.smf.events.ui.timeslotsexpandablelist.model.ExpandableListData.data
import com.smf.events.ui.timeslotsexpandablelist.model.TitleData
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class TimeSlotsExpandableListFragment :
    BaseFragment<FragmentTimeSlotsExpandableListBinding, TimeSlotsExpandableListViewModel>(),
    CustomExpandableListAdapter.TimeSlotIconClickListener {

    private var expandableListView: ExpandableListView? = null
    private var adapter: CustomExpandableListAdapter? = null
    private var titleList: List<TitleData>? = null

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModel(): TimeSlotsExpandableListViewModel =
        ViewModelProvider(this, factory).get(TimeSlotsExpandableListViewModel::class.java)

    override fun getBindingVariable(): Int = BR.timeSlotsExpandableListViewModel

    override fun getContentView(): Int = R.layout.fragment_time_slots_expandable_list

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("TAG", "onViewCreated: TimeSlotsExpandableListFragment called")
        // 2527 - Initialize expendableList
        expandableListView = mDataBinding?.expendableList
        initializeExpandableListSetUp()

    }

    private fun initializeExpandableListSetUp() {
        if (expandableListView != null) {
            val listData = data
            titleList = ArrayList(listData.keys)
            adapter = CustomExpandableListAdapter(
                requireContext(),
                titleList as ArrayList<TitleData>,
                listData
            )
            expandableListView!!.setAdapter(adapter)
            adapter?.setOnClickListener(this)

            expandableListView!!.setOnGroupClickListener { parent, v, groupPosition, id ->

                return@setOnGroupClickListener false
            }

            expandableListView!!.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->

                return@setOnChildClickListener false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("TAG", "onViewCreated: TimeSlotsExpandableListFragment onResume called")
    }

    override fun onClick(listPosition: Int) {
        Log.d("TAG", "actionCardClick: clicked")
    }

}