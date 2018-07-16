package de.blankedv.lanbahnpanel.railroad.loconet

/**
 * predefined throttle messages
 *
 * @author mblank
 */
object ThrottleMessages {

    fun forward(slotNumber: Int): String {
        val s = "A1 0$slotNumber 30"

        return "SEND " + addChecksumToLNString(s)
    }

    fun backward(slotNumber: Int): String {
        val s = "A1 0$slotNumber 10"
        return "SEND " + addChecksumToLNString(s)
    }

    /* convert integer (0...255) to two char hexstring
        10 => "0A" etc.

     */
    fun twoCharFromInt(i: Int): String {
        if (i > 255) {
            return "00"
        }
        var s = Integer.toHexString(i and 0xFF)
        if (s.length == 1) {
            s = "0$s"
        }
        return s.toUpperCase()
    }

    fun aquire(locoAddr: Int): String {
        val s: String
        if (locoAddr < 0) {
            return ""
        }

        val hAddr = locoAddr and 0xFF80 shr 7
        val lAddr = locoAddr and 0x007F
        s = "BF " + twoCharFromInt(hAddr) + " " + twoCharFromInt(lAddr)

        println(s)
        return "SEND " + addChecksumToLNString(s)

    }

    fun nullMove(slot: Int): String {

        val s = "BA " + twoCharFromInt(slot) + " " + twoCharFromInt(slot)
        return "SEND " + addChecksumToLNString(s)

    }

    /**
     * add a checksum to a loconet message in hex form for example "A1 00 20"
     *
     * @param s
     * @return
     */
    fun addChecksumToLNString(s: String): String {
        val hexStrings = s.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val len = hexStrings.size // total length
        val msgBytes = ByteArray(len)
        var checksum = 0
        for (i in 0 until len) {
            val d = Integer.parseInt(hexStrings[i], 16)
            //System.out.print(hexStrings[i]+" ");
            msgBytes[i] = (d and 0xff).toByte()
        }
        checksum = msgBytes[0].toInt()
        //System.out.print("cksum calc: " + Integer.toHexString(checksum & 0xff).toUpperCase()+" ");
        //System.out.println();
        for (i in 1 until len) {
            checksum = checksum xor msgBytes[i].toInt()
            //System.out.print(Integer.toHexString(checksum & 0xff).toUpperCase()+" ");
        }
        checksum = checksum xor 0xff
        return s + " " + twoCharFromInt(checksum and 0xff)
    }


}
