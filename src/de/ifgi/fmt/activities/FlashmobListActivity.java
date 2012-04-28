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
import de.ifgi.fmt.classes.Flashmob;

public class FlashmobListActivity extends SherlockListActivity
{
	// Fake set of flashmobs
	private Flashmob[] flashmobs = new Flashmob[3];
	private String[] flashmobTitles = new String[flashmobs.length];
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

		createFakeFlashmobData();

		// Setting the layout and content for the list
		setListAdapter(new ArrayAdapter<String>(this, R.layout.flashmob_list_activity, flashmobTitles));
		final ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) 
			{
				// TODO: Add the flashmob data to the intent
				Intent i = new Intent(FlashmobListActivity.this, FlashmobDetailsActivity.class);
				
				Bundle bundle = new Bundle();
				bundle.putString("id", flashmobs[position].getId());
				bundle.putString("title", flashmobs[position].getTitle());
				bundle.putBoolean("isPublic", flashmobs[position].isPublic());
				bundle.putInt("participants", flashmobs[position].getParticipants());
				bundle.putString("description", flashmobs[position].getDescription());
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

	/**
	 * Create fake flashmob data for testing.
	 */
	public void createFakeFlashmobData()
	{
		// Creating pseudo-flashmobs
		Flashmob danceFlashmob = new Flashmob("001", "Dance Flashmob", true, 66,
		"People dance in public to a certain song.");
		Flashmob freezeFlashmob = new Flashmob("002", "Freeze Flashmob", true, 1001,
				"People freeze for 2 minutes.");
		Flashmob zombieFlashmob = new Flashmob("003", "Zombie Flashmob", false, 42,
				"People dressed as zombies act on command.");

		// Adding the flashmobs to the array
		flashmobs[0] = danceFlashmob;
		flashmobs[1] = freezeFlashmob;
		flashmobs[2] = zombieFlashmob;
		
		// Adding the flashmobs titles to the array for displaying on the list view
		for(int i=0; i<flashmobs.length; i++)
		{
			flashmobTitles[i] = flashmobs[i].getTitle();
		}
	}
	
}
