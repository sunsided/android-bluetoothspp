package de.widemeadows.android.bluetoothspptest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class MainActivity extends Activity implements SensorEventListener, IBluetoothServiceEventReceiver
{
	/**
	 * Ausgabeformat für Dezimalzahlen
	 */
	@NotNull
	// TODO: private static final DecimalFormat df = (DecimalFormat)DecimalFormat.getNumberInstance(Locale.US);
	private static final DecimalFormat df = new DecimalFormat(",##0.00000");

	/**
	 * Der Sensor-Manager
	 */
	@NotNull
	private SensorManager sensorManager;

	/**
	 * Der Accelerometer
	 */
	@NotNull
	private Sensor accelerometer;

	/**
	 * Der Orientierungssensor
	 */
	@NotNull
	private Sensor orientation;

	/**
	 * {@link TextView} für X-Beschleunigung
	 */
	@NotNull
	private TextView textViewX;

	/**
	 * {@link TextView} für Y-Beschleunigung
	 */
	@NotNull
	private TextView textViewY;

	/**
	 * {@link TextView} für Z-Beschleunigung
	 */
	@NotNull
	private TextView textViewZ;

	/**
	 * {@link TextView} für X-Orientierung
	 */
	@NotNull
	private TextView textViewXOr;

	/**
	 * {@link TextView} für Y-Orientierung
	 */
	@NotNull
	private TextView textViewYOr;

	/**
	 * {@link TextView} für Z-Orientierung
	 */
	@NotNull
	private TextView textViewZOr;

	/**
	 * {@link TextView} für Genauigkeot
	 */
	@NotNull
	private TextView textViewAccuracy;
	
	/**
	 * {@link TextView} für Genauigkeit
	 */
	@NotNull
	private TextView textViewAccuracyOr;

	/**
	 * Der {@link PowerManager.WakeLock}, der das Handy wach hält
	 */
	@NotNull
	private PowerManager.WakeLock wakeLock;

	/**
	 * Letzte Beschleunigung in X-Richtung
	 */
	float lastXAcceleration = 0;

	/**
	 * Letzte Beschleunigung in Y-Richtung
	 */
	float lastYAcceleration = 0;

	/**
	 * Letzte Beschleunigung in Z-Richtung
	 */
	float lastZAcceleration = 0;

	/**
	 * Letzte Orientierung in X-Richtung
	 */
	float lastXOrientation = 0;

	/**
	 * Letzte Orientierung in Y-Richtung
	 */
	float lastYOrientation = 0;

	/**
	 * Letzte Orientierung in Z-Richtung
	 */
	float lastZOrientation = 0;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

	    // UI-Elemente beziehen
	    setContentView(R.layout.main);
	    textViewX = (TextView) findViewById(R.id.textViewX);
	    textViewY = (TextView) findViewById(R.id.textViewY);
	    textViewZ = (TextView) findViewById(R.id.textViewZ);
	    textViewXOr = (TextView) findViewById(R.id.textViewXOr);
	    textViewYOr = (TextView) findViewById(R.id.textViewYOr);
	    textViewZOr = (TextView) findViewById(R.id.textViewZOr);
	    textViewAccuracy = (TextView) findViewById(R.id.textViewAccuracy);
	    textViewAccuracyOr = (TextView) findViewById(R.id.textViewAccuracyOr);

	    // Sensoren beziehen
	    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

	    // Wake lock beziehen
	    final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	    wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "do_not_turn_off");

	    // Bluetooth initialisieren
	    BluetoothService.initialize(getApplicationContext(), this);
    }

	@Override
	protected void onStart() {
		super.onStart();
		if (!BluetoothService.requestEnableBluetooth(this)) {
			bluetoothEnabled();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		wakeLock.acquire();
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_GAME);
		BluetoothService.registerBroadcastReceiver(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		wakeLock.release();
		sensorManager.unregisterListener(this);
		BluetoothService.unregisterBroadcastReceiver(this);
		BluetoothService.disconnect();
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		// http://developer.android.com/reference/android/hardware/SensorEvent.html#values

		if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
		
			final float x = sensorEvent.values[0];
			final float y = sensorEvent.values[1];
			final float z = sensorEvent.values[2];

			if (x == lastXAcceleration || y == lastYAcceleration || z == lastZAcceleration) {
				return;
			}

			// Werte setzen
			lastXAcceleration = x;
			lastYAcceleration = y;
			lastZAcceleration = z;

			// Text anzeigen
			textViewX.setText(df.format(x));
			textViewY.setText(df.format(y));
			textViewZ.setText(df.format(z));

		}
		else if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {

			final float x = sensorEvent.values[0];
			final float y = sensorEvent.values[1];
			final float z = sensorEvent.values[2];

			if (x == lastXAcceleration || y == lastYAcceleration || z == lastZAcceleration) {
				return;
			}

			// Werte setzen
			lastXOrientation = x;
			lastYOrientation = y;
			lastZOrientation = z;

			// Text anzeigen
			textViewXOr.setText(df.format(x));
			textViewYOr.setText(df.format(y));
			textViewZOr.setText(df.format(z));

		}

		// an Ziel senden
		if (BluetoothService.isConnected()) {
			BluetoothService.sendToTarget(df.format(lastXAcceleration) + "; " + df.format(lastYAcceleration) + "; " + df.format(lastZAcceleration) + "; " +
										  df.format(lastXOrientation) + "; " + df.format(lastYOrientation) + "; " + df.format(lastZOrientation));
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		try {
			String accuracyString = getResources().getStringArray(R.array.sensor_accuracy)[accuracy];
			if (sensor.getType() == Sensor.TYPE_ACCELEROMETER)
				textViewAccuracy.setText(accuracyString);
			else if (sensor.getType() == Sensor.TYPE_ORIENTATION)
				textViewAccuracyOr.setText(accuracyString);
		}
		catch(Exception e) {
			textViewAccuracy.setText(Integer.toString(accuracy));
		}
	}

	/**
	 * Bluetooth wird aktiviert
	 */
	@Override
	public void bluetoothEnabling() {
		// Text setzen
		((TextView) findViewById(R.id.textViewState)).setText(R.string.value_enabling);
	}

	/**
	 * Bluetooth wurde aktiviert
	 */
	@Override
	public void bluetoothEnabled() {
		Toast.makeText(this, R.string.bluetooth_enabled, Toast.LENGTH_SHORT).show();

		// Text setzen
		((TextView) findViewById(R.id.textViewState)).setText(R.string.value_enabled);

		// Gerät suchen
		startSearchDeviceIntent();
	}

	/**
	 * Sucht nach einem Bluetooth-Gerät zum Verbinden
	 */
	private void startSearchDeviceIntent() {
		Intent serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, IntentRequestCodes.BT_SELECT_DEVICE);
	}

	/**
	 * Bluetooth wird deaktiviert
	 */
	@Override
	public void bluetoothDisabling() {
		// Text setzen
		((TextView) findViewById(R.id.textViewState)).setText(R.string.value_disabling);
	}

	/**
	 * Bluetooth wurde deaktiviert
	 */
	@Override
	public void bluetoothDisabled() {
		Toast.makeText(this, R.string.bluetooth_not_enabled, Toast.LENGTH_SHORT).show();

		// Text setzen
		((TextView) findViewById(R.id.textViewState)).setText(R.string.value_disabled);
		((TextView) findViewById(R.id.textViewTarget)).setText(R.string.value_na);
	}

	/**
	 * Bluetooth verbunden mit einem Gerät
	 *
	 * @param name    Der Name des Gerätes
	 * @param address Die MAC-Adresse des Gerätes
	 */
	@Override
	public void connectedTo(@NotNull String name, @NotNull String address) {
		((TextView)findViewById(R.id.textViewTarget)).setText(name + " (" + address + ")");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case IntentRequestCodes.BT_REQUEST_ENABLE: {
				if (BluetoothService.bluetoothEnabled()) {
					bluetoothEnabled();
				}
				break;
			}

			case IntentRequestCodes.BT_SELECT_DEVICE: {
				if (resultCode == Activity.RESULT_OK) {
					// Get the device MAC address
					String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

					// Und verbinden
					BluetoothService.connectToDevice(address);
				}
			}

			default: {
				super.onActivityResult(requestCode, resultCode, data);
				break;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (BluetoothService.bluetoothAvailable()) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.option_menu, menu);
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.scan:
				
				if (!BluetoothService.bluetoothEnabled()) {
					BluetoothService.requestEnableBluetooth(this);
					return true;
				}
				
				// Gerät suchen
				startSearchDeviceIntent();
				return true;
		}
		return false;
	}
}
