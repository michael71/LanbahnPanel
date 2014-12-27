package de.blankedv.lanbahnpanel;

import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

public class Dialogs {

    private static final int MAX_ADDR = 999;
    private static final int MIN_ADDR = 1;

	static void selectAddressDialog(PanelElement el) {

		final LayoutInflater factory = LayoutInflater.from(appContext);
		final View selAddressView = factory.inflate(
				R.layout.alert_dialog_sel_address, null);
		final NumberPicker address = (NumberPicker) selAddressView
				.findViewById(R.id.picker1);
		
		address.setRange(MIN_ADDR, MAX_ADDR); 
		// TODO: replace by list of active lanbahn messages 
		
		address.setSpeed(100); // faster change for long press
		final ActivePanelElement e = (ActivePanelElement) el;
		String msg;
		address.setCurrent(e.getAdr());
		msg = "Adresse?";
		AlertDialog addrDialog = new AlertDialog.Builder(appContext)
				.setMessage(msg)
				.setCancelable(false)
				.setView(selAddressView)
				.setPositiveButton("Speichern",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Toast.makeText(appContext,"Adresse "+sxAddress.getCurrent()
								// +"/"+sxBit.getCurrent()+" wurde selektiert",
								// Toast.LENGTH_SHORT)
								// .show();
								e.setAdr(address.getCurrent());
								configHasChanged = true; // flag for saving the
															// configuration
															// later when
															// pausing the
															// activity
								dialog.dismiss();

							}
						})
				.setNegativeButton("Zurück",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// dialog.cancel();
								dialog.dismiss();
							}
						}).create();
		addrDialog.show();
		addrDialog.getWindow().setLayout(350, 400);
	}
}
