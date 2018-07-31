package de.blankedv.lanbahnpanel.settings

import de.blankedv.lanbahnpanel.model.*

/**
 * the 4 quadrants can have different scaling, a scale consists of
 *
 * @param scale = scaling of panelElements when displayed (both for X and Y)
 * @param xoff = offset on x axis
 * @param yoff = offset on y axis
 */
data class Scaling (var scale : Float, var xoff :  Float, var yoff:  Float)


data class PanelSettings (var selScale : String, var selStyle : String,
                          var fiveViews: Boolean = false, var selQua : Int = 0,
                          var qClip : Array<Scaling> = arrayOf(
                Scaling(1.0f, (10f * prescale), (10f * prescale)),  // selQuadrant 0 = all
                Scaling(1.0f, (10f * prescale), (10f * prescale)),  // selQuadrant  1
                Scaling(1.0f, (10f * prescale), (10f * prescale)),  // ... 2
                Scaling(1.0f, (10f * prescale), (10f * prescale)),
                Scaling(1.0f, (10f * prescale), (10f * prescale)))
)

