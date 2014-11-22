package net.syntaxblitz.plucklock;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
	
	private static double MIN_THRESHOLD = .15;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settingsactivity);

		setThemes();

		this.prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		enabledCheck = (CheckBox) findViewById(R.id.checkBox2);
		deviceAdminCheck = (CheckBox) findViewById(R.id.checkBox1);
		thresholdEdit = (EditText) findViewById(R.id.pref_threshold_edit);
		
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
		
		thresholdEdit.setText("" + prefs.getFloat(PreferenceString.THRESHOLD, 1));
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
						thresholdEdit.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
						Toast.makeText(getBaseContext(), getResources().getString(R.string.too_low), Toast.LENGTH_SHORT).show();
					} else {
						SharedPreferences.Editor editor = prefs.edit();
						thresholdEdit.setBackgroundColor(getResources().getColor(android.R.color.white));
						editor.putFloat(PreferenceString.THRESHOLD, newVal);
						editor.commit();
					}
				} catch (NumberFormatException e) {
					thresholdEdit.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
				}
			}
		});
		
		final ComponentName adminComponent = new ComponentName(this,
				AdminReceiver.class);

		if (!prefs.getBoolean(PreferenceString.DISABLED_DEVICE_ADMIN, false)) {	// user has never unchecked it, ever 
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
			startActivity(intent);
		}
		
		final DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		
		deviceAdminCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isChecked) {
				if (isChecked) {	// we're not here to be DRY like those ruby freaks
					Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
					intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
					startActivity(intent);
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
	
	private void setThemes() {
		// API < 10
		setTheme(android.R.style.Theme);
		// API > 11
		setTheme(android.R.style.Theme_Holo);
		// API > 14
		setTheme(android.R.style.Theme_DeviceDefault);
	}
}
