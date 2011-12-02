package de.widemeadows.android.bluetoothspptest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Klasse, welche die Bluetooth-Funktionalität
 * bereitstellt
 */
public final class BluetoothService {

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
	public static synchronized boolean initialize(@NotNull Context applicationContext) {
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
	 */
	public static void requestEnableBluetooth(@NotNull Activity activity) {
		if (bluetoothEnabled()) return;

		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		activity.startActivityForResult(enableIntent, IntentRequestCodes.BT_REQUEST_ENABLE);
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
					if (currentState == -1) return; // TODO: Fehler!
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
	private static void onBluetoothEnabled() {
		Toast.makeText(applicationContext, R.string.bluetooth_enabled, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Wird gerufen, wenn Bluetooth aktiviert wurde
	 */
	private static void onBluetoothDisabling() {
		Toast.makeText(applicationContext, R.string.bluetooth_not_enabled, Toast.LENGTH_SHORT).show();
	}
}
