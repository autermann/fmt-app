package de.ifgi.fmt.activities;

import java.io.IOException;
import java.util.ArrayList;

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

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;
import de.ifgi.fmt.data.Store;
import de.ifgi.fmt.objects.Activity;
import de.ifgi.fmt.objects.Flashmob;
import de.ifgi.fmt.objects.Role;
import de.ifgi.fmt.objects.SoundSignal;
import de.ifgi.fmt.objects.Task;
import de.ifgi.fmt.objects.TextSignal;
import de.ifgi.fmt.objects.Trigger;
import de.ifgi.fmt.objects.VibrationSignal;
import de.ifgi.fmt.parser.ActivityJSONParser;
import de.ifgi.fmt.parser.SignalJSONParser;
import de.ifgi.fmt.parser.TaskJSONParser;
import de.ifgi.fmt.parser.TriggerJSONParser;

public class ContentActivity extends SherlockActivity {
	Flashmob f;
	Role r;

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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, StartActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
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
							text += trigger.getTimeAsString();
						}
						if (trigger.getLocation() != null) {
							text += trigger.getLocation().getLatitude() + ", "
									+ trigger.getLocation().getLongitude();
						}
						if (trigger.getDescription() != null) {
							if (trigger.getTime() != null
									|| trigger.getLocation() != null) {
								text += " (" + trigger.getDescription() + ")";
							} else {
								text += " " + trigger.getDescription();
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
						signal.setText(a.getSignal());
						signalRow.setVisibility(View.VISIBLE);
					} else {
						signalRow.setVisibility(View.GONE);
					}

					layout.addView(ll, lp);
				}

				// Signals
				boolean activityHasSignals = false;
				for (Activity a : activities) {
					if (a.getSignal() == null) {
						continue;
					}
					activityHasSignals = true;
					String signal = a.getSignal();
					// String signal = "Sound";
					String message = a.getTask().getDescription();
					if (signal.equals("Sound")) {
						new SoundSignal(ContentActivity.this, message);
					} else if (signal.equals("Text")) {
						new TextSignal(ContentActivity.this, message);
					} else if (signal.equals("Vibration")) {
						new VibrationSignal(ContentActivity.this, message);
					}
				}
				if (activityHasSignals) {
					// keep the screen awake, don't let the user miss anything
					getWindow().addFlags(
							WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}

			} else if (result == 0) {
				Toast.makeText(getApplicationContext(),
						"There is a problem with the Internet connection.",
						Toast.LENGTH_LONG).show();
			}
			progress.dismiss();
		}
	}

}