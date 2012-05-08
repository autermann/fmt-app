package de.ifgi.fmt.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;
import de.ifgi.fmt.objects.Flashmob;
import de.ifgi.fmt.objects.Point;
import de.ifgi.fmt.objects.TestFlashmobContainer;

public class FlashmobListActivity extends SherlockListActivity
{
	// Fake set of flashmobs
	private TestFlashmobContainer tfmc = new TestFlashmobContainer();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

		// Setting the layout and content for the list
		setListAdapter(new ArrayAdapter<String>(this, R.layout.flashmob_list_activity,
				tfmc.flashmobTitles));
		final ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Intent i = new Intent(FlashmobListActivity.this, FlashmobDetailsActivity.class);

				Bundle bundle = new Bundle();
				
				bundle.putString("id", tfmc.flashmobs[position].getId());
				bundle.putString("title", tfmc.flashmobs[position].getTitle());
				bundle.putDouble("latitude", tfmc.flashmobs[position].getLocation().getLatitude());
				bundle.putDouble("longitude", tfmc.flashmobs[position].getLocation().getLongitude());
				bundle.putBoolean("isPublic", tfmc.flashmobs[position].isPublic());
				bundle.putInt("participants", tfmc.flashmobs[position].getParticipants());
				bundle.putString("description", tfmc.flashmobs[position].getDescription());
				
				i.putExtras(bundle);
				
				startActivity(i);
			}
		});
	}

	public void startFlashmobDetailsActivity(View v)
	{
		startActivity(new Intent(this, FlashmobDetailsActivity.class));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
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
}
