package net.syntaxblitz.plucklock;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends Activity {

	private SharedPreferences prefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settingsactivity);

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

		Intent accelerometerIntent = new Intent(getBaseContext(),
				AccelerometerService.class);
		getBaseContext().startService(accelerometerIntent);
		
		this.prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		final EditText thresholdEdit = (EditText) this
				.findViewById(R.id.pref_threshold_edit);
		thresholdEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				try {
					float newVal = Float.valueOf(s.toString());
					if (newVal < .15) { 
						thresholdEdit.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
						Toast.makeText(getBaseContext(), getResources().getString(R.string.too_low), 2).show();
					} else {
						thresholdEdit.setBackgroundColor(getResources().getColor(android.R.color.white));
						SharedPreferences.Editor editor = prefs.edit();
						editor.putFloat("threshold_pref_key", newVal);
						editor.commit();
					}
				} catch (NumberFormatException e) {
					thresholdEdit.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
				}
			}
		});
	}
}
