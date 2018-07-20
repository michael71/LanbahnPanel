package de.blankedv.lanbahnpanel.view

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.NumberPicker
import de.blankedv.lanbahnpanel.R
import de.blankedv.lanbahnpanel.elements.ActivePanelElement
import android.content.DialogInterface
import de.blankedv.lanbahnpanel.elements.PanelElement
import de.blankedv.lanbahnpanel.loco.WriteLocos
import de.blankedv.lanbahnpanel.loco.Loco

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.EditText
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import de.blankedv.lanbahnpanel.model.*


/**
 * predefined dialogs
 *
 * TODO review kotlin code
 */
object Dialogs {

    private val MAX_ADDR = 999
    private val MIN_ADDR = 1

    private var selLocoIndex: Int = 0
    private val NOTHING = 99999
    private val NEW_LOCO_NAME = "+ NEUE LOK (3)"

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


        val sxDialog = AlertDialog.Builder(appContext)
                //, R.style.Animations_GrowFromBottom ) => does  not work
                //.setMessage("Lok auswählen - "+locolist.name)
                .setCancelable(true)
                .setView(selSxAddressView)
                .setPositiveButton("Auswählen", DialogInterface.OnClickListener { dialog, id ->
                    if (selLocoIndex === NEW_LOCO) {
                        dialog.dismiss()
                        val l = Loco(NEW_LOCO_NAME)
                        locolist.add(l)
                        selectedLoco = l
                        openEditDialog()
                    } else if (selLocoIndex !== NOTHING) {
                        //Toast.makeText(appContext,"Loco-index="+selLocoIndex
                        //		+" wurde selektiert", Toast.LENGTH_SHORT)
                        //		.show();
                        selectedLoco = locolist.get(selLocoIndex)
                        selectedLoco?.initFromSX()
                        dialog.dismiss()
                    }
                })
                .setNeutralButton("Edit", DialogInterface.OnClickListener { dialog, id ->
                    if (selLocoIndex === NEW_LOCO) {
                        val l = Loco(NEW_LOCO_NAME)
                        locolist.add(l)
                        selectedLoco = l
                    } else if (selLocoIndex !== NOTHING) {
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
        vmax.minValue = 30
        vmax.maxValue = 300


        val newLoco = selectedLoco?.name.equals(NEW_LOCO_NAME)

        sxAddress.value = selectedLoco?.adr   ?: 1
        mass.value = selectedLoco?.mass  ?: 1
        lName.setText(selectedLoco?.name)

        val sxDialog = AlertDialog.Builder(appContext)
                //.setMessage("")
                .setCancelable(false)
                .setView(selSxAddressView)
                .setPositiveButton("Speichern", DialogInterface.OnClickListener { dialog, id ->
                    //e.setSxAdr(sxAddress.getValue());
                    //e.setSxBit(sxBit.getValue());

                    locoConfigHasChanged = true // flag for saving the configuration later when pausing the activity
                    selectedLoco?.adr = sxAddress.value
                    selectedLoco?.mass = mass.value
                    selectedLoco?.name = lName.text.toString()
                    selectedLoco?.vmax = 10 * (vmax.value / 10)
                    WriteLocos.writeToXML()
                    selectedLoco?.initFromSX()
                    dialog.dismiss()
                })
                .setNeutralButton("Löschen", DialogInterface.OnClickListener { dialog, id ->
                    if (selLocoIndex !== NOTHING) {
                        //Toast.makeText(appContext,"Loco-index="+selLocoIndex
                        //		+" wurde selektiert", Toast.LENGTH_SHORT)
                        //		.show();
                        openDeleteDialog(selLocoIndex)
                        selLocoIndex = 0  // reset to an existing value
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
                    locoConfigHasChanged = true // flag for saving the configuration later when pausing the activity
                    if (locolist?.size >= 1) {
                        selectedLoco = locolist.get(0)
                        selectedLoco?.initFromSX()
                    } else {
                        selectedLoco = Loco()
                        locolist.add(selectedLoco!!) // at least 1 loco should be in the list
                    }
                    WriteLocos.writeToXML()

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
