package de.ifgi.fmt.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;
import de.ifgi.fmt.data.Store;
import de.ifgi.fmt.objects.Activity;
import de.ifgi.fmt.objects.Flashmob;
import de.ifgi.fmt.objects.Role;
import de.ifgi.fmt.objects.Task;
import de.ifgi.fmt.objects.Trigger;
import de.ifgi.fmt.parser.ActivityJSONParser;
import de.ifgi.fmt.parser.SignalJSONParser;
import de.ifgi.fmt.parser.TaskJSONParser;
import de.ifgi.fmt.parser.TriggerJSONParser;
import de.ifgi.fmt.signal.Signal;

public class ContentActivity extends SherlockActivity {
	private static final int MENU_NAVIGATION = 1;
	Flashmob f;
	Role r;
	ArrayList<Signal> signals;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_activity);

		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

		f = ((Store) getApplicationContext()).getFlashmobById(getIntent()
				.getExtras().getString("id"));
		setTitle(f.getTitle());

		r = f.getSelectedRole();
		getSupportActionBar().setSubtitle(r.getTitle());

		String url = "http://giv-flashmob.uni-muenster.de/fmt/flashmobs/"
				+ f.getId() + "/roles/" + r.getId() + "/activities";
		new DownloadActivitiesTask().execute(url);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_NAVIGATION, 0, "Navigation")
				.setIcon(R.drawable.ic_action_navigation)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_ALWAYS
								| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, StartActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case MENU_NAVIGATION:
			Intent i = new Intent(Intent.ACTION_VIEW,
					Uri.parse("google.navigation:q="
							+ f.getLocation().getLatitudeE6() / 1E6 + ","
							+ f.getLocation().getLongitudeE6() / 1E6));
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void checkTimePreference() {
		try {
			if (signals.size() > 0
					&& android.provider.Settings.System.getInt(
							getContentResolver(),
							android.provider.Settings.System.AUTO_TIME) == 0) {
				AlertDialog.Builder alert = new AlertDialog.Builder(
						ContentActivity.this);
				alert.setTitle("Time accuracy");
				alert.setMessage("This flashmob includes time triggers. "
						+ "We would therefore recommend to use "
						+ "network-provided values for the system time. "
						+ "Do you want to enable this feauture on your device?");
				alert.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								android.provider.Settings.System
										.putInt(getContentResolver(),
												android.provider.Settings.System.AUTO_TIME,
												1);
							}
						});
				alert.setNegativeButton("No, thanks!",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						});
				alert.show();
			}

		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (signals != null) {
			for (Signal s : signals) {
				s.stopThread();
			}
		}
	}

	class DownloadActivitiesTask extends AsyncTask<String, Void, Integer> {
		ProgressDialog progress;
		ArrayList<Activity> activities;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = new ProgressDialog(ContentActivity.this);
			progress.setMessage("Loading activities...");
			progress.show();
		}

		@Override
		protected Integer doInBackground(String... url) {
			activities = new ArrayList<Activity>();
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(url[0]);
				HttpResponse response = client.execute(request);
				String result = EntityUtils.toString(response.getEntity());

				Log.i("URL", "" + url[0]);
				Log.i("Status", "" + response.getStatusLine());

				JSONObject root = new JSONObject(result);
				JSONArray activitiesArray = root.getJSONArray("activities");
				for (int i = 0; i < activitiesArray.length(); i++) {
					JSONObject activity = (JSONObject) activitiesArray
							.getJSONObject(i);
					String href = activity.getString("href");
					href = href
							.replace("/activities/activities", "/activities");
					request = new HttpGet(href);
					response = client.execute(request);
					result = EntityUtils.toString(response.getEntity());

					Log.i("URL", "" + request.getURI());
					Log.i("Status", "" + response.getStatusLine());

					Activity a = ActivityJSONParser.parse(result);

					// Task
					String taskHref = href + "/task";
					request = new HttpGet(taskHref);
					response = client.execute(request);
					Log.i("URL", "" + request.getURI());
					Log.i("Status", "" + response.getStatusLine());
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						result = EntityUtils.toString(response.getEntity());
						Task ta = TaskJSONParser.parse(result);
						a.setTask(ta);
					}

					// Trigger
					String triggerHref = href
							.replace("/roles/" + r.getId(), "") + "/trigger";
					request = new HttpGet(triggerHref);
					response = client.execute(request);
					Log.i("URL", "" + request.getURI());
					Log.i("Status", "" + response.getStatusLine());
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						result = EntityUtils.toString(response.getEntity());
						JSONObject json = new JSONObject(result);
						triggerHref = json.getString("href");
						request = new HttpGet(triggerHref);
						response = client.execute(request);
						Log.i("URL", "" + request.getURI());
						Log.i("Status", "" + response.getStatusLine());
						if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							result = EntityUtils.toString(response.getEntity());
							Trigger tr = TriggerJSONParser.parse(result);
							a.setTrigger(tr);
						}
					}

					// Signal
					String signalHref = href.replace("/roles/" + r.getId(), "")
							+ "/signal";
					request = new HttpGet(signalHref);
					response = client.execute(request);
					Log.i("URL", "" + request.getURI());
					Log.i("Status", "" + response.getStatusLine());
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						result = EntityUtils.toString(response.getEntity());
						String signal = SignalJSONParser.parse(result);
						a.setSignal(signal);
					}

					activities.add(a);
				}
				return 1;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				return 0;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return 404;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			progress.dismiss();
			if (result == 1) {
				LinearLayout layout = (LinearLayout) findViewById(R.id.layout);

				int counter = 1;
				for (Activity a : activities) {
					LayoutInflater l = getLayoutInflater();
					LinearLayout ll = (LinearLayout) l.inflate(
							R.layout.activity_item, null);

					LinearLayout.LayoutParams lp = new LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					// Converts 8 dip into its equivalent px
					float marginBottom = TypedValue.applyDimension(
							TypedValue.COMPLEX_UNIT_DIP, 16, getResources()
									.getDisplayMetrics());
					lp.setMargins(0, 0, 0, Math.round(marginBottom));

					TextView activityTitle = (TextView) ll
							.findViewById(R.id.activity_title);
					activityTitle.setText("Activity #" + counter++ + ": "
							+ a.getTitle());

					TextView activityDescription = (TextView) ll
							.findViewById(R.id.activity_description);
					activityDescription.setText(a.getDescription());

					// Task
					final Task t = a.getTask();
					LinearLayout taskRow = (LinearLayout) ll
							.findViewById(R.id.task_row);
					if (t != null) {
						TextView taskDescription = (TextView) ll
								.findViewById(R.id.task_description);
						taskDescription.setText(t.getDescription());
						taskRow.setVisibility(View.VISIBLE);
					} else {
						taskRow.setVisibility(View.GONE);
					}

					// Task media file
					ImageButton taskMediaButton = (ImageButton) ll
							.findViewById(R.id.task_media_button);
					if (t != null && t.getHref() != null) {
						taskMediaButton.setVisibility(View.VISIBLE);
						taskMediaButton
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										startActivity(new Intent(
												Intent.ACTION_VIEW, Uri.parse(t
														.getHref())));
									}
								});
					} else {
						taskMediaButton.setVisibility(View.GONE);
					}

					// Trigger
					LinearLayout triggerRow = (LinearLayout) ll
							.findViewById(R.id.trigger_row);
					if (a.getTrigger() != null) {
						Trigger trigger = a.getTrigger();
						TextView triggerText = (TextView) ll
								.findViewById(R.id.trigger);
						String text = "";
						if (trigger.getTime() != null) {
							text += trigger.getDateAsString() + " \u00B7 ";
							text += trigger.getTimeAsString();
						}
						if (trigger.getLocation() != null) {
							text += trigger.getLocation().getLatitude() + ", "
									+ trigger.getLocation().getLongitude();
						}
						if (trigger.getDescription() != null) {
							if (trigger.getTime() != null
									|| trigger.getLocation() != null) {
								text += "\n" + trigger.getDescription();
							} else {
								text += trigger.getDescription();
							}
						}
						triggerText.setText(text);
					} else {
						triggerRow.setVisibility(View.GONE);
					}

					// Signal
					LinearLayout signalRow = (LinearLayout) ll
							.findViewById(R.id.signal_row);
					if (a.getSignal() != null) {
						TextView signal = (TextView) ll
								.findViewById(R.id.signal);
						String signalText = a.getSignal().toLowerCase();
						char newChar = signalText.charAt(0);
						newChar = Character.toUpperCase(newChar);
						signalText = signalText.replaceFirst(
								String.valueOf(signalText.charAt(0)),
								String.valueOf(newChar));
						signal.setText(signalText);
						signalRow.setVisibility(View.VISIBLE);
					} else {
						signalRow.setVisibility(View.GONE);
					}

					layout.addView(ll, lp);
				}

				// Signals
				signals = new ArrayList<Signal>();
				for (Activity a : activities) {
					if (a.getSignal() == null) {
						continue;
					}
					if (a.getTrigger() != null) {
						Date time = a.getTrigger().getTime();
						// set trigger time manually for testing
						// try {
						// SimpleDateFormat df = new SimpleDateFormat(
						// "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
						// String t = "2012-07-12T16:51:00.000+02:00";
						// a.getTrigger().setTime(df.parse(t));
						// time = a.getTrigger().getTime();
						// } catch (ParseException e) {
						// e.printStackTrace();
						// }
						String signalType = a.getSignal();
						// set signal type manually for testing
						// String signalType = "Vibration";
						String message = a.getTask().getDescription();
						Signal signal = null;
						if (signalType.equals("SOUND")) {
							signal = new Signal(ContentActivity.this,
									Signal.TYPE_SOUND, time, message);
						} else if (signalType.equals("TEXT")) {
							signal = new Signal(ContentActivity.this,
									Signal.TYPE_TEXT, time, message);
						} else if (signalType.equals("VIBRATION")) {
							signal = new Signal(ContentActivity.this,
									Signal.TYPE_VIBRATION, time, message);
						}
						if (signal != null)
							signals.add(signal);
					}
					if (signals.size() > 0) {
						// keep the screen awake, don't let the user miss
						// anything
						getWindow().addFlags(
								WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
						checkTimePreference();
					}
				}
			} else if (result == 0) {
				Toast.makeText(getApplicationContext(),
						"There is a problem with the Internet connection.",
						Toast.LENGTH_LONG).show();
			}
		}
	}

}