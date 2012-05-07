package de.ifgi.fmt.activities;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.android.maps.MapView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;
 
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import de.ifgi.fmt.R;

public class MapActivity extends SherlockMapActivity {
	private static final int MENU_LAYER_MAP = 1;
	private static final int MENU_LAYER_SATELLITE = 2;

	private MapView mapView;
    MapController mc;
    GeoPoint p;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		mapView = (MapView) findViewById(R.id.mapview);
		
		mc = mapView.getController();
        String coordinates[] = {"51.962956", "7.629592"};
        double lat = Double.parseDouble(coordinates[0]);
        double lng = Double.parseDouble(coordinates[1]);
 
        p = new GeoPoint(
            (int) (lat * 1E6), 
            (int) (lng * 1E6));
 
        mc.animateTo(p);
        mc.setZoom(13); 
        mapView.invalidate();
        
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
		case MENU_LAYER_MAP:
			mapView.setSatellite(false);
			return true;
		case MENU_LAYER_SATELLITE:
			mapView.setSatellite(true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu layers = menu.addSubMenu("Layers");
		layers.add(0, MENU_LAYER_MAP, 0, "Map");
		layers.add(0, MENU_LAYER_SATELLITE, 0, "Satellite");

		MenuItem layersItem = layers.getItem();
		layersItem.setIcon(R.drawable.ic_layers);
		layersItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return super.onCreateOptionsMenu(menu);
	}
}