package de.widemeadows.android.bluetoothspptest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;

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
	public static boolean bluetoothAvailable() {
		return btAdapter != null;
	}

	/**
	 * Ermittelt, ob Bluetooth aktiviert ist
	 *
	 * @return <code>true</code>, wenn Bluetooth aktiviert ist
	 */
	public static boolean bluetoothEnabled() {
		return btAdapter != null && btAdapter.isEnabled();
	}

	/**
	 * Fordert das Aktivieren von Bluetooth an
	 *
	 * @return <code>false</code>, wenn Bluetooth schon aktiviert war
	 */
	public static boolean requestEnableBluetooth(@NotNull Activity activity) {
		if (bluetoothEnabled()) return false;

		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		activity.startActivityForResult(enableIntent, IntentRequestCodes.BT_REQUEST_ENABLE);
		return true;
	}

	/**
	 * Registriert den Broadcast-Receiver
	 * @param activity Die Activity
	 */
	public static void registerBroadcastReceiver(@NotNull final Activity activity) {
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
	public static void unregisterBroadcastReceiver(@NotNull Activity activity) {
		if (broadcastReceiver == null) return;
		activity.unregisterReceiver(broadcastReceiver);
	}

	/**
	 * Wird gerufen, wenn Bluetooth aktiviert wird
	 */
	private static void onBluetoothEnabling() {
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
	private static void onBluetoothEnabled() {
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
	private static void onBluetoothDisabling() {
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
	private static void onBluetoothDisabled() {
		assert eventReceiver != null;
		eventReceiverHandler.post(new Runnable() {
			@Override
			public void run() {
				eventReceiver.bluetoothDisabled();
			}
		});
	}
}
