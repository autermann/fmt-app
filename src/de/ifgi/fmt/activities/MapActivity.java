package de.ifgi.fmt.activities;

//import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.android.maps.MapView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.OverlayItem;

import java.util.List;
import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MyLocationOverlay;



import de.ifgi.fmt.R;

public class MapActivity extends SherlockMapActivity {
	private static final int MENU_LAYER_MAP = 1;
	private static final int MENU_LAYER_SATELLITE = 2;

	private MapView mapView = null;
    MapController mc;
    GeoPoint p;    
    private MyLocationOverlay me=null;
	public Context mContext;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		mapView = (MapView) findViewById(R.id.mapview);
		Drawable marker=getResources().getDrawable(R.drawable.location);
		
		
		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
                marker.getIntrinsicHeight());

		mapView.getOverlays().add(new SitesOverlay(marker));

		me=new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(me);


		
		//start position when loading the map
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

	 private GeoPoint getPoint(double lat, double lon) {
		    return(new GeoPoint((int)(lat*1000000.0),
		                          (int)(lon*1000000.0)));
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
	
	
	
	
	 private class SitesOverlay extends ItemizedOverlay<OverlayItem> {
		    private List<OverlayItem> items=new ArrayList<OverlayItem>();
		    
		    
		    public SitesOverlay(Drawable marker) {
		    	super(marker);	      

		      
		      boundCenterBottom(marker);
		      
		      // List of Points (FMs) to display
		      items.add(new OverlayItem(getPoint(51.940932,
		    		  7.609992),
		                                "Freeze Flashmob", " Come here on 23.05.2012"));
		      items.add(new OverlayItem(getPoint(51.951195,
		    		  7.603297),
		                                "Freeze Flashmob", "Starting on 24.05.2012"));
		      items.add(new OverlayItem(getPoint(51.962409,
		    		  7.631621),
		                                "Pillow Flashmob", "Party @ 25.05.2012"));
		      items.add(new OverlayItem(getPoint(51.963995,
		    		  7.610507),
		                                "Pillow Flashmob", "Let's meet: 26.05.2012"));

		      populate();
		    }
		    
		    @Override
		    protected OverlayItem createItem(int i) {
		      return(items.get(i));
		    }
		    
		    // Traditional way of displaying the information with a AlertDialog
//		    @Override
//		    protected boolean onTap(int i) { 
//			OverlayItem item = items.get(i);
//		      AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
//		      dialog.setTitle(item.getTitle());
//		      dialog.setMessage(item.getSnippet());
//		      dialog.show();
//		      return true;
//		    }
		    
			//Cool Way of displaying the information of the FM 		   
		    @Override
		    protected boolean onTap(int i) {
		      Toast.makeText(MapActivity.this,
		                      items.get(i).getTitle() + "\n" + items.get(i).getSnippet(),
		                      Toast.LENGTH_LONG).show();
		      
		      return(true);
		    }
		    
		    
		    @Override
		    public int size() {
		      return(items.size());
		    }
		  }

}    

