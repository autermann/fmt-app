package de.ifgi.fmt.objects;

import android.content.Context;
import android.os.Vibrator;

public class VibrationSignal {

	public VibrationSignal(final Context ctx, String message) {
		// Get instance of Vibrator from current Context
		Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);

		// Vibrate for 300 milliseconds
		v.vibrate(1000);

		// addtional text signal
		new TextSignal(ctx, message);
	}
}