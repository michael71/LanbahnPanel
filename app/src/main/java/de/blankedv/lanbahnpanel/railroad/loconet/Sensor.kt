package de.blankedv.lanbahnpanel.railroad.loconet


class Sensor (address : Int, data : Int ) : DCC_Data ( address, data ) {
    override fun toString(): String {
        var s = "Sensor addr=$address"
        if (data == 0) {
            s +=  " free"
        } else {
            s += " occupied"
        }
        return s
    }


companion object {

    val sensorData = ArrayList<Sensor>()
    //lateinit var sensorViewAdapter: RecyclerView.Adapter<*>   // must be manually "notified" when data change

    private fun selectorSensor(p: Sensor): Int = p.address

    fun addOrReplaceSensorData(sen: Sensor) {

        sensorData.forEachIndexed { index, e ->
            if (e.address == sen.address) {
                sensorData.set(index, sen)
                //sensorViewAdapter.notifyDataSetChanged() // NOT called automatically in this case
                return
            }
        }

        sensorData.add(sen)
        sensorData.sortBy({ it.address })
    }
}

}