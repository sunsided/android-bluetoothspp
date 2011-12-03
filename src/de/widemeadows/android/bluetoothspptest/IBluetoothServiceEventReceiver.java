package de.widemeadows.android.bluetoothspptest;

/**
 * Interface für Bluetooth-Ereignisreceiver
 */
public interface IBluetoothServiceEventReceiver {

	/**
	 * Bluetooth wird aktiviert
	 */
	public void bluetoothEnabling();

	/**
	 * Bluetooth wurde aktiviert
	 */
	public void bluetoothEnabled();

	/**
	 * Bluetooth wird deaktiviert
	 */
	public void bluetoothDisabling();

	/**
	 * Bluetooth wurde deaktiviert
	 */
	public void bluetoothDisabled();

}
