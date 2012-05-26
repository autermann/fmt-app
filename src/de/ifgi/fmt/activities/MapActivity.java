package de.ifgi.fmt.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.maps.OnSingleTapListener;
import com.readystatesoftware.maps.TapControlledMapView;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.location.Location;
import android.widget.Toast;



import de.ifgi.fmt.R;
import de.ifgi.fmt.data.Store;
import de.ifgi.fmt.network.NetworkRequest;
import de.ifgi.fmt.objects.Flashmob;
import de.ifgi.fmt.parser.FlashmobJSONParser;

public class MapActivity extends SherlockMapActivity
{
	private static final int MENU_LOCATION = 1;
	private static final int MENU_LAYER_MAP = 2;
	private static final int MENU_LAYER_SATELLITE = 3;

	private TapControlledMapView mapView = null;
	MapController mc;
	GeoPoint p;
	private MyLocationOverlay me = null;
	private MyLocationOverlay myLocationOverlay;
	Drawable marker;

	FlashmobsOverlay itemizedOverlay;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		mapView = (TapControlledMapView) findViewById(R.id.mapview);
		// dismiss balloon upon single tap of MapView (iOS behavior)
		mapView.setOnSingleTapListener(new OnSingleTapListener()
		{
			@Override
			public boolean onSingleTap(MotionEvent e)
			{
				itemizedOverlay.hideAllBalloons();
				return true;
			}
		});
		mapView.setBuiltInZoomControls(true);
		
		me = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(me);
		
		// create an overlay that shows our current location
		myLocationOverlay = new MyCustomLocationOverlay(this, mapView);

		// add this overlay to the MapView and refresh it
		mapView.getOverlays().add(myLocationOverlay);
		mapView.postInvalidate();
		
		// start position when loading the map
		mc = mapView.getController();
		String coordinates[] = { "51.962956", "7.629592" };
		double lat = Double.parseDouble(coordinates[0]);
		double lng = Double.parseDouble(coordinates[1]);

		p = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));

		mc.animateTo(p);
		mc.setZoom(15);
		mapView.invalidate();
            
		marker = getResources().getDrawable(R.drawable.location);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		new DownloadTask().execute("http://giv-webteam.uni-muenster.de/matthias/flashmobs");

	}
	
	 @Override
     protected void onResume() {
             super.onResume();
             // when our activity resumes, we want to register for location updates
             myLocationOverlay.enableMyLocation();
     }

     @Override
     protected void onPause() {
             super.onPause();
             // when our activity pauses, we want to remove listening for location updates
             myLocationOverlay.disableMyLocation();
     }
     
     /**
      * This method zooms to the user's location with a zoom level of 10.
      */
     private void zoomToMyLocation() {
             GeoPoint myLocationGeoPoint = myLocationOverlay.getMyLocation();
             if(myLocationGeoPoint != null) {
                     mapView.getController().animateTo(myLocationGeoPoint);
             }
             else {
                     Toast.makeText(this, "Cannot determine location", Toast.LENGTH_SHORT).show();
             }
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
		case MENU_LAYER_MAP:
			mapView.setSatellite(false);
			return true;
		case MENU_LAYER_SATELLITE:
			mapView.setSatellite(true);
			return true;
		case MENU_LOCATION:
			zoomToMyLocation();
			return true;
			
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private GeoPoint getPoint(double lat, double lon)
	{
		return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
	}

	private class FlashmobsOverlay extends BalloonItemizedOverlay<OverlayItem>
	{
		private ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
		private ArrayList<Flashmob> flashmobs = new ArrayList<Flashmob>();

		public FlashmobsOverlay(Drawable defaultMarker, MapView mapView)
		{
			super(boundCenter(defaultMarker), mapView);
		}

		public void addOverlay(OverlayItem overlay, Flashmob flashmob)
		{
			items.add(overlay);
			flashmobs.add(flashmob);
			populate();
		}

		@Override
		protected OverlayItem createItem(int i)
		{
			return (items.get(i));
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow)
		{
			super.draw(canvas, mapView, shadow);

			boundCenterBottom(marker);
		}

		@Override
		protected boolean onBalloonTap(int index, OverlayItem item)
		{
			Intent intent = new Intent(getApplicationContext(), FlashmobDetailsActivity.class);
			intent.putExtra("id", flashmobs.get(index).getId());
			startActivity(intent);
			return true;
		}

		@Override
		public int size()
		{
			return (items.size());
		}

	}
	
	
	public class MyCustomLocationOverlay extends MyLocationOverlay {
	    private Context mContext;
	    private float   mOrientation;

	    public MyCustomLocationOverlay(Context context, MapView mapView) {
	        super(context, mapView);
	        mContext = context;
	    }

	    @Override 
	    protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation, long when) {
	        // translate the GeoPoint to screen pixels
	        Point screenPts = mapView.getProjection().toPixels(myLocation, null);

	        // create a rotated copy of the marker
	        Bitmap arrowBitmap = BitmapFactory.decodeResource( mContext.getResources(), R.drawable.arrow_red);
	        Matrix matrix = new Matrix();
	        matrix.postRotate(mOrientation);

	        Bitmap rotatedBmp = Bitmap.createBitmap(
	            arrowBitmap, 
	            0, 0, 
	            arrowBitmap.getWidth(), 
	            arrowBitmap.getHeight(), 
	            matrix, 
	            true
	        );
	        // add the rotated marker to the canvas
	        canvas.drawBitmap(
	            rotatedBmp, 
	            screenPts.x - (rotatedBmp.getWidth()  / 2), 
	            screenPts.y - (rotatedBmp.getHeight() / 2), 
	            null
	        );
	    }

	    public void setOrientation(float newOrientation) {
	         mOrientation = newOrientation;
	    }
	}
	
	class DownloadTask extends AsyncTask<String, Void, String>
	{

		@Override
		protected String doInBackground(String... url)
		{
			NetworkRequest request = new NetworkRequest(url[0]);
			request.send();
			return request.getResult();
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			// parsing the result
			final ArrayList<Flashmob> flashmobs = FlashmobJSONParser.parse(result,
					getApplicationContext());
			// get access to the store and save the new flashmobs
			((Store) getApplicationContext()).setFlashmobs(flashmobs);

			// create new overlay
			itemizedOverlay = new FlashmobsOverlay(marker, mapView);
			itemizedOverlay.setShowClose(false);
			itemizedOverlay.setShowDisclosure(true);
			// List of Points (FMs) to display
			for (Flashmob f : flashmobs)
			{
				OverlayItem o = new OverlayItem(getPoint(f.getLocation().getLatitudeE6() / 1e6, f
						.getLocation().getLongitudeE6() / 1e6), f.getTitle(), f.getStreetAddress()
						+ " \u00B7 " + f.getStartDate());
				itemizedOverlay.addOverlay(o, f);
			}
			mapView.getOverlays().add(itemizedOverlay);
			mapView.invalidate();
		}

	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

	// avoid resuming activity on switch from portrait to landscape, etc.
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		SubMenu layers = menu.addSubMenu("Layers");
		layers.add(0, MENU_LAYER_MAP, 0, "Map");
		layers.add(0, MENU_LAYER_SATELLITE, 0, "Satellite");

		MenuItem layersItem = layers.getItem();
		layersItem.setIcon(R.drawable.ic_action_layers);
		layersItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menu.add(0, MENU_LOCATION, 0, "Location")
		.setIcon(R.drawable.ic_action_location)
		.setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return super.onCreateOptionsMenu(menu);
	}
}
