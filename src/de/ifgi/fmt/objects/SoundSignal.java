package de.ifgi.fmt.objects;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

public class SoundSignal {

	public SoundSignal(final Context ctx, String message) {
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

		// addtional text signal
		new TextSignal(ctx, message);
	}
}