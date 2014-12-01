package net.syntaxblitz.plucklock;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends Activity {

	private SharedPreferences prefs;
	
	private CheckBox enabledCheck;
	private CheckBox deviceAdminCheck;
	private EditText thresholdEdit;
	
	public static float MIN_THRESHOLD = 1.5f;
	public static float DEFAULT_THRESHOLD = 10f;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);

		setThemes();

		this.prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		processOldPrefs();
		
		enabledCheck = (CheckBox) findViewById(R.id.enabled);
		deviceAdminCheck = (CheckBox) findViewById(R.id.enable_device_admin);
		thresholdEdit = (EditText) findViewById(R.id.threshold_edit);
		
		enabledCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean checked) {
				Intent accelerometerIntent = new Intent(getBaseContext(),
						AccelerometerService.class);
				if (checked) {
					AccelerometerService.dead = false;
					getBaseContext().startService(accelerometerIntent);
				} else {
					AccelerometerService.dead = true;
					getBaseContext().stopService(accelerometerIntent);
				}
				
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean(PreferenceString.ENABLED, checked).commit();
			}
		});
		
		enabledCheck.setChecked(this.prefs.getBoolean(PreferenceString.ENABLED, true));	// this triggers the listener, which will start the Service.
		
		float currentThreshold = prefs.getFloat(PreferenceString.THRESHOLD, DEFAULT_THRESHOLD);
		if (currentThreshold < MIN_THRESHOLD) {
			currentThreshold = (float) MIN_THRESHOLD;
			prefs.edit().putFloat(PreferenceString.THRESHOLD, currentThreshold);
		}
		thresholdEdit.setText("" + currentThreshold);
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
					if (newVal < MIN_THRESHOLD) { 
						thresholdEdit.setBackgroundColor(getResources().getColor(R.color.red));
						Toast.makeText(getBaseContext(), getResources().getString(R.string.too_low), Toast.LENGTH_SHORT).show();
					} else {
						SharedPreferences.Editor editor = prefs.edit();
						thresholdEdit.setBackgroundColor(getResources().getColor(R.color.white));
						editor.putFloat(PreferenceString.THRESHOLD, newVal);
						editor.commit();
					}
				} catch (NumberFormatException e) {
					thresholdEdit.setBackgroundColor(getResources().getColor(R.color.red));
				}
			}
		});
		
		final ComponentName adminComponent = new ComponentName(this,
				AdminReceiver.class);

		if (!prefs.getBoolean(PreferenceString.DISABLED_DEVICE_ADMIN, false)) {	// user has never unchecked it, ever 
			requestDeviceAdmin(adminComponent);
		}
		
		final DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		
		deviceAdminCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isChecked) {
				if (isChecked) {
					requestDeviceAdmin(adminComponent);
					enabledCheck.setEnabled(true);
				} else {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putBoolean(PreferenceString.DISABLED_DEVICE_ADMIN, true).commit();
					dpm.removeActiveAdmin(adminComponent);
					enabledCheck.setChecked(false);
					enabledCheck.setEnabled(false);
				}
			}
		});
		
		deviceAdminCheck.setChecked(dpm.isAdminActive(adminComponent));
	}
	
	private void processOldPrefs() {
		// Get the last version that the app was running. 5 is the last version that didn't keep track, so we set that as our version number.
		int prefsVersion = prefs.getInt(PreferenceString.PREFS_VERSION, 5);
		
		if (prefsVersion < 6) {
			// In version 6, we stopped measuring in terms of g and started using straight metres/second^2.
			// Let's bump up the threshold by a factor of 10. This isn't exactly g, but it will provide more friendly values.
			prefs.edit().putFloat(PreferenceString.THRESHOLD, prefs.getFloat(PreferenceString.THRESHOLD, DEFAULT_THRESHOLD) * 10).commit();
		}
		
		try {
			// update the pref version so that we know that everything is good.
			prefs.edit().putInt(PreferenceString.PREFS_VERSION, getPackageManager().getPackageInfo(getPackageName(), 0).versionCode).commit();
		} catch (NameNotFoundException e) {}	// if this ever happens I will eat many hats. 
	}

	private void setThemes() {
		// API < 10
		setTheme(android.R.style.Theme);
		// API > 11
		setTheme(android.R.style.Theme_Holo);
		// API > 14
		setTheme(android.R.style.Theme_DeviceDefault);
	}
	
	private void requestDeviceAdmin(ComponentName adminComponent) {
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
		startActivity(intent);
	}
}
