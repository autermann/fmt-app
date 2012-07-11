package de.ifgi.fmt.objects;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.Toast;
import de.ifgi.fmt.activities.FlashmobDetailsActivity;

public class PasswordDialog {

	public PasswordDialog(final Context ctx, final Flashmob f) {
		AlertDialog.Builder alert = new AlertDialog.Builder(ctx);

		alert.setTitle("Private flashmob");
		alert.setMessage("Please enter the flashmob's password.");

		// Set an EditText view to get user input
		final EditText input = new EditText(ctx);
		input.setTransformationMethod(PasswordTransformationMethod
				.getInstance());
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				if (value.equals(f.getKey())) {
					Intent intent = new Intent(ctx,
							FlashmobDetailsActivity.class);
					intent.putExtra("id", f.getId());
					ctx.startActivity(intent);
				} else {
					Toast.makeText(ctx, "The password is not correct.",
							Toast.LENGTH_LONG).show();
				}
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});

		alert.show();
	}
}