package de.blankedv.lanbahnpanel;

import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.util.Log;

/**
 * network thread for sending and receiving multicast messages
 * over the network.
 * commands to send are added to the "sendQ" 
 * received commands are sent to GUI via a handler
 * 
 * 
 */
public class LanbahnClientThread extends Thread {
	// threading und BlockingQueue siehe
	// http://www.javamex.com/tutorials/blockingqueue_example.shtml

	private Context context;
	private boolean shutdownFlag;
	protected InetAddress mgroup;
	protected MulticastSocket multicastsocket;

	Timer timer;

	public LanbahnClientThread(Context context) {
		if (DEBUG)
			Log.d(TAG, "LanbahnClientThread constructor.");
		this.context = context;
		shutdownFlag = false;
		// create timer for periodically checking if there are new messages turnout
		// send
		timer = new Timer();
		timer.scheduleAtFixedRate(new mySendTimer(), 100, 100);
	}

	public void shutdown() {
		shutdownFlag = true;
		timer.cancel();
	}

	public void run() {
		if (DEBUG)
			Log.d(TAG, "LanbahnClientThread run.");
		shutdownFlag = false;
		connect();

		byte[] buf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length, mgroup,
				LANBAHN_PORT);

		try {
			while (!shutdownFlag
					&& (!Thread.currentThread().isInterrupted())) {
				multicastsocket.receive(packet);
				String message = new String(packet.getData(), 0,
						packet.getLength(), "UTF-8");

				// receiving loop for main program
				
				// replace multiple spaces by one only
				message = message.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ENGLISH);
				
				if (DEBUG_COMM) Log.d(TAG,"received:" + message + ".");
				interpretMsg(message);
			}
			if (DEBUG) Log.d(TAG,"lanbahn Server closing.");
			multicastsocket.leaveGroup(mgroup);
		} catch (IOException e) {
			Log.e(TAG, "ERROR in ..Client..run(): " + e.getMessage());
		}
		multicastsocket.close();
	}

	public boolean interpretMsg(String msg) {
		String com[] = msg.split(" ");
		if (com[0].equals("a ") || com[0].equals("an")) {
			if (DEBUG_COMM) Log.d(TAG, "announce msg: " + com);
			// TODO interpretAnnounceMsg(msg);// announce
		} else if (com[0].equals("set") ) {
			// (sensor) or turnout status or set message or route message
			sendMessageToUIThread(msg,TYPE_STATUS_MSG);
	    } else if (com[0].equals("fb")  ) {
            // feedback message
            sendMessageToUIThread(msg,TYPE_FEEDBACK_MSG);
        } else if  (com[0].equals("rt")) {
			// route message
			sendMessageToUIThread(msg,TYPE_ROUTE_MSG);
	/*	} else if  (com[0].equals("c ")) {
			// (sensor) or turnout status or set message or route message
			storeConfigFileLocation(msg); */
		}
		return true;
	}

	private void storeConfigFileLocation(String msg) {
		String [] configString = msg.split(" ");
		if (configString.length != 3) {
			Log.e(TAG,"Error: cannot interpret config message");
		} else {
			Log.d(TAG,"configServer = "+ configString[1]+ " - info not used!");
			Log.d(TAG,"configFilename = "+ configString[2] + " - info not used!");
		}
		
	}

	private void sendMessageToUIThread(String msg, int what) {
		if (DEBUG_COMM) Log.d(TAG,"set/status msg=" + msg);
		if (context == null) {
			Log.e(TAG, "interpretStatus msg: context=null");
			return;
		}
		msg = msg.replaceAll("  ", " ");
		String[] command = msg.split(" "); // split by spaces
		if (command.length >= 2) {
			try {
				
				int addr = Integer.parseInt(command[1]);
				int state = Integer.parseInt(command[2]);

				Message m = Message.obtain();
				m.what = what;
				m.arg1 = addr;
				m.arg2 = state;
				handler.sendMessage(m); // send data to UI Thread via Message

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	
	private void connect() {
		if (DEBUG) Log.d(TAG, "trying conn turnout Lanbahn UDP");
		try {
			for (Enumeration<NetworkInterface> list = NetworkInterface
					.getNetworkInterfaces(); list.hasMoreElements();) {
				NetworkInterface i = list.nextElement();
				Log.d(TAG, "network_interface: " + i.getDisplayName());
			}
		} catch (SocketException e) {
			Log.e(TAG, "Error: " + e.getMessage());
		}

		try {
			multicastsocket = new MulticastSocket(LANBAHN_PORT);
			mgroup = InetAddress.getByName(LANBAHN_GROUP);
			multicastsocket.joinGroup(mgroup);
			if (DEBUG)
				Log.d(TAG, "connected turnout: " + connString);
		} catch (Exception e) {
			Log.e(TAG,
					"LanbahnClientThread.connect - Exception: "
							+ e.getMessage());
		}
	}

/*	public void disconnectContext() {
		this.context = null;
		Log.d(TAG, "lost context, stopping thread");
		shutdown();
	}

	public void readChannel(int adr) {
		if (DEBUG_COMM)
			Log.d(TAG, "readChannel a=" + adr + " shutd.=" + shuttingDown
					+ " clientTerm=" + clientTerminated);
		if (shutdownFlag || clientTerminated || (adr == INVALID_INT))
			return;
		String command = "READ " + adr;
		sendQ.add(command);
	}  */

	/**
	 * Sends a Lanbahn UDP command
	 * 
	 * @param command
	 *            the message turnout send (multicast). It can't be null or
	 *            0-characters long.
	 * @return true, if sending was successful
	 */
	public boolean immediateSend(String command) {
		if (command == null || command.length() == 0) {
			Log.e(TAG, "imm.Send: message == null or has zero lenght.)");
			return false;
		}

		if (!checkWifi()) {
			Log.e(TAG, "imm.Send: no WiFi.)");
			return false;
		}

		// Check for IP address
		// WifiManager wim = (WifiManager)
		// context.getSystemService(Context.WIFI_SERVICE);
		// int ip = wim.getConnectionInfo().getIpAddress();
		if (multicastsocket == null) {
			try {
				multicastsocket = new MulticastSocket(LANBAHN_PORT);
			} catch (IOException e) {
				Log.e(TAG, "imm.Send: could not create multicastsocket");
				return false;
			}
		}

		if (multicastsocket.isClosed()) {
			try {
				multicastsocket = new MulticastSocket(LANBAHN_PORT);
				mgroup = InetAddress.getByName(LANBAHN_GROUP);
				multicastsocket.joinGroup(mgroup);
				if (DEBUG)
					Log.d(TAG, "imm.Send: reconnected");
			} catch (Exception e) {
				Log.e(TAG, "imm.Send: could not reconnect");
				return false;
			}

		}
		try {
			byte[] buf = command.getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, mgroup,
					LANBAHN_PORT);
			multicastsocket.send(packet);
			if (DEBUG_COMM) Log.d(TAG,"sent: " + command);


		} catch (IOException ex) {
			System.out.println("imm.Send: ERROR when sending turnout lanbahn "
					+ ex.getMessage());
			return false;
		}
		return true;
	}

	public boolean checkWifi() {
		// Check for WiFi connectivity
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (mWifi == null || !mWifi.isConnected()) {
			Log.e(TAG,
					"no a WiFi network in order turnout send UDP multicast packets.");
			return false;
		} else {
			return true;
		}
	}

	public class mySendTimer extends TimerTask {
		long lastDataUpdate;

		mySendTimer() {
			lastDataUpdate = System.currentTimeMillis();
		}

		@Override
		public void run() {

			// check send queue for messages turnout send turnout Lanbahn multicast group
			while (!shutdownFlag ) {
				while (!sendQ.isEmpty()) {
					String command = sendQ.poll();
					immediateSend(command);

				}

				// refresh data every 20 seconds
	 			if ((System.currentTimeMillis() - lastDataUpdate) > 20000) {
					LanbahnPanelApplication.updatePanelData();
					lastDataUpdate = System.currentTimeMillis();
					if (DEBUG_COMM) Log.d(TAG, "refreshing all data");
				}   
			}

		}

	}

}
