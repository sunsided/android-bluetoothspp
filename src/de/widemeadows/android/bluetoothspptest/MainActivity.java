package de.widemeadows.android.bluetoothspptest;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.TextView;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class MainActivity extends Activity implements SensorEventListener
{
	/**
	 * Ausgabeformat für Dezimalzahlen
	 */
	@NotNull
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
	 * {@link TextView} für Genauigkeot
	 */
	@NotNull
	private TextView textViewAccuracy;

	/**
	 * Der {@link PowerManager.WakeLock}, der das Handy wach hält
	 */
	@NotNull
	private PowerManager.WakeLock wakeLock;

	/**
	 * Die MAC-Adresse der Zielhardware
	 */
	@NotNull
	private static final String targetMac = "00:16:38:3A:3B:A8";

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
	    textViewAccuracy = (TextView) findViewById(R.id.textViewAccuracy);

	    // Sensoren beziehen
	    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

	    // Wake lock beziehen
	    final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	    wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "do_not_turn_off");
    }

	@Override
	protected void onResume() {
		super.onResume();
		wakeLock.acquire();
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		wakeLock.release();
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		// http://developer.android.com/reference/android/hardware/SensorEvent.html#values

		final float x = sensorEvent.values[0];
		final float y = sensorEvent.values[1];
		final float z = sensorEvent.values[2];

		textViewX.setText(df.format(x));
		textViewY.setText(df.format(y));
		textViewZ.setText(df.format(z));
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		try {
			String accuracyString = getResources().getStringArray(R.array.sensor_accuracy)[accuracy];
			textViewAccuracy.setText(accuracyString);
		}
		catch(Exception e) {
			textViewAccuracy.setText(Integer.toString(accuracy));
		}
	}
}
