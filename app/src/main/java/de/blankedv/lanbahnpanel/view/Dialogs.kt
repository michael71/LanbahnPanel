package de.blankedv.lanbahnpanel.view

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import de.blankedv.lanbahnpanel.R
import android.content.DialogInterface
import de.blankedv.lanbahnpanel.loco.Loco


import android.util.Log
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import de.blankedv.lanbahnpanel.model.*


/**
 * predefined dialogs
 *
 * TODO review kotlin code
 */
object Dialogs {

    private var selLocoIndex: Int = 0
    private val NOTHING = 99999
    private val NEW_LOCO_NAME = "+ Neue Lok"

    fun selectLoco() {
        if (prefs.getBoolean(KEY_LOCAL_LOCO_LIST,false)) {
            selectLocoEditDialog()
        } else {
            selectLocoDialog()
        }
    }

    private fun selectLocoEditDialog() {

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


        val res = appContext?.resources

        val sxDialog = AlertDialog.Builder(appContext)
                //, R.style.Animations_GrowFromBottom ) => does  not work
                //.setMessage("Lok auswählen - "+locolist.name)
                .setCancelable(true)
                .setView(selSxAddressView)
                .setPositiveButton(res!!.getString(R.string.select)) { dialog, id ->
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
                }
                .setNeutralButton(res!!.getString(R.string.edit)) { dialog, id ->
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
                }
                .setNegativeButton("Zurück") { dialog, id ->     //dialog.cancel();
                }

                .show()

    }

    private fun selectLocoDialog() {

        val factory = LayoutInflater.from(appContext)
        val selSxAddressView = factory.inflate(R.layout.alert_dialog_sel_loco_from_list, null)
        val selLoco = selSxAddressView.findViewById(R.id.spinner) as Spinner

        val locosToSelect = arrayOfNulls<String>(locolist?.size)

        var index = 0
        var selection = 0
        for (l in locolist) {
            locosToSelect[index] = l.name + " (" + l.adr + ")"
            if (l == selectedLoco) {
                selection = index
            }
            index++
        }

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


        val res = appContext?.resources

        val sxDialog = AlertDialog.Builder(appContext)
                //, R.style.Animations_GrowFromBottom ) => does  not work
                //.setMessage("Lok auswählen - "+locolist.name)
                .setCancelable(true)
                .setView(selSxAddressView)
                .setPositiveButton(res!!.getString(R.string.select)) { dialog, id ->
                    if (selLocoIndex != NOTHING) {
                        //Toast.makeText(appContext,"Loco-index="+selLocoIndex
                        //		+" wurde selektiert", Toast.LENGTH_SHORT)
                        //		.show();
                        selectedLoco = locolist.get(selLocoIndex)
                        selectedLoco?.initFromSX()
                        dialog.dismiss()
                    }
                }
                .setNegativeButton("Zurück") { dialog, id ->     //dialog.cancel();
                }

                .show()

    }



    private fun openEditDialog() {

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


    private fun openDeleteDialog(index: Int) {

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
