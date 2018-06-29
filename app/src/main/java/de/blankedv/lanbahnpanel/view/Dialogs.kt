package de.blankedv.lanbahnpanel.view

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.NumberPicker
import de.blankedv.lanbahnpanel.R
import de.blankedv.lanbahnpanel.elements.ActivePanelElement
import de.blankedv.lanbahnpanel.elements.PanelElement
import de.blankedv.lanbahnpanel.model.appContext
import de.blankedv.lanbahnpanel.model.configHasChanged

/**
 * predefined dialogs
 *
 * TODO review kotlin code
 */
object Dialogs {

    private val MAX_ADDR = 999
    private val MIN_ADDR = 1

    internal fun selectAddressDialog(el: PanelElement) {

        val factory = LayoutInflater.from(appContext)
        val selAddressView = factory.inflate(
                R.layout.alert_dialog_sel_address, null)
        val address = selAddressView
                .findViewById<View>(R.id.picker1) as NumberPicker

        address.minValue = MIN_ADDR
        address.maxValue = MAX_ADDR
        // TODO: replace by list of active lanbahn messages

        address.setOnLongPressUpdateInterval(100) // faster change for long press
        val e = el as ActivePanelElement
        val msg: String
        address.value = e.adr
        msg = "Adresse?"
        val addrDialog = AlertDialog.Builder(appContext)
                .setMessage(msg)
                .setCancelable(false)
                .setView(selAddressView)
                .setPositiveButton("Speichern"
                ) { dialog, id ->
                    // Toast.makeText(appContext,"Adresse "+sxAddress.getCurrent()
                    // +"/"+sxBit.getCurrent()+" wurde selektiert",
                    // Toast.LENGTH_SHORT)
                    // .show();
                    e.adr = address.value
                    configHasChanged = true // flag for saving the
                    // configuration
                    // later when
                    // pausing the
                    // activity
                    dialog.dismiss()
                }
                .setNegativeButton("Zurück"
                ) { dialog, id ->
                    // dialog.cancel();
                    dialog.dismiss()
                }.create()
        addrDialog.show()
        addrDialog.window!!.setLayout(350, 400)
    }
}
