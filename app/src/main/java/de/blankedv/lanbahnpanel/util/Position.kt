package de.blankedv.lanbahnpanel.util

import de.blankedv.lanbahnpanel.model.INVALID_INT

class Position (
        var x: Int = 0, // starting point
        var y: Int = 0,
        var x2:Int = INVALID_INT, // endpoint - x2 always >x
        var y2 :Int= INVALID_INT,
        var xt : Int= INVALID_INT, // "thrown" position for turnout
        var yt : Int = INVALID_INT
)