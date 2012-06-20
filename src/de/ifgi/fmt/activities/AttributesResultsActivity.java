package de.ifgi.fmt.activities;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;
import de.ifgi.fmt.adapter.FlashmobListAdapter;
import de.ifgi.fmt.data.Store;
import de.ifgi.fmt.network.NetworkRequest;
import de.ifgi.fmt.objects.Flashmob;
import de.ifgi.fmt.parser.FlashmobJSONParser;

public class AttributesResultsActivity extends SherlockActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flashmob_list_activity);
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		Bundle extras = getIntent().getExtras();
		new DownloadTask(this).execute(extras.getString("URL"));
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
	class DownloadTask extends AsyncTask<String, Void, String> {
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
		protected String doInBackground(String... url) {
			NetworkRequest request = new NetworkRequest(url[0]);
			request.send();
			return request.getResult();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			// parsing the result
			final ArrayList<Flashmob> flashmobs = FlashmobJSONParser.parse(
					result, getApplicationContext());
			// get access to the store and save the new flashmobs
			((Store) getApplicationContext()).setFlashmobs(flashmobs);

			ListAdapter adapter = new FlashmobListAdapter(
					getApplicationContext(), flashmobs, null);
			ListView list = (ListView) findViewById(android.R.id.list);
			list.setAdapter(adapter);
			list.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					Intent intent = new Intent(getApplicationContext(),
							FlashmobDetailsActivity.class);
					intent.putExtra("id", flashmobs.get(arg2).getId());
					startActivity(intent);
				}
			});

			progressDialog.dismiss();
		}

	}
}