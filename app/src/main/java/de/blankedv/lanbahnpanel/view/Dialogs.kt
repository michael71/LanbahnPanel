package de.blankedv.lanbahnpanel.view

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import de.blankedv.lanbahnpanel.R
import de.blankedv.lanbahnpanel.elements.ActivePanelElement
import android.content.DialogInterface
import de.blankedv.lanbahnpanel.elements.PanelElement
import de.blankedv.lanbahnpanel.loco.Loco

import android.content.ContentValues.TAG
import android.content.res.Resources
import android.opengl.Visibility
import android.util.Log
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import de.blankedv.lanbahnpanel.elements.SignalElement
import de.blankedv.lanbahnpanel.model.*


/**
 * predefined dialogs
 *
 * TODO review kotlin code
 */
object Dialogs {

    private val MAX_ADDR = 9999
    private val MIN_ADDR = 1

    private var selLocoIndex: Int = 0
    private val NOTHING = 99999
    private val NEW_LOCO_NAME = "+ NEUE LOK (3)"

    internal fun selectAddressDialog(el: PanelElement) {

        val factory = LayoutInflater.from(appContext)
        val selAddressView = factory.inflate(
                R.layout.alert_dialog_sel_address, null)

        val tvAdr2 = selAddressView
                .findViewById<View>(R.id.tvAddress2) as TextView
        val tvInv2 = selAddressView
                .findViewById<View>(R.id.tvInverted2) as TextView
        val tvInv = selAddressView
                .findViewById<View>(R.id.tvInverted) as TextView
        val address = selAddressView
                .findViewById<View>(R.id.picker1) as NumberPicker
        val inverted = selAddressView.findViewById<View>(R.id.cbInverted) as CheckBox

        val address2 = selAddressView
                .findViewById<View>(R.id.picker2) as NumberPicker
        val inverted2 = selAddressView.findViewById<View>(R.id.cbInverted2) as CheckBox

        address.minValue = MIN_ADDR
        address.maxValue = MAX_ADDR

        address2.minValue = MIN_ADDR
        address2.maxValue = MAX_ADDR

        address.setOnLongPressUpdateInterval(100) // faster change for long press
        val e = el as ActivePanelElement
        if (e.adr2 != INVALID_INT) {
            address2.visibility = View.VISIBLE
            inverted2.visibility = View.VISIBLE
            tvAdr2.visibility = View.VISIBLE
            tvInv2.visibility = View.VISIBLE

        } else {   // hide second address selection if there is not second address for this PanelElement
            address2.visibility = View.GONE
            inverted2.visibility = View.GONE
            tvAdr2.visibility = View.GONE
            tvInv2.visibility = View.GONE
        }
        if (e is SignalElement) {
            tvInv.visibility = View.GONE
            inverted.visibility = View.GONE
        } else {
            tvInv.visibility = View.VISIBLE
            inverted.visibility = View.VISIBLE
        }
        val msg: String
        address.value = e.adr
        address2.value = e.adr2
        inverted.isChecked = (e.invert == DISP_INVERTED)
        inverted2.isChecked = (e.invert2 == DISP_INVERTED)
        val res = appContext?.resources
        msg = res!!.getString(R.string.address) + "?"
        val addrDialog = AlertDialog.Builder(appContext)
                .setMessage(msg)
                .setCancelable(false)
                .setView(selAddressView)
                .setPositiveButton(res.getString(R.string.save)
                ) { dialog, id ->
                    // Toast.makeText(appContext,"Adresse "+sxAddress.getCurrent()
                    // +"/"+sxBit.getCurrent()+" wurde selektiert",
                    // Toast.LENGTH_SHORT)
                    // .show();
                    // TODO check address for validity
                    e.adr = address.value
                    if (inverted.isChecked) {
                        e.invert = DISP_INVERTED
                    } else {
                        e.invert = DISP_STANDARD
                    }
                    // TODO check address2 for validity
                    if (e.adr2 != INVALID_INT) {
                        e.adr2 = address2.value
                        if (inverted2.isChecked) {
                            e.invert2 = DISP_INVERTED
                        } else {
                            e.invert2 = DISP_STANDARD
                        }
                    }
                    configHasChanged = true // flag for saving the
                    // configuration
                    // later when
                    // pausing the
                    // activity
                    dialog.dismiss()
                }
                .setNegativeButton(res.getString(R.string.back)
                ) { dialog, id ->
                    // dialog.cancel();
                    dialog.dismiss()
                }.create()
        addrDialog.show()
        if (e.adr2 != INVALID_INT) {
            addrDialog.window!!.setLayout(700, 400)
        } else {
            addrDialog.window!!.setLayout(350, 400)
        }
    }

    fun selectLocoDialog() {

        val factory = LayoutInflater.from(appContext)
        val selSxAddressView = factory.inflate(R.layout.alert_dialog_sel_loco_from_list, null)
        val selLoco = selSxAddressView.findViewById(R.id.spinner) as Spinner

        val locosToSelect = arrayOfNulls<String>(locolist?.size + 1)

        var index = 0
        var selection = 0
        for (l in locolist) {
            locosToSelect[index] = l.name + " (" + l.adr + ")"
            if (l == selectedLoco) {
                selection = index
            }
            index++
        }
        locosToSelect[index] = NEW_LOCO_NAME
        val NEW_LOCO = index

        val adapter = ArrayAdapter<String>(appContext,
                android.R.layout.simple_spinner_dropdown_item,
                locosToSelect)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        selLoco.adapter = adapter
        selLoco.setSelection(selection)

        selLocoIndex = NOTHING
        selLoco.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>, arg1: View,
                                        arg2: Int, arg3: Long) {
                selLocoIndex = arg2   // save for later use when "SAVE" pressed
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {
                selLocoIndex = NOTHING
            }
        }

        /*selLoco.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                   int arg2, long arg3) {
                int removeLocoIndex = arg2;
                //locolist.remove(removeLocoIndex);
                Log.d(TAG,"remove index "+removeLocoIndex);
                return true;
            }
        }); funktioniert nicht wie erwartet */

        val res = appContext?.resources

        val sxDialog = AlertDialog.Builder(appContext)
                //, R.style.Animations_GrowFromBottom ) => does  not work
                //.setMessage("Lok auswählen - "+locolist.name)
                .setCancelable(true)
                .setView(selSxAddressView)
                .setPositiveButton(res!!.getString(R.string.select), { dialog, id ->
                    if (selLocoIndex == NEW_LOCO) {
                        dialog.dismiss()
                        val l = Loco(NEW_LOCO_NAME)
                        locolist.add(l)
                        selectedLoco = l
                        openEditDialog()
                    } else if (selLocoIndex != NOTHING) {
                        //Toast.makeText(appContext,"Loco-index="+selLocoIndex
                        //		+" wurde selektiert", Toast.LENGTH_SHORT)
                        //		.show();
                        selectedLoco = locolist.get(selLocoIndex)
                        selectedLoco?.initFromSX()
                        dialog.dismiss()
                    }
                })
                .setNeutralButton(res!!.getString(R.string.edit), { dialog, id ->
                    if (selLocoIndex == NEW_LOCO) {
                        val l = Loco(NEW_LOCO_NAME)
                        locolist.add(l)
                        selectedLoco = l
                    } else if (selLocoIndex != NOTHING) {
                        //Toast.makeText(appContext,"Loco-index="+selLocoIndex
                        //		+" wurde selektiert", Toast.LENGTH_SHORT)
                        //		.show();
                        selectedLoco = locolist.get(selLocoIndex)
                    }
                    dialog.dismiss()
                    openEditDialog()
                })
                .setNegativeButton("Zurück", DialogInterface.OnClickListener { dialog, id ->
                    //dialog.cancel();
                })

                .show()
    }


    fun openEditDialog() {

        val factory = LayoutInflater.from(appContext)
        val selSxAddressView = factory.inflate(R.layout.alert_dialog_edit, null)
        val lName = selSxAddressView.findViewById(R.id.setname) as EditText
        val sxAddress = selSxAddressView.findViewById(R.id.picker1) as NumberPicker
        sxAddress.minValue = 1
        sxAddress.maxValue = 99
        sxAddress.wrapSelectorWheel = false
        val mass = selSxAddressView.findViewById(R.id.picker2) as NumberPicker
        mass.minValue = 1
        mass.maxValue = 5
        val vmax = selSxAddressView.findViewById(R.id.vmax_picker) as NumberPicker
        val vValues = arrayOf("30","60","90","120","160","200","250","300")
        vmax.displayedValues = vValues
        vmax.value = 160
        vmax.minValue = 0
        vmax.maxValue = vValues.size - 1
        //val newLoco = selectedLoco?.name.equals(NEW_LOCO_NAME)

        sxAddress.value = selectedLoco?.adr   ?: 1
        mass.value = selectedLoco?.mass  ?: 1
        lName.setText(selectedLoco?.name)
        vmax.value = selectedLoco?.vmax  ?: 160

        val sxDialog = AlertDialog.Builder(appContext)
                //.setMessage("")
                .setCancelable(false)
                .setView(selSxAddressView)
                .setPositiveButton("Speichern", DialogInterface.OnClickListener { dialog, id ->
                    //e.setSxAdr(sxAddress.getValue());
                    //e.setSxBit(sxBit.getValue());

                    selectedLoco?.adr = sxAddress.value
                    selectedLoco?.mass = mass.value
                    selectedLoco?.name = lName.text.toString()
                    selectedLoco?.vmax = vmax.value.toInt()
                    configHasChanged = true
                    selectedLoco?.initFromSX()
                    dialog.dismiss()
                })
                .setNeutralButton("Löschen", DialogInterface.OnClickListener { dialog, id ->
                    if (selLocoIndex != NOTHING) {
                        //Toast.makeText(appContext,"Loco-index="+selLocoIndex
                        //		+" wurde selektiert", Toast.LENGTH_SHORT)
                        //		.show();
                        openDeleteDialog(selLocoIndex)
                        selLocoIndex = 0  // reset to an existing value
                        configHasChanged = true
                    }
                    dialog.dismiss()
                })
                .setNegativeButton("Zurück", DialogInterface.OnClickListener { dialog, id ->
                    //dialog.cancel();
                    dialog.dismiss()
                })
                .create()
        sxDialog.show()
    }


    fun openDeleteDialog(index: Int) {

        Log.d(TAG, "lok löschen Ja/nein $index")

        val delLoco = locolist.get(index)

        val deleteDialog = AlertDialog.Builder(appContext)
                .setMessage("Lok " + delLoco.name + " wirklich löschen")
                .setCancelable(false)
                .setPositiveButton("Löschen") { dialog, id ->
                    //e.setSxAdr(sxAddress.getValue());
                    //e.setSxBit(sxBit.getValue());

                    locolist.remove(delLoco)
                    configHasChanged = true // flag for saving the configuration later when pausing the activity
                    if (locolist?.size >= 1) {
                        selectedLoco = locolist.get(0)
                        selectedLoco?.initFromSX()
                    } else {
                        selectedLoco = Loco()
                        locolist.add(selectedLoco!!) // at least 1 loco should be in the list
                    }

                    dialog.dismiss()
                }

                .setNegativeButton("Zurück") { dialog, id ->
                    //dialog.cancel();
                    dialog.dismiss()
                }
                .create()
        deleteDialog.show()
        return
    }




}
