package de.widemeadows.android.bluetoothspptest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.UUID;

/**
 * Klasse, welche die Bluetooth-Funktionalität
 * bereitstellt
 */
public final class BluetoothService {

	/**
	 * Das Logging-Tag
	 */
	@NotNull
	private static final String TAG = "BluetoothService";
	
	/**
	 * UUID für SPP-Protokoll
	 */
	@NotNull
	private static final UUID uuidSpp = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	/**
	 * Der Anwendungskontext
	 */
	private static Context applicationContext;

	/**
	 * Gibt an, ob die Klasse initialisiert wurde
	 */
	private static boolean initialized;

	/**
	 * Der Intent-Filter für die Broadcasts
	 */
	@NotNull
	private static final IntentFilter broadcastIntentFilter = new IntentFilter();

	/**
	 * Der Broadcast-Receiver
	 */
	private static BroadcastReceiver broadcastReceiver;

	/**
	 * Keine Instanzen erlauben
	 */
	private BluetoothService() {}

	/**
	 * Der Bluetooth-Adapter
	 */
	private static BluetoothAdapter btAdapter;

	/**
	 * Der Event-Receiver
	 */
	private static IBluetoothServiceEventReceiver eventReceiver;

	/**
	 * Handler für den event receiver
	 */
	@NotNull
	private final static Handler eventReceiverHandler = new Handler();

	/**
	 * Das verbundene Gerät
	 */
	@Nullable
	private static BluetoothDevice connectedDevice;

	/**
	 * Der Socket des verbundenen Gerätes
	 */
	@Nullable
	private static BluetoothSocket connectedSocket;

	/**
	 * Der Ausgabestream
	 */
	@Nullable
	private static BufferedOutputStream outputStream;

	/**
	 * Der Eingabestream
	 */
	@Nullable
	private static BufferedInputStream inputStream;

	/**
	 * Initialisiert Bluetooth.
	 *
	 * <p/>
	 *
	 * Diese Methode muss vor allen anderen Methoden aufgerufen werden!
	 *
	 * @param applicationContext Der Anwendungskontext
	 * @return <code>true</code>, wenn der Service erfolgreich initialisiert wurde (oder bereits initialisiert war)<br/>
	 *         <code>false</code>, wenn bei der Initialisierung ein Fehler auftrat
	 */
	public static synchronized boolean initialize(@NotNull Context applicationContext, @NotNull IBluetoothServiceEventReceiver eventReceiver) {
		BluetoothService.eventReceiver = eventReceiver;

		if (initialized) return true;
		BluetoothService.applicationContext = applicationContext;

		// Lokalen Adapter besorgen
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			Toast.makeText(applicationContext, R.string.no_bluetooth_modem, Toast.LENGTH_LONG).show();;
			return false;
		}

		// Erfolg.
		initialized = true;
		return true;
	}

	/**
	 * Ermittelt, ob Bluetooth vorhanden ist.
	 *
	 * @return <code>true</code>, wenn Bluetooth prinzipiell vorhanden ist.
	 */
	public static synchronized boolean bluetoothAvailable() {
		return btAdapter != null;
	}

	/**
	 * Ermittelt, ob Bluetooth aktiviert ist
	 *
	 * @return <code>true</code>, wenn Bluetooth aktiviert ist
	 */
	public static synchronized boolean bluetoothEnabled() {
		return btAdapter != null && btAdapter.isEnabled();
	}

	/**
	 * Fordert das Aktivieren von Bluetooth an
	 *
	 * @return <code>false</code>, wenn Bluetooth schon aktiviert war
	 */
	public static synchronized boolean requestEnableBluetooth(@NotNull Activity activity) {
		if (bluetoothEnabled()) return false;

		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		activity.startActivityForResult(enableIntent, IntentRequestCodes.BT_REQUEST_ENABLE);
		return true;
	}

	/**
	 * Registriert den Broadcast-Receiver
	 * @param activity Die Activity
	 */
	public static synchronized void registerBroadcastReceiver(@NotNull final Activity activity) {
		if (broadcastReceiver == null) {
			broadcastReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {

					int currentState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
					int lastState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);

					Log.v(TAG, "Bluetooth state change received: " + lastState + " --> " + currentState);
					switch(currentState) {
						case BluetoothAdapter.STATE_TURNING_ON:
							onBluetoothEnabling();
							break;
						case BluetoothAdapter.STATE_ON:
							onBluetoothEnabled();
							break;
						case BluetoothAdapter.STATE_TURNING_OFF:
							onBluetoothDisabling();
							break;
						case BluetoothAdapter.STATE_OFF:
							onBluetoothDisabled();
							break;
					}

					if (currentState == BluetoothAdapter.STATE_ON)
						onBluetoothEnabled();
					if (currentState == BluetoothAdapter.STATE_TURNING_OFF)
						onBluetoothDisabling();
				}
			};

			broadcastIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		}
		activity.registerReceiver(broadcastReceiver, broadcastIntentFilter);
	}

	/**
	 * Deregistriert den Broadcast-Receiver
	 * @param activity Die Activity
	 */
	public static synchronized void unregisterBroadcastReceiver(@NotNull Activity activity) {
		if (broadcastReceiver == null) return;
		activity.unregisterReceiver(broadcastReceiver);
	}

	/**
	 * Wird gerufen, wenn Bluetooth aktiviert wird
	 */
	private static synchronized void onBluetoothEnabling() {
		assert eventReceiver != null;
		eventReceiverHandler.post(new Runnable() {
			@Override
			public void run() {
				eventReceiver.bluetoothEnabling();
			}
		});
	}

	/**
	 * Wird gerufen, wenn Bluetooth aktiviert wird
	 */
	private static synchronized void onBluetoothEnabled() {
		assert eventReceiver != null;
		eventReceiverHandler.post(new Runnable() {
			@Override
			public void run() {
				eventReceiver.bluetoothEnabled();
			}
		});
	}

	/**
	 * Wird gerufen, wenn Bluetooth aktiviert wurde
	 */
	private static synchronized void onBluetoothDisabling() {
		assert eventReceiver != null;
		eventReceiverHandler.post(new Runnable() {
			@Override
			public void run() {
				eventReceiver.bluetoothDisabling();
			}
		});
	}

	/**
	 * Wird gerufen, wenn Bluetooth aktiviert wurde
	 */
	private static synchronized void onBluetoothDisabled() {
		assert eventReceiver != null;
		eventReceiverHandler.post(new Runnable() {
			@Override
			public void run() {
				eventReceiver.bluetoothDisabled();
			}
		});
	}

	/**
	 * Verbindet mit dem angegeben Gerät
	 * @param macAddress Die MAC-Adresse
	 */
	public static synchronized void connectToDevice(@NotNull final String macAddress) {
		assert eventReceiver != null;

		// Alte Verbindung trennen
		disconnect();

		// Bezieht das Gerät
		BluetoothDevice device = btAdapter.getRemoteDevice(macAddress);
		Log.i(TAG, "Bluetooth-Gerät ausgewählt: " + device.getName() + "; " + device.getAddress());
		connectedDevice = device;

		// Benachrichtigung senden
		final String deviceName = device.getName();
		eventReceiverHandler.post(new Runnable() {
			@Override
			public void run() {
				eventReceiver.connectedTo(deviceName == null ? "unnamed" : deviceName, macAddress);
			}
		});
		
		// Socket aufbauen
		try {
			BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuidSpp);
			connectedSocket = socket;
			if (socket == null) {
				Log.e(TAG, "Konnte Bluetooth-Socket nicht erzeugen!"); // TODO: An UI weitergeben!
				return;
			}

			// Wenn wir discovern - abbrechen
			if (btAdapter.isDiscovering()) btAdapter.cancelDiscovery();

			// Verbinden
			try {
				Log.i(TAG, "Connecting Socket to " + device.getName()); // TODO: An UI weitergeben!
				socket.connect();
			}
			catch (IOException e) {
				Log.e(TAG, "Konnte Verbindung nicht herstellen.", e); // TODO: An UI weitergeben!
				return;
			}

			// Ausgabestream besorgen
			try {
				InputStream realInputStream = socket.getInputStream();
				if (realInputStream == null) {
					Log.e(TAG, "Konnte Input-Stream nicht erzeugen"); // TODO: An UI weitergeben!
					return;
				}
				inputStream = new BufferedInputStream(realInputStream);
			} catch (IOException e) {
				Log.e(TAG, "Konnte Input-Stream nicht erzeugen", e); // TODO: An UI weitergeben!
				return;
			}

			// Ausgabestream besorgen
			try {
				OutputStream realOutputStream = socket.getOutputStream();
				if (realOutputStream  == null) {
					Log.e(TAG, "Konnte Output-Stream nicht erzeugen"); // TODO: An UI weitergeben!
					return;
				}
				outputStream = new BufferedOutputStream(realOutputStream);
			} catch (IOException e) {
				Log.e(TAG, "Konnte Output-Stream nicht erzeugen", e); // TODO: An UI weitergeben!
				return;
			}

			// Sync senden
			sendSyncMessage();

		} catch (IOException e) {
			e.printStackTrace(); // TODO: An UI weitergeben!
		} catch (NullPointerException e) {
			Log.e(TAG, "Nullreferenz-Ausnahmefehler!", e); // TODO: An UI weitergeben!
		}
	}

	/**
	 * Trennt die Verbindung
	 */
	public static synchronized void disconnect() {

		// Ausgabestream schließen
		if (outputStream != null) try {
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
		}
		outputStream = null;

		// Eingabestream schließen
		if (inputStream != null) try {
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
		}
		inputStream = null;

		// Eingabestream schließen
		if (connectedSocket != null) try {
			connectedSocket.getOutputStream().close();
			connectedSocket.getInputStream().close();
			connectedSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
		}
		connectedSocket = null;

		// Gerät freigeben
		connectedDevice = null;
	}

	/**
	 * Sendet eine sync-Nachricht
	 */
	private static synchronized void sendSyncMessage() {
		assert outputStream != null;

		String syncMessage = "SYNC from "+ btAdapter.getName() + " " + btAdapter.getAddress() + "\r\n";
		try {
			outputStream.write(syncMessage.getBytes());
			outputStream.flush();
		} catch (IOException e) {
			Log.e(TAG, "Fehler beim Senden der Sync-Nachricht", e);
		}
	}

	/**
	 * Gibt an, ob eine Bluetooth-Verbindung besteht
	 * @return <code>true</code>, wenn die Verbindung besteht
	 */
	public static boolean isConnected() {
		return connectedSocket != null && outputStream != null;
	}

	/**
	 * Sendet eine Nachricht an den Server
	 * @param message Die zu sendende Nachricht
	 */
	public static synchronized void sendToTarget(@NotNull String message) {
		try {
			outputStream.write(message.getBytes());
			outputStream.write('\r');
			outputStream.write('\n');
			outputStream.flush();
		} catch (IOException e) {
		} catch (NullPointerException e) {
		}
	}
}
