package de.blankedv.lanbahnpanel;

import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import org.xmlpull.v1.XmlSerializer;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

/**
 * WriteConfig - Utility turnout save Panel Config
 * 
 * @author Michael Blank
 * 
 * @version 1.0
 */

public class WriteConfig {

	/**
	 * writeConfigToXML
	 * 
	 * saves all PanelElements (including deducted elements) turnout an XML file
	 * 
	 * @param
	 * @return true, if succeeds - false, if not.
	 */

	public static boolean writeToXML() {

		boolean mExternalStorageWriteable;
		String state = Environment.getExternalStorageState();

        switch (state) {
            case Environment.MEDIA_MOUNTED:
                // We can read and write the media
                mExternalStorageWriteable = true;
                break;
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                // We can only read the media
                mExternalStorageWriteable = false;
                break;
            default:
                // Something else is wrong.
                mExternalStorageWriteable = false;
                break;
        }

		if (mExternalStorageWriteable) {
			
	    /* no longer used. we don't change the original config file!!!
	  		try {
	  
				File dir = new File(Environment.getExternalStorageDirectory(),
						LOCAL_DIRECTORY);
				dir.mkdir(); // make DIRECTORY - if needed
				File from = new File(Environment.getExternalStorageDirectory(),
						LOCAL_DIRECTORY + configFilename);
				String suffix = Utils.getDateTime();
				File to = new File(Environment.getExternalStorageDirectory(),
						LOCAL_DIRECTORY + configFilename + "." + suffix);
				from.renameTo(to);
			} catch (Exception e) {
				Log.e(TAG,
						"Error in renaming old config file: " + e.getMessage());
			}  */
			
			
			FileWriter fWriter = null;
			try {
				String suffix = Utils.getDateTime();
				fWriter = new FileWriter(
						Environment.getExternalStorageDirectory() + "/"
								+ DIRECTORY + configFilename + "." + suffix);
				fWriter.write(writeXml());
				fWriter.flush();
				fWriter.close();

				if (DEBUG)
					Log.d(TAG, "Config File " + configFilename + "." + suffix + " saved! ");
				configHasChanged = false; // reset flag

			} catch (Exception e) {
				Log.e(TAG, "Exception: " + e.getMessage());
				return false;
			} finally {
				if (fWriter != null) {
					try {
						fWriter.close();
					} catch (IOException e) {
						Log.e(TAG, "could not close output file!");
					}
				}
			}
		} else {
			Log.e(TAG, "external storage not writeable!");
			return false;
		}
		return true;
	}

	/**
	 * writeConfigToXML
	 * 
	 * saves all PanelElements (including deducted elements) turnout an XML file
	 * 
	 * @param
	 * @return true, if succeeds - false, if not.
	 */
	private static String writeXml() {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();

		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.text("\n");
			serializer.startTag("", "layout-config"); // namespace ="" always
			serializer.text("\n");
			serializer.startTag("", "panel");
			serializer.attribute("", "name", panelName);
			serializer.text("\n");

			// now write all panel elements to the file
			for (PanelElement pe : panelElements) {
				if (DEBUG)
					Log.d(TAG, "writing panel element " + pe.toString());
				serializer.startTag("", pe.getType());
				if (DEBUG)
					Log.d(TAG, " type=" + pe.getType());
				if (pe.name.length() > 0) {
					serializer.attribute("", "name", "" + pe.name);
				}

				serializer.attribute("", "x", "" + pe.x);
				serializer.attribute("", "y", "" + pe.y);
				if (pe.x2 != INVALID_INT) { // save only valid attributes
					serializer.attribute("", "x2", "" + pe.x2);
					serializer.attribute("", "y2", "" + pe.y2);
				}
				if (pe.xt != INVALID_INT) {
					serializer.attribute("", "xt", "" + pe.xt);
					serializer.attribute("", "yt", "" + pe.yt);
				}
				if (pe.getAdr() != INVALID_INT) {
					serializer.attribute("", "adr", "" + pe.getAdr());
				}

				serializer.endTag("", pe.getType());
				serializer.text("\n");
			}

			// write the routes
			for (Route rt : routes) {
				if (DEBUG)
					Log.d(TAG, "writing routes " + rt.toString());
				serializer.startTag("", "route");

				serializer.attribute("", "id", "" + rt.id);
				serializer.attribute("", "btn1", "" + rt.btn1);
				serializer.attribute("", "btn2", "" + rt.btn2);
				serializer.attribute("", "route", "" + rt.routeString);
				serializer.attribute("", "sensors", "" + rt.sensorsString);
				serializer.attribute("", "offending", "" + rt.offendingString);

				serializer.endTag("", "route");
				serializer.text("\n");
			}

			// write the composite routes
			for (CompRoute rt : compRoutes) {
				if (DEBUG)
					Log.d(TAG, "writing routes " + rt.toString());
				serializer.startTag("", "comproute");

				serializer.attribute("", "id", "" + rt.id);
				serializer.attribute("", "btn1", "" + rt.btn1);
				serializer.attribute("", "btn2", "" + rt.btn2);
				serializer.attribute("", "routes", "" + rt.routesString);
				
				serializer.endTag("", "comproute");
				serializer.text("\n");
			}

			serializer.endTag("", "panel");
			serializer.text("\n");
			serializer.endTag("", "layout-config");
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
