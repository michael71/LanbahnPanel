package de.blankedv.lanbahnpanel.railroad.loconet

import de.blankedv.lanbahnpanel.model.INVALID_INT

// loco or lissy directions
const val DIRECTION_FORWARD = 0
const val DIRECTION_BACKWARD = 1
const val DIRECTION_UNKNOWN = INVALID_INT

// accessory states
const val ACC_CLOSED = 0
const val ACC_THROWN = 1
const val ACC_UNKNOWN = INVALID_INT
const val ACC_RED = ACC_THROWN
const val ACC_GREEN = ACC_CLOSED