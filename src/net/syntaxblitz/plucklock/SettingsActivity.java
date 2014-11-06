package net.syntaxblitz.plucklock;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// API < 10
		setTheme(android.R.style.Theme);
		// API > 11
		setTheme(android.R.style.Theme_Holo);
		// API > 14
		setTheme(android.R.style.Theme_DeviceDefault);

		ComponentName adminComponent = new ComponentName(this,
				AdminReceiver.class);

		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
		startActivity(intent);
		
		Intent accelerometerIntent = new Intent(getBaseContext(), AccelerometerService.class);
		getBaseContext().startService(accelerometerIntent);
		
		this.addPreferencesFromResource(R.xml.preferences);
	}

}
