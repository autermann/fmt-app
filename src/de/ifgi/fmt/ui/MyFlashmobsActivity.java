package de.ifgi.fmt.ui;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;
import de.ifgi.fmt.adapter.FlashmobListAdapter;
import de.ifgi.fmt.data.PersistentStore;
import de.ifgi.fmt.data.Store;
import de.ifgi.fmt.io.Flashmob;
import de.ifgi.fmt.io.Role;
import de.ifgi.fmt.parser.FlashmobJSONParser;
import de.ifgi.fmt.parser.RoleJSONParser;

/**
 * Activity that lists all the flashmobs, the user participates in with direct
 * access to the flashmob's content via a play button.
 * 
 * @author Matthias Robbers
 */
public class MyFlashmobsActivity extends SherlockActivity {
	public static boolean outdated;
	private ListView list;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flashmob_list_activity);
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		list = (ListView) findViewById(android.R.id.list);
		outdated = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		String userName = PersistentStore.getUserName(this);
		if (outdated) {
			list.setAdapter(null);
			if (userName != null) {
				String url = "http://giv-flashmob.uni-muenster.de/fmt/flashmobs/?participant="
						+ userName;
				new DownloadTask(this).execute(url);
			}
		}
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

	// AsyncTask instead of a Thread, in order to download the online data
	class DownloadTask extends AsyncTask<String, Void, ArrayList<Flashmob>> {
		ProgressDialog progressDialog;

		public DownloadTask(Context context) {
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Loading flashmobs...");
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		@Override
		protected ArrayList<Flashmob> doInBackground(String... url) {
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(url[0]);
				HttpResponse response = client.execute(request);
				Log.i("URL", "" + request.getURI());
				Log.i("Status", "" + response.getStatusLine());
				String result = EntityUtils.toString(response.getEntity());
				// parsing the result
				final ArrayList<Flashmob> flashmobs = FlashmobJSONParser.parse(
						result, getApplicationContext());
				// get access to the store and save the new flashmobs
				Store store = (Store) getApplicationContext();
				for (Flashmob f : flashmobs) {
					if (store.hasFlashmob(f)) {
						Log.i("Store", "Flashmob is already in the store.");
					} else {
						// get selected Role
						if (PersistentStore.isMyFlashmob(
								getApplicationContext(), f)) {
							request = new HttpGet(
									"http://giv-flashmob.uni-muenster.de/fmt/users/"
											+ PersistentStore
													.getUserName(getApplicationContext())
											+ "/flashmobs/" + f.getId()
											+ "/role");
							Cookie cookie = PersistentStore
									.getCookie(getApplicationContext());
							request.setHeader("Cookie", cookie.getName() + "="
									+ cookie.getValue());
							response = client.execute(request);
							Log.i("URL", "" + request.getURI());
							Log.i("Status", "" + response.getStatusLine());
							result = EntityUtils.toString(response.getEntity());
							Role role = RoleJSONParser.parse(result,
									getApplicationContext());
							f.setSelectedRole(role);
						}
						// add to the temporal store
						store.addFlashmob(f);
						Log.i("Store", "Flashmob added to the store.");
					}
				}
				return flashmobs;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(final ArrayList<Flashmob> flashmobs) {
			super.onPostExecute(flashmobs);
			if (flashmobs != null) {
				ListAdapter adapter = new FlashmobListAdapter(
						getApplicationContext(), flashmobs, null, false, true);
				list.setAdapter(adapter);
				list.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						Intent intent = new Intent(getApplicationContext(),
								DetailsActivity.class);
						intent.putExtra("id", flashmobs.get(arg2).getId());
						startActivity(intent);
					}
				});
				outdated = false;
			} else {
				Toast.makeText(getApplicationContext(),
						"There is a problem with the Internet connection.",
						Toast.LENGTH_LONG).show();
			}
			progressDialog.dismiss();
		}

	}
}