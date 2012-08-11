package de.ifgi.fmt.signal;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

/**
 * This class activates a signal of the type text, vibration + text or sound +
 * text. It executes at a certain time, unless it was cancelled before.
 * 
 * @author Matthias Robbers
 */
public class Signal {
	public static final int TYPE_VIBRATION = 1;
	public static final int TYPE_SOUND = 2;
	public static final int TYPE_TEXT = 3;

	private volatile boolean stop = false;

	Context ctx;
	String message;
	Date time;
	SignalThread thread;
	int type;
	long action;
	long now;

	public Signal(final Context ctx, final int type, final Date time,
			String message) {
		this.ctx = ctx;
		this.message = message;
		this.time = time;
		this.type = type;

		action = time.getTime();
		now = System.currentTimeMillis();
		long left = action - now;
		if (now < action && left < 240 * 60 * 1000) { // 240 * 60 *1000 = 4 hrs.
			DateFormat dateFormat = new SimpleDateFormat("hh:mm aa", Locale.US);
			switch (type) {
			case TYPE_VIBRATION:
				Log.i("Signal", "Vibration signal will be triggered at "
						+ dateFormat.format(time));
				break;
			case TYPE_SOUND:
				Log.i("Signal", "Sound signal will be triggered at "
						+ dateFormat.format(time));
				break;
			case TYPE_TEXT:
				Log.i("Signal", "Text signal will be triggered at "
						+ dateFormat.format(time));
				break;
			}
			startThread();
		}
	}

	public synchronized void startThread() {
		if (thread == null) {
			thread = new SignalThread();
			thread.start();
		}
	}

	public synchronized void stopThread() {
		if (thread != null) {
			thread.requestStop();
			thread = null;
		}
	}

	class SignalThread extends Thread {
		@Override
		public void run() {
			long action = time.getTime();
			long now = System.currentTimeMillis();
			while (!stop) {
				now = System.currentTimeMillis();
				// Log.i("Time", "" + now);
				if (now < action) {

				} else {
					handler.sendEmptyMessage(0);
					stop = true;
				}
			}
			if (stop)
				Log.i("Signal", "Thread stopped.");
			super.run();
		}

		public synchronized void requestStop() {
			stop = true;
		}

		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (type) {
				case TYPE_VIBRATION:
					vibration();
					text();
					break;
				case TYPE_SOUND:
					sound();
					text();
					break;
				case TYPE_TEXT:
					text();
					break;
				}
			}
		};
	};

	private void vibration() {
		Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(1000);
	}

	private void sound() {
		try {
			MediaPlayer mp = new MediaPlayer();
			AssetFileDescriptor descriptor;
			descriptor = ctx.getAssets().openFd("signal.mp3");
			mp.setDataSource(descriptor.getFileDescriptor(),
					descriptor.getStartOffset(), descriptor.getLength());
			descriptor.close();
			mp.prepare();
			mp.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void text() {
		AlertDialog.Builder alert = new AlertDialog.Builder(ctx);

		alert.setTitle("Go!");
		alert.setMessage(message);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});

		alert.show();
	}
}