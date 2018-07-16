package de.blankedv.lanbahnpanel.railroad.loconet

import de.blankedv.lanbahnpanel.model.INVALID_INT
import java.util.ArrayList

class Lissy(
        var id: Int,
        var speed: Int = INVALID_INT,
        var locoAddr: Int = INVALID_INT,
        var cat: Int = INVALID_INT,
        var dir: Int = INVALID_INT,
        var timeStamp: Long = 0L) {

    override fun toString(): String {
        var s = ""
        if (id != INVALID_INT) {
            if (locoAddr != INVALID_INT) {
                s += "Lissy#$id loco=$locoAddr"
                if (speed != INVALID_INT) s += " speed=$speed"
                if (dir != INVALID_INT) s += " dir=$dir"
                if (cat != INVALID_INT) s += " cat=$cat"

                return s
            }
        }
        return "-?"
    }

    fun toShortString(): String {
        var s = ""
        if (id != INVALID_INT) {
            if (locoAddr != INVALID_INT) {
                s += "L$id loco=$locoAddr"
                if (speed != INVALID_INT) s += " sp=$speed"
                if (cat != INVALID_INT) s += " k=$cat"
                return s
            }
        }
        return "-?"
    }

    fun isTooOld() : Boolean {
        return ((System.currentTimeMillis() - timeStamp) > LISSY_MAX_TIME)
    }
    companion object {

        const val LISSY_MAX_TIME = 20000  // in milliseconds

        val lissyData = ArrayList<Lissy>()

        /** there are two different lissy update messages - only together they provide complete
         * lissy data, therefor the check for INVALID_INT here and the more complicated update
         * process of the data */
        fun update(lissyID: Int, sp: Int, adr: Int, c: Int, d: Int): Lissy {
            for (lis in lissyData) {
                if (lis.id == lissyID) {
                    if (sp != INVALID_INT) lis.speed = sp
                    if (adr != INVALID_INT) lis.locoAddr = adr
                    if (c != INVALID_INT) lis.cat = c
                    if (d != INVALID_INT) lis.dir = d
                    lis.timeStamp = System.currentTimeMillis()
                    //lissyViewAdapter.notifyDataSetChanged()  // NOT called automatically in this case
                    return lis // there is only one lissy per id
                }
            }
            // lissy with this ID does no yet exist, lets create one
            var newLissy = Lissy(lissyID) // new Lissy
            if (sp != INVALID_INT) newLissy.speed = sp
            if (adr != INVALID_INT) newLissy.locoAddr = adr
            if (c != INVALID_INT) newLissy.cat = c
            if (d != INVALID_INT) newLissy.dir = d
            newLissy.timeStamp = System.currentTimeMillis()

            lissyData.add(newLissy) // and add to the Lissy list
            lissyData.sortBy({ it.id })
            return newLissy
        }


    }
}