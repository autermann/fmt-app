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
import com.google.android.maps.OverlayItem;

import java.util.List;
import com.google.android.maps.Overlay; 
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;


import de.ifgi.fmt.R;

public class MapActivity extends SherlockMapActivity {
	private static final int MENU_LAYER_MAP = 1;
	private static final int MENU_LAYER_SATELLITE = 2;

	MapView mapView;
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
        
        List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.location);
		ItemizedOverlay itemizedoverlay = new ItemizedOverlay(drawable, this);
		
		
		String coordinates_point1[] = {"51.962960", "7.629596"};
        double lat2 = Double.parseDouble(coordinates_point1[0]);
        double lng2 = Double.parseDouble(coordinates_point1[1]);
		GeoPoint point = new GeoPoint((int) (lat2 * 1E6),(int) (lng2 * 1E6));
		OverlayItem overlayitem = new OverlayItem(point, "Freeze Flashmob MS \n 23.05.2012", "test");
        
		String coordinates_point2[] = {"51.962952", "7.629588"};
		double lat3 = Double.parseDouble(coordinates_point2[0]);
		double lng3 = Double.parseDouble(coordinates_point2[1]);
		GeoPoint point2 = new GeoPoint((int) (lat3 * 1E6),(int) (lng3 * 1E6));
		OverlayItem overlayitem2 = new OverlayItem(point2, "Pillow Flashmob MS \n 24.05.2012", "Let's meet tomorrow");
		
				
		itemizedoverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedoverlay);
		itemizedoverlay.addOverlay(overlayitem2);
		mapOverlays.add(itemizedoverlay);
		
        // map-overlay: icons as location markers instead of points     
//        MapOverlay mapOverlay = new MapOverlay();
//        List<Overlay> listOfOverlays = mapView.getOverlays();
//        listOfOverlays.clear();
//        listOfOverlays.add(mapOverlay);        
// 
//        mapView.invalidate();
		
		
        //start position when loading the map
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
	
//	class MapOverlay extends com.google.android.maps.Overlay
//	{						
//	    @Override
//	    public boolean draw(Canvas canvas, MapView mapView, 
//	    boolean shadow, long when) 
//	    {
//	        super.draw(canvas, mapView, shadow);                   
//
//	        //translates the GeoPoint to screen pixels
//	        Point screenPts = new Point();
//	        mapView.getProjection().toPixels(p, screenPts);
//
//	        //add's the marker to the screen
//	        Bitmap bmp = BitmapFactory.decodeResource(
//	            getResources(), R.drawable.location);            
//	        canvas.drawBitmap(bmp, screenPts.x, screenPts.y-48, null);         
//	        return true;
//	    }
//	} 
}    

