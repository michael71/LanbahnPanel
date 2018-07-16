package de.blankedv.lanbahnpanel.railroad.loconet

import java.util.ArrayList

class Accessory (address : Int, data : Int, val state : Int): DCC_Data ( address, data ) {

    override fun toString(): String {
        var s = "switch addr=$address"
        // from loconet manual DIR=1 for Closed,/GREEN, =0 for Thrown/RED
        if (data == 1) {
            s += " 1/closed/green"
        } else {
            s += " 0/thrown/red"
        }
        if (state == 0) {
            s += " OFF"
        } else {
            s += " ON"
        }
        return s
    }


    companion object {
        /** ArrayLists to hold the DCC data, also during MainActivity restart */
        /** accessories and sensor do not share the address space in DCC */
        var accessoryData = ArrayList<Accessory>()
        //lateinit var accViewAdapter: RecyclerView.Adapter<*>   // must be manually "notified" when data change

        fun addOrReplaceAccessoryData(acc: Accessory ) {

            accessoryData.forEachIndexed { index, e ->
                if (e.address == acc.address) {
                    accessoryData.set(index, acc) // replace
                    //accViewAdapter.notifyDataSetChanged() // NOT called automatically in this case
                    return
                }
            }

            accessoryData.add(acc)
            accessoryData.sortBy({ it.address })


        }
    }

}