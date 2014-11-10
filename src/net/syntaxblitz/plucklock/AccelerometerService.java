package net.syntaxblitz.plucklock;

import android.app.KeyguardManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class AccelerometerService extends Service {

	private SensorManager sensorManager;
	private Sensor sensor;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

		sensorManager.registerListener(new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {

			}

			@Override
			public void onSensorChanged(SensorEvent event) {
				final double threshold = Double.valueOf(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("threshold_pref_key", "1"));
				double x = Math.abs(event.values[0] / 9.81);
				double y = Math.abs(event.values[1] / 9.81);
				double z = Math.abs(event.values[2] / 9.81);
				double sum = x + y + z;
				if (sum > threshold && threshold > .15) {
					KeyguardManager keyguardManager = (KeyguardManager) getBaseContext().getSystemService(Context.KEYGUARD_SERVICE);
					if (!keyguardManager.inKeyguardRestrictedInputMode()) {
						DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
						dpm.lockNow();
					}
				}
			}
		}, sensor, SensorManager.SENSOR_DELAY_NORMAL);

		return START_STICKY;
	}

}
