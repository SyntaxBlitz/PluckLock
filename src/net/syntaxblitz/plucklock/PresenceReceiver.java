package net.syntaxblitz.plucklock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PresenceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent i) {
		Intent accelerometerIntent = new Intent(context,
				AccelerometerService.class);

		// ACTION_SCREEN_ON includes turning on without unlocking; USER_PRESENT
		// waits for the unlock.
		if (i.getAction().equals(Intent.ACTION_USER_PRESENT)) {
			AccelerometerService.dead = false;

		// From the docs:
		// For historical reasons, the name of this broadcast action refers
		// to the power state of the screen but it is actually sent in
		// response to changes in the overall interactive state of the
		// device.
		} else if (i.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			AccelerometerService.dead = true;
		}
	}

}
