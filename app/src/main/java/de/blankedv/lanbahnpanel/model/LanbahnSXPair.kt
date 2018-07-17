package de.blankedv.lanbahnpanel.model

/**
 * is used for mapping of lanbahn addresses to SX addresses
 *
 * NEEDED for "SX Style" panels with selectrix addresses in xml-config
 *
 * @author mblank
 */
class LanbahnSXPair {

    var lbAddr: Int = 0   // lanbahn address
    var sxAddr: Int = 0   // sx Address
    var sxBit: Int = 0    // (first) sx bit (1..8) (== lowest bit value
    // example sxBit = 5, nBit=2
    // bit5=1 ==> value = 1
    // bit6=1 ==> value = 2
    // bit5 and bit6 =set => Value = 3

    var nBit: Int = 0    // number of bits used 1 ...4

    val isValid: Boolean
        get() = if (lbAddr != INVALID_INT &&
                sxAddr != INVALID_INT &&
                sxBit >= 1 &&
                sxBit <= 8) {
            true
        } else {
            false
        }

    internal constructor() {
        lbAddr = INVALID_INT
        sxAddr = INVALID_INT
        sxBit = 1
        nBit = 1
    }

    internal constructor(l: Int, s: Int, b: Int, n: Int) {
        lbAddr = l
        sxAddr = s
        sxBit = b
        nBit = n
    }

    internal constructor(l: Int, s: Int, b: Int) {
        lbAddr = l
        if ((l == INVALID_INT) and (s != INVALID_INT) and (b != INVALID_INT)) {
            // only sx address given, but lanbahn can be calculated out of sxadr and sxbit
            lbAddr = s * 10 + b  // s=98, b=7  ==> l=987
        }
        sxAddr = s
        sxBit = b
        nBit = 1
    }

    /**
     * calculate lanbahn value from the SX data byte
     * use only relevant bits sxBit ... sxBit+(nBit-1)
     * @param d
     * @return
     */
    /*   fun getLBValueFromSXByte(d: Int): Int {
           var v = 0
           for (i in sxBit until sxBit + nBit) {
               if (SXUtils.isSet(d, i) !== 0) {
                   v = v + (1 shl i - sxBit)
               }
           }
           //if (sxAddr == 70) {
           //System.out.println("lbaddr="+lbAddr+ " sxaddr="+sxAddr+ " sxBit="+sxBit+" nBit="+nBit+" v="+v);
           //}
           return v
       } */

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("lbAddr=")
        sb.append(lbAddr)
        sb.append(" sxAddr=")
        sb.append(sxAddr)
        for (i in nBit downTo 1) {
            sb.append(" bit=")
            sb.append(sxBit + (i - 1))
        }
        return sb.toString()
    }

}