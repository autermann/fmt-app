package de.ifgi.fmt.activities;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;
import de.ifgi.fmt.data.Store;
import de.ifgi.fmt.objects.Activity;
import de.ifgi.fmt.objects.Flashmob;
import de.ifgi.fmt.objects.Role;
import de.ifgi.fmt.parser.ActivityJSONParser;

public class ContentActivity extends SherlockActivity {
	Flashmob f;
	Role r;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_activity);

		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

		Log.i("wichtig",
				"F (Intent): " + getIntent().getExtras().getString("id"));

		Flashmob f = ((Store) getApplicationContext())
				.getFlashmobById(getIntent().getExtras().getString("id"));
		Role r = new Role();
		r.setId("4fe8de12e4b002f266de750e");

		setTitle(f.getTitle());

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
				HttpGet get = new HttpGet(url[0]);
				HttpResponse response = client.execute(get);
				String result = EntityUtils.toString(response.getEntity());

				Log.i("wichtig", "URL: " + url[0]);
				Log.i("wichtig", "Status: " + response.getStatusLine());

				JSONObject root = new JSONObject(result);
				JSONArray activitiesArray = root.getJSONArray("activities");
				for (int i = 0; i < activitiesArray.length(); i++) {
					JSONObject activity = (JSONObject) activitiesArray
							.getJSONObject(i);
					String href = activity.getString("href");
					href = href
							.replace("/activities/activities", "/activities");
					Log.i("wichtig", "href: " + href);

					get = new HttpGet(href);
					response = client.execute(get);
					result = EntityUtils.toString(response.getEntity());

					Log.i("wichtig", "URL: " + url[0]);
					Log.i("wichtig", "Status: " + response.getStatusLine());

					Activity a = ActivityJSONParser.parse(result,
							getApplicationContext());
					activities.add(a);
				}
				return 1;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result == 1) {
				for (Activity a : activities) {
					Log.i("wichtig", "Title: " + a.getTitle());
					Log.i("wichtig", "Description: " + a.getDescription());
				}
			} else {
				Toast.makeText(getApplicationContext(),
						"There is a problem with the Internet connection.",
						Toast.LENGTH_LONG).show();
			}
			progress.dismiss();
		}
	}

}