package net.syntaxblitz.plucklock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutostartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent i) {
		Intent accelerometerIntent = new Intent(context, AccelerometerService.class);
		context.startService(accelerometerIntent);
		AccelerometerService.dead = false;
	}

}
