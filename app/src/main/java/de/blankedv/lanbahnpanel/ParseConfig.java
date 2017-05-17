package de.blankedv.lanbahnpanel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;


/**
 * Parse Configuration from XML file
 * 
 * @author mblank
 * 
 */
public class ParseConfig {
	
	static boolean reqRunningFlag = false;
    static final boolean DEBUG_PARSING = false;
    
	public static boolean readConfig(Context context) {
		// determine whether to read from http server or from local storage
		// during preferences loading the variables
		// configServer and configFilename are read.
		
	/*	if (!configServer.isEmpty()) {
			if (DEBUG) Log.d(TAG,"configServer="+configServer+" trying to read from the server.");
			
			if (DEBUG) Log.d(TAG,"configFilename="+configFilename);
			// try to read from server - if not successful, read from local storage
			MakeHttpRequestTask httpTask = new MakeHttpRequestTask();
			String req = "http://"+configServer+"/"+configFilename;
			httpTask.execute(req);
			
		} else {  */
			//if (DEBUG) Log.d(TAG,"configServer string is empty, reading from local file.");
			if (DEBUG) Log.d(TAG,"reading data from configFilename="+CONFIG_FILENAME);
			// read config from file
			String error = readConfigFromFile(context);
			if (!error.isEmpty() ){
				Toast.makeText(context, error, Toast.LENGTH_LONG).show();
				return false;
			} else {
				return true;
			}
		// }
		// return true;
	}

	/**
	 * 
	 * read all PanelElements from a configuration (XML) file and add deducted
	 * turnouts if needed. The results will be put into global ArrayList
	 * "panelElements" turnouts and signals must be defined before sensors and
	 * turnouts,signal and sensors must be defined before routes (because the
	 * routes are dependent on these panel elements.)
	 * 
	 * all dimension will be scaled with the global value of "prescale"
	 * 
	 * @return true, if succeeds - false, if not.
	 * 
	 */
	private static String readConfigFromFile(Context context) {

		// TODO determine whether we can read a config file from a web server

		boolean mExternalStorageAvailable;
		String state = Environment.getExternalStorageState();
        String error;

        switch (state) {
            case Environment.MEDIA_MOUNTED:
                // We can read and write the media
                mExternalStorageAvailable = true;
                break;
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                // We can only read the media
                mExternalStorageAvailable = true;
                break;
            default:
                // Something else is wrong. It may be one of many other states, but
                // all we need to know is we can neither read nor write
                mExternalStorageAvailable = false;
                break;
        }

		if (mExternalStorageAvailable) {
			try {
				File f = new File(Environment.getExternalStorageDirectory()
						+ "/" + LOCAL_DIRECTORY + "/"+ CONFIG_FILENAME);
				// auf dem Nexus 7 unter /mnt/shell/emulated/0/lanbahnpanel
				FileInputStream fis;
				if (!f.exists()) {
					Log.e(TAG,"config file="+CONFIG_FILENAME+" not found, using demo data.");
                    InputStream demoIs = context.getAssets().open(DEMO_FILE);
					configHasChanged = true;
					error=readXMLConfigFile(demoIs);
					demoIs.close();

					// create the folder for later use (if it does not exist already)
					File f2 = new File(Environment.getExternalStorageDirectory()
							+ "/" + LOCAL_DIRECTORY + "/");
					if (!f2.exists()) {
                        f2.mkdirs();
                        Log.e(TAG,"creating 'lanbahnpanel' folder");
                    }
				} else {
					fis = new FileInputStream(f);
					error=readXMLConfigFile(fis);
				}
			} catch (FileNotFoundException e) {
				Log.e(TAG,"FileNotFound "+e.getMessage());
				return "FileNotFound "+e.getMessage();
			} catch (IOException e) {
				Log.e(TAG,"IOException "+e.getMessage());
				return "IOException "+e.getMessage();
			} 
		} else {
			Toast.makeText(context, "ERROR:External storage not readable",
					Toast.LENGTH_LONG).show();
			return "ERROR:External storage not readable";
		}

		return error;
	}

	private static String readXMLConfigFile(InputStream fis) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;

		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			Log.e(TAG, "ParserConfigException Exception - " + e1.getMessage());
			return "ParserConfigException";
		}
		Document doc;
		try {
			doc = builder.parse(fis);
			panelElements = parsePanelElements(doc);
			PanelElement.scaleAll();
			routes = parseRoutes(doc); // can be done only after all panel
										// elements have been read
			Route.calcOffendingRoutes(); // calculate offending routes
			compRoutes = parseCompRoutes(doc); // can be done only after routes
												// have been read
			lampButtons = parseLampGroups(doc);
		} catch (SAXException e) {
			Log.e(TAG, "SAX Exception - " + e.getMessage());
			return "SAX Exception - " + e.getMessage();
		} catch (IOException e) {
			Log.e(TAG, "IO Exception - " + e.getMessage());
			return  "IO Exception - " + e.getMessage();
		}

		return "";
	}

	private static ArrayList<PanelElement> parsePanelElements(Document doc) {
		// assemble new ArrayList of tickets.
		ArrayList<PanelElement> pes = new ArrayList<>();
		NodeList items;
		Element root = doc.getDocumentElement();

		items = root.getElementsByTagName("panel");
		if (DEBUG)
			Log.d(TAG, "config: " + items.getLength() + " panel");
		panelName = parsePanelName(items.item(0));

		// NamedNodeMap attributes = item.getAttributes();
		// Node theAttribute = attributes.items.item(i);

		// look for TrackElements - this is the lowest layer
		items = root.getElementsByTagName("track");
		if (DEBUG)
			Log.d(TAG, "config: " + items.getLength() + " track");
		for (int i = 0; i < items.getLength(); i++) {
			pes.add(parseTrack(items.item(i)));
		}

		// look for existing and known turnouts - on top of track
		items = root.getElementsByTagName("turnout");
		if (DEBUG)
			Log.d(TAG, "config: " + items.getLength() + " turnouts");
		for (int i = 0; i < items.getLength(); i++) {
			pes.add(parseTurnout(items.item(i)));
		}

		// look for signals - on top of track
		items = root.getElementsByTagName("signal");
		if (DEBUG)
			Log.d(TAG, "config: " + items.getLength() + " signals");
		for (int i = 0; i < items.getLength(); i++) {
			pes.add(parseSignal(items.item(i)));
		}

		// check for intersection of track, if new, add a turnout with unknown
		// lanbahn address
		for (int i = 0; i < pes.size(); i++) {
			PanelElement p = pes.get(i);

			for (int j = i + 1; j < pes.size(); j++) {
				PanelElement q = pes.get(j);

				PanelElement panelelement = LinearMath.trackIntersect(p, q);

				if (panelelement != null) {
					if (panelelement.getType().equals("doubleslip")) {
						// do nothing in the meantime
						// TODO implement for doubleslip a similar method as
						// with turnout
						// TODO currently doubleslip are two turnouts, separated
						// by a single pixel
						if (DEBUG_PARSING)
							Log.d(TAG, "(i,j)=(" + i + "," + j
									+ ") new? doubleslip found at x="
									+ panelelement.x + " y=" + panelelement.y);

					} else {
						// there is an intersection with a turnout => make new
						// turnout
						if (DEBUG_PARSING)
							Log.d(TAG, "(i,j)=(" + i + "," + j
									+ ") new? turnout found at x="
									+ panelelement.x + " y=" + panelelement.y
									+ " xc=" + panelelement.x2 + " yc="
									+ panelelement.y2 + " xt="
									+ panelelement.xt + " yt="
									+ panelelement.yt);

						// check whether this turnout is already known
						boolean known = false;
						for (PanelElement e : pes) {
							if ((e.getType().equals("turnout"))
									&& (e.x == panelelement.x)
									&& (e.y == panelelement.y)) {
								// at same position => match
								known = true;
								break;
							}
						}
						if (!known) {
							configHasChanged = true;
							pes.add(new TurnoutElement(panelelement));
						}
					}
				}

			}
		}

		// nach dem Track => on top
		items = root.getElementsByTagName("routebutton");
		if (DEBUG)
			Log.d(TAG, "config: " + items.getLength() + " routebuttons");
		for (int i = 0; i < items.getLength(); i++) {
			pes.add(parseRouteButton(items.item(i)));
		}

		// look for sensors
		// SENSORS als LETZTE !!!! important (sind damit immer "on top")

		items = root.getElementsByTagName("sensor");
		if (DEBUG)
			Log.d(TAG, "config: " + items.getLength() + " sensors");
		for (int i = 0; i < items.getLength(); i++) {
			pes.add(parseSensor(items.item(i)));
		}

		return pes;
	}

	private static TurnoutElement parseTurnout(Node item) {

		TurnoutElement pe = new TurnoutElement();
		NamedNodeMap attributes = item.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node theAttribute = attributes.item(i);
			// if (DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
			// theAttribute.getNodeValue());
			if (theAttribute.getNodeName().equals("name")) {
				pe.name = theAttribute.getNodeValue();
			} else if (theAttribute.getNodeName().equals("x")) {
				pe.x = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("y")) {
				pe.y = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("x2")) {
				pe.x2 = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("y2")) {
				pe.y2 = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("xt")) {
				pe.xt = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("yt")) {
				pe.yt = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("adr")) {
				pe.setAdr(Integer.parseInt(theAttribute.getNodeValue()));
			} else {
				if (DEBUG_PARSING)
					Log.d(TAG,
							"unknown attribute " + theAttribute.getNodeName()
									+ " in config file");
			}
		}

		return pe;

	}

	private static int getPositionNode(Node a) {
		return Integer.parseInt(a.getNodeValue());
	}

	private static SignalElement parseSignal(Node item) {

		SignalElement pe = new SignalElement();
		NamedNodeMap attributes = item.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node theAttribute = attributes.item(i);
			// if (DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
			// theAttribute.getNodeValue());
			if (theAttribute.getNodeName().equals("name")) {
				pe.name = theAttribute.getNodeValue();
			} else if (theAttribute.getNodeName().equals("x")) {
				pe.x = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("y")) {
				pe.y = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("x2")) {
				pe.x2 = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("y2")) {
				pe.y2 = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("adr")) {
				pe.setAdr(Integer.parseInt(theAttribute.getNodeValue()));
			} else {
				if (DEBUG_PARSING)
					Log.d(TAG,
							"unknown attribute " + theAttribute.getNodeName()
									+ " in config file");
			}
		}

		return pe;

	}

	private static RouteButtonElement parseRouteButton(Node item) {

		RouteButtonElement pe = new RouteButtonElement();
		NamedNodeMap attributes = item.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node theAttribute = attributes.item(i);
			// if (DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
			// theAttribute.getNodeValue());
			if (theAttribute.getNodeName().equals("name")) {
				pe.name = theAttribute.getNodeValue();
			} else if (theAttribute.getNodeName().equals("x")) {
				pe.x = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("y")) {
				pe.y = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("route")) {
				pe.route = theAttribute.getNodeValue();
			} else if (theAttribute.getNodeName().equals("adr")) {
				pe.setAdr(Integer.parseInt(theAttribute.getNodeValue()));
			} else {
				if (DEBUG_PARSING)
					Log.d(TAG,
							"unknown attribute " + theAttribute.getNodeName()
									+ " in config file");
			}
		}

		return pe;

	}

	private static String parsePanelName(Node item) {
		NamedNodeMap attributes = item.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node theAttribute = attributes.item(i);
			if (DEBUG_PARSING)
				Log.d(TAG,
						theAttribute.getNodeName() + "="
								+ theAttribute.getNodeValue());

			if (theAttribute.getNodeName().equals("name")) {
				String name = theAttribute.getNodeValue();
				return name;

			}
		}
		return "";
	}

	private static SensorElement parseSensor(Node item) {
		// ticket node can be Incident oder UserRequest
		SensorElement pe = new SensorElement();
		pe.x2 = INVALID_INT; // to be able to distinguish between
			// different types of sensors (LAMP or dashed track)
		NamedNodeMap attributes = item.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node theAttribute = attributes.item(i);
			// if (DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
			// theAttribute.getNodeValue());
			if (theAttribute.getNodeName().equals("name")) {
				pe.name = theAttribute.getNodeValue();
			} else if (theAttribute.getNodeName().equals("x")) {
				pe.x = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("y")) {
				pe.y = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("x2")) {
				pe.x2 = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("y2")) {
				pe.y2 = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("adr")) {
				pe.setAdr(Integer.parseInt(theAttribute.getNodeValue()));
			} else {
				if (DEBUG_PARSING)
					Log.d(TAG,
							"unknown attribute " + theAttribute.getNodeName()
									+ " in config file");
			}
		}

		return pe;

	}

	private static int getValue(String s) {
		float b = Float.parseFloat(s);
		return (int) b;
	}

	private static PanelElement parseTrack(Node item) {
		// track is element of type "PanelElement" (=not active)
		PanelElement pe = new PanelElement();
		NamedNodeMap attributes = item.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node theAttribute = attributes.item(i);
			// if (DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
			// theAttribute.getNodeValue());
			if (theAttribute.getNodeName().equals("x")) {
				pe.x = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("y")) {
				pe.y = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("x2")) {
				pe.x2 = getPositionNode(theAttribute);
			} else if (theAttribute.getNodeName().equals("y2")) {
				pe.y2 = getPositionNode(theAttribute);
			} else {
				if (DEBUG_PARSING)
					Log.d(TAG,
							"unknown attribute " + theAttribute.getNodeName()
									+ " in config file");
			}
		}

		if (pe.x2 < pe.x) { // swap 1/2, x2 must always be >x
			int tmp = pe.x;
			pe.x = pe.x2;
			pe.x2 = tmp;
			tmp = pe.y;
			pe.y = pe.y2;
			pe.y2 = tmp;
		}
		return pe;

	}

	private static String getConcatNodeValues(Node prop) {
		// behaves well for non-existing nodes and for node values which are
		// broken into several values because of special characters like '"'
		// needed for the android code - this problem only exists in
		// Android xml library and not on the PC !!
		if (prop.hasChildNodes()) { // false for optional attributes
			StringBuilder text = new StringBuilder();
			NodeList chars = prop.getChildNodes();
			for (int k = 0; k < chars.getLength(); k++) {
				text.append(chars.item(k).getNodeValue());
			}
			return text.toString().trim();
		} else {
			return (""); // return empty string if empty
		}
	}

	private static ArrayList<Route> parseRoutes(Document doc) {
		// assemble new ArrayList of tickets.
		ArrayList<Route> myroutes = new ArrayList<>();
		NodeList items;
		Element root = doc.getDocumentElement();

		// items = root.getElementsByTagName("panel");

		// look for routes - this is the lowest layer
		items = root.getElementsByTagName("route");
		if (DEBUG)
			Log.d(TAG, "config: " + items.getLength() + " routes");
		for (int i = 0; i < items.getLength(); i++) {
			Route rt = parseRoute(items.item(i));
			if (rt != null)
				myroutes.add(rt);
		}

		return myroutes;
	}

	private static Route parseRoute(Node item) {
		// ticket node can be Incident oder UserRequest
		int id = INVALID_INT;
		int btn1 = INVALID_INT;
		int btn2 = INVALID_INT;
		String route = null, sensors = null;
		String offending = ""; // not mandatory

		NamedNodeMap attributes = item.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node theAttribute = attributes.item(i);
			// if (DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
			// theAttribute.getNodeValue());
			if (theAttribute.getNodeName().equals("id")) {
				id = getValue(theAttribute.getNodeValue());
			} else if (theAttribute.getNodeName().equals("btn1")) {
				btn1 = getValue(theAttribute.getNodeValue());
			} else if (theAttribute.getNodeName().equals("btn2")) {
				btn2 = getValue(theAttribute.getNodeValue());
			} else if (theAttribute.getNodeName().equals("route")) {
				route = theAttribute.getNodeValue();
			} else if (theAttribute.getNodeName().equals("sensors")) {
				sensors = theAttribute.getNodeValue();
			} else if (theAttribute.getNodeName().equals("offending")) {
				offending = theAttribute.getNodeValue();
			} else {
				if (DEBUG_PARSING)
					Log.d(TAG,
							"unknown attribute " + theAttribute.getNodeName()
									+ " in config file");
			}
		}

		// check for mandatory and valid input data
		if (id == INVALID_INT) {
			// missing info, log error
			Log.e(TAG, "missing id= info in route definition");
			return null;
		} else if (btn1 == INVALID_INT) {
			Log.e(TAG, "missing btn1= info in route definition");
			return null;
		} else if (btn2 == INVALID_INT) {
			Log.e(TAG, "missing btn2= info in route definition");
			return null;
		} else if (route == null) {
			Log.e(TAG, "missing route= info in route definition");
			return null;
		} else if (sensors == null) {
			Log.e(TAG, "missing sensors= info in route definition");
			return null;
		} else {
			// everything is o.k.

			return new Route(id, btn1, btn2, route, sensors, offending);
		}

	}

	private static ArrayList<CompRoute> parseCompRoutes(Document doc) {
		// assemble new ArrayList of tickets.
		ArrayList<CompRoute> myroutes = new ArrayList<>();
		NodeList items;
		Element root = doc.getDocumentElement();

		// look for comp routes - this is the lowest layer
		items = root.getElementsByTagName("comproute");
		if (DEBUG)
			Log.d(TAG, "config: " + items.getLength() + " comproutes");
		for (int i = 0; i < items.getLength(); i++) {
			CompRoute rt = parseCompRoute(items.item(i));
			if (rt != null)
				myroutes.add(rt);
		}

		return myroutes;
	}

	private static CompRoute parseCompRoute(Node item) {
		//
		int id = INVALID_INT;
		int btn1 = INVALID_INT;
		int btn2 = INVALID_INT;
		String routes = null;

		NamedNodeMap attributes = item.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node theAttribute = attributes.item(i);
			// if (DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
			// theAttribute.getNodeValue());
			if (theAttribute.getNodeName().equals("id")) {
				id = getValue(theAttribute.getNodeValue());
			} else if (theAttribute.getNodeName().equals("btn1")) {
				btn1 = getValue(theAttribute.getNodeValue());
			} else if (theAttribute.getNodeName().equals("btn2")) {
				btn2 = getValue(theAttribute.getNodeValue());
			} else if (theAttribute.getNodeName().equals("routes")) {
				routes = theAttribute.getNodeValue();
			} else {
				if (DEBUG_PARSING)
					Log.d(TAG,
							"unknown attribute " + theAttribute.getNodeName()
									+ " in config file");
			}
		}

		// check for mandatory and valid input data
		if (id == INVALID_INT) {
			// missing info, log error
			Log.e(TAG, "missing id= info in route definition");
			return null;
		} else if (btn1 == INVALID_INT) {
			Log.e(TAG, "missing btn1= info in route definition");
			return null;
		} else if (btn2 == INVALID_INT) {
			Log.e(TAG, "missing btn2= info in route definition");
			return null;
		} else if (routes == null) {
			Log.e(TAG, "missing routes= info in route definition");
			return null;
		} else {
			// everything is o.k.

			return new CompRoute(id, btn1, btn2, routes);
		}

	}

	private static ArrayList<LampGroup> parseLampGroups(Document doc) {
		// assemble new ArrayList of tickets.
		ArrayList<LampGroup> lampGroups = new ArrayList<>();
		NodeList items;
		Element root = doc.getDocumentElement();

		// look for comp routes - this is the lowest layer
		items = root.getElementsByTagName("lampgroup");
		if (DEBUG)
			Log.d(TAG, "config: " + items.getLength() + " lampgroups");
		if (items.getLength() > MAX_LAMP_BUTTONS)
			Log.e(TAG, "ERROR: too many (>" + MAX_LAMP_BUTTONS
					+ ") lampgroups defined, n=" + items.getLength());

		for (int i = 0; i < Math.min(items.getLength(), MAX_LAMP_BUTTONS); i++) {
			LampGroup lg = parseLampGroup(items.item(i));
			if (lg != null)
				lampGroups.add(lg);
		}

		return lampGroups;
	}

	private static LampGroup parseLampGroup(Node item) {
		//
		int btnPos = INVALID_INT;
		String addresses = "";
		int[] addrs;

		NamedNodeMap attributes = item.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node theAttribute = attributes.item(i);
			// if (DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
			// theAttribute.getNodeValue());
			if (theAttribute.getNodeName().equals("btn")) {
				btnPos = getValue(theAttribute.getNodeValue());
			} else if (theAttribute.getNodeName().equals("lamps")) {
				addresses = theAttribute.getNodeValue();
			} else {
				if (DEBUG_PARSING)
					Log.d(TAG,
							"unknown attribute " + theAttribute.getNodeName()
									+ " in config file");
			}
		}

		// check for mandatory and valid input data
		if (btnPos == INVALID_INT) {
			// missing info, log error
			Log.e(TAG, "missing btn number info in lampgroup definition");
			return null;
		} else if (addresses.length() == 0) {
			Log.e(TAG, "missing lamps info in lampgroup definition");
			return null;
		} else {
			// input is o.k., now parse addresses
			String a[] = addresses.split(",");
			final int len = a.length;
			addrs = new int[len];
			try {
				for (int i = 0; i < a.length; i++) {
					addrs[i] = Integer.parseInt(a[i]);
				}
			} catch (NumberFormatException e) {
				Log.e(TAG,
						"NumberFormatException when parsing lamproup definition");
				return null;
			}

			// LampGroup(int pos, int[] lamps) {
			return new LampGroup(btnPos, addrs);
		}

	}

	static class MakeHttpRequestTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected String doInBackground(String... req) {
			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			HttpGet request = new HttpGet();

			HttpResponse response;
			
			// do not log password, log only search expression
			if (DEBUG) Log.d(TAG, "http request string=" + req[0]);
			try {
				request.setURI(new URI(req[0]));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			try {
				response = client.execute(request);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
                return "ERROR";
			}
			String status = response.getStatusLine().toString();
			if (DEBUG)
				Log.d(TAG, "http req status: " + status);

			InputStream instream;
			try {
				instream = response.getEntity().getContent();
				Log.d(TAG,"reading config data from server.");
				readXMLConfigFile(instream);
			} catch (IllegalStateException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            client.close();
			return status;
		}

		@Override
		protected void onPostExecute (String stat) {
			// request worked fine, retrieved some data
			Log.d(TAG,"reading finished. status="+stat);
			Log.d(TAG,"now reload panel.");
			
		}

	}
}
