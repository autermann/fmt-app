package de.ifgi.fmt.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import de.ifgi.fmt.R;

/**
 * About Dialog that show app version, copyright and open source licences
 * 
 * @author Matthias Robbers
 */
public class AboutDialog extends Dialog {
	public AboutDialog(final Context context) {
		super(context);
		// app version
		PackageManager pm = context.getPackageManager();
		String packageName = context.getPackageName();
		String versionName = null;
		try {
			PackageInfo info = pm.getPackageInfo(packageName, 0);
			versionName = info.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		// Build the about body view and append the link to see OSS licenses
		SpannableStringBuilder aboutBody = new SpannableStringBuilder();
		aboutBody.append(Html.fromHtml(context.getString(R.string.about_body,
				versionName)));

		SpannableString licensesLink = new SpannableString(
				"Open Source Licenses");
		licensesLink.setSpan(new ClickableSpan() {
			@Override
			public void onClick(View view) {
				new OpenSourceLicensesDialog(context);
			}
		}, 0, licensesLink.length(), 0);
		aboutBody.append("\n\n");
		aboutBody.append(licensesLink);

		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TextView aboutBodyView = (TextView) layoutInflater.inflate(
				R.layout.about_dialog, null);
		aboutBodyView.setText(aboutBody);
		aboutBodyView.setMovementMethod(new LinkMovementMethod());

		new AlertDialog.Builder(context).setTitle("About")
				.setIcon(R.drawable.ic_launcher).setView(aboutBodyView)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				}).create().show();
	}

	class OpenSourceLicensesDialog extends Dialog {

		public OpenSourceLicensesDialog(Context context) {
			super(context);
			WebView webView = new WebView(context);
			webView.loadUrl("file:///android_asset/licenses.html");

			new AlertDialog.Builder(context)
					.setTitle("Open Source Licenses")
					.setView(webView)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dialog.dismiss();
								}
							}).create().show();
		}
	}
}
