package com.smf.events.ui.timeslotsexpandablelist.model

internal object ExpandableListData {
    val data: HashMap<TitleData, List<List<ListData>>>
        get() {
            val expandableListDetail =
                HashMap<TitleData, List<List<ListData>>>()

            val one: MutableList<List<ListData>> = ArrayList()
            val myFavCricketPlayers: MutableList<ListData> =
                ArrayList()
            myFavCricketPlayers.add(ListData("12am-3am","Available"))
            myFavCricketPlayers.add(ListData("3am-6am","Available"))
            myFavCricketPlayers.add(ListData("6am-9am","Available"))
            myFavCricketPlayers.add(ListData("9am-12pm","Available"))
            myFavCricketPlayers.add(ListData("12pm-3pm","Available"))
            one.add(myFavCricketPlayers)

            val two: MutableList<List<ListData>> = ArrayList()
            val myFavFootballPlayers: MutableList<ListData> = ArrayList()
            myFavFootballPlayers.add(ListData("12am-3am","NotAvailable"))
            myFavFootballPlayers.add(ListData("3am-6am","NotAvailable"))
            myFavFootballPlayers.add(ListData("6am-9am","NotAvailable"))
            myFavFootballPlayers.add(ListData("9am-12pm","NotAvailable"))
            myFavFootballPlayers.add(ListData("12pm-3pm","NotAvailable"))
            myFavFootballPlayers.add(ListData("3pm-6pm","NotAvailable"))
            myFavFootballPlayers.add(ListData("6pm-9pm","NotAvailable"))
            myFavFootballPlayers.add(ListData("9pm-12am","NotAvailable"))
            two.add(myFavFootballPlayers)

            val three: MutableList<List<ListData>> = ArrayList()
            val myFavTennisPlayers: MutableList<ListData> = ArrayList()
            myFavTennisPlayers.add(ListData("12am-3am","NotAvailable"))
            myFavTennisPlayers.add(ListData("3am-6am","NotAvailable"))
            myFavTennisPlayers.add(ListData("6am-9am","NotAvailable"))
            myFavTennisPlayers.add(ListData("9am-12pm","NotAvailable"))
            myFavTennisPlayers.add(ListData("12pm-3pm","NotAvailable"))
            three.add(myFavTennisPlayers)

            expandableListDetail[TitleData("Aug 12", "Wednesday")] = one
            expandableListDetail[TitleData("Aug 30", "Monday")] = two
            expandableListDetail[TitleData("Apr 25", "Sunday")] = three

            return expandableListDetail
        }
}