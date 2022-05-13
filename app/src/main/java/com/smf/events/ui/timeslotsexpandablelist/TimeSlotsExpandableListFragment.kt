package com.smf.events.ui.timeslotsexpandablelist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.smf.events.R
import com.smf.events.ui.schedulemanagement.ScheduleManagementViewModel
import com.smf.events.ui.timeslotsexpandablelist.adapter.CustomExpandableListAdapter
import com.smf.events.ui.timeslotsexpandablelist.model.ListData
import com.smf.events.ui.timeslotsexpandablelist.model.Status

class TimeSlotsExpandableListFragment : Fragment(),
    CustomExpandableListAdapter.TimeSlotIconClickListener {

    private var expandableListView: ExpandableListView? = null
    private var adapter: CustomExpandableListAdapter? = null
    private var topics = HashMap<String, List<ListData>>()
    private var langs = ArrayList<String>()

    // SharedViewModel Initialization
    private val sharedViewModel: ScheduleManagementViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_time_slots_expandable_list, container, false)
        expandableListView = root.findViewById(R.id.expendableList)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 2558 - Get ExpandableList Data
        fillData()
        // 2558 - ExpandableList Initialization
        initializeExpandableListSetUp()
        // 2558 - getDate ScheduleManagementViewModel Observer
        sharedViewModel.getDate.observe(requireActivity(), {
            Log.d("TAG", "onCreateView viewModel called TimeSlotsExpandableListFragment: $it")
        })
    }

    // 2558 - Method for Get ExpandableList Data
    private fun fillData() {
        langs.add("05/09/2022")
        langs.add("05/19/2022")
        val java = ArrayList<ListData>()

        val one = ArrayList<Status>()
        one.add(Status("Catering", "Food Capital", "Vi Vi Bday", "10/10/2022"))
        one.add(Status("Salon", "Salon Shop", "Vi Vi Bday", "10/10/2022"))
        one.add(Status("Wedding", "Wedding Shop", "Vi Vi Bday", "10/10/2022"))
        one.add(Status("Party", "Party Shop", "Vi Vi Bday", "10/10/2022"))

        java.add(ListData("12am - 3am", one))
        java.add(ListData("3am - 6am", one))
        topics.put(langs[0], java)
        topics.put(langs[1], java)
    }

    // 2558 - Method for ExpandableList Initialization
    private fun initializeExpandableListSetUp() {
        if (expandableListView != null) {
            adapter = CustomExpandableListAdapter(
                requireContext(),
                langs,
                topics
            )
            expandableListView!!.setAdapter(adapter)
            adapter?.setOnClickListener(this)

            expandableListView!!.setOnGroupClickListener { parent, v, groupPosition, id ->

                return@setOnGroupClickListener false
            }

            expandableListView!!.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->

                return@setOnChildClickListener false
            }

            expandableListView!!.setOnGroupCollapseListener { groupPosition ->
                Log.d(
                    "TAG",
                    "initializeExpandableListSetUp: clope $groupPosition"
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("TAG", "onViewCreated: TimeSlotsExpandableListFragment onResume called")
    }

    override fun onClick(expandedListPosition: Int) {
        Log.d("TAG", "onCreateView viewModel called $expandedListPosition")
        sharedViewModel.setDate("05/20/2022")
    }

}