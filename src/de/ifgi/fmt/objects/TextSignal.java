package de.ifgi.fmt.objects;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class TextSignal {

	public TextSignal(final Context ctx, String message) {
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