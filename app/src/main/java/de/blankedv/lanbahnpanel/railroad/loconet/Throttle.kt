package de.blankedv.lanbahnpanel.railroad.loconet

/**
 *
 * @author mblank
 *
 * adapted from arduino lib "loconet"
 */
object Throttle {

    const val STAT1_SL_SPURGE = 0x80
    /* internal use only, not seen on net */
    const val STAT1_SL_CONUP = 0x40
    /* consist status                     */
    const val STAT1_SL_BUSY = 0x20
    /* used with STAT1_SL_ACTIVE,         */
    const val STAT1_SL_ACTIVE = 0x10
    /*                                    */
    const val STAT1_SL_CONDN = 0x08
    /*                                    */
    const val STAT1_SL_SPDEX = 0x04
    /*                                    */
    const val STAT1_SL_SPD14 = 0x02
    /*                                    */
    const val STAT1_SL_SPD28 = 0x01
    /*                                    */
    const val STAT2_SL_SUPPRESS = 0x01
    /* 1 = Adv. Consisting supressed      */
    const val STAT2_SL_NOT_ID = 0x04
    /* 1 = ID1/ID2 is not ID usage        */
    const val STAT2_SL_NOTENCOD = 0x08
    /* 1 = ID1/ID2 is not encoded alias   */
    const val STAT2_ALIAS_MASK = STAT2_SL_NOTENCOD or STAT2_SL_NOT_ID
    const val STAT2_ID_IS_ALIAS = STAT2_SL_NOT_ID

    const val LOCOSTAT_MASK = STAT1_SL_BUSY or STAT1_SL_ACTIVE
    const val LOCO_IN_USE = STAT1_SL_BUSY or STAT1_SL_ACTIVE
    const val LOCO_IDLE = STAT1_SL_BUSY
    const val LOCO_COMMON = STAT1_SL_ACTIVE
    const val LOCO_FREE = 0


    fun statusToString(stat: Int): String {
        when (stat and LOCOSTAT_MASK) {
            LOCO_IN_USE -> return "In-Use"
            LOCO_IDLE -> return "Idle"
            LOCO_COMMON -> return "Common"
            else -> return "Free"
        }
    }

}
