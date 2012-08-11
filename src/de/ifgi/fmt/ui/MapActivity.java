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
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

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

import de.ifgi.fmt.R;
import de.ifgi.fmt.data.PersistentStore;
import de.ifgi.fmt.data.Store;
import de.ifgi.fmt.io.Flashmob;
import de.ifgi.fmt.io.Role;
import de.ifgi.fmt.parser.FlashmobJSONParser;
import de.ifgi.fmt.parser.RoleJSONParser;

/**
 * The map.
 * 
 * @author Stefan Arndt
 */
public class MapActivity extends SherlockMapActivity {
	private static final int MENU_LOCATION = 1;
	private static final int MENU_LAYER_MAP = 2;
	private static final int MENU_LAYER_SATELLITE = 3;

	private TapControlledMapView mapView = null;
	private MapController mc;
	private GeoPoint p, q;
	private MyLocationOverlay me = null;
	private MyLocationOverlay myLocationOverlay;
	private Drawable marker;
	private FlashmobsOverlay itemizedOverlay;
	private int zoomLevel = 15;
	private LocationManager locationManager;
	private LocationListener locationListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);

		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.
				p = new GeoPoint((int) (location.getLatitude() * 1E6),
						(int) (location.getLongitude() * 1E6));
				mc.setCenter(p);
			}

			@Override
			public void onProviderDisabled(String provider) {

			}

			@Override
			public void onProviderEnabled(String provider) {

			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {

			}
		};

		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		mapView = (TapControlledMapView) findViewById(R.id.mapview);
		// dismiss balloon upon single tap of MapView (iOS behavior)
		mapView.setOnSingleTapListener(new OnSingleTapListener() {
			@Override
			public boolean onSingleTap(MotionEvent e) {
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

		double coordinates[] = { 51.962956, 7.629592 };
		double lat = coordinates[0];
		double lng = coordinates[1];
		q = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
		mc.setCenter(q);
		// zoomToMyLocation();

		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

		mapView.invalidate();

		marker = getResources().getDrawable(R.drawable.marker_blue);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
				marker.getIntrinsicHeight());

		String url = "http://giv-flashmob.uni-muenster.de/fmt/flashmobs";
		// for testing, Jan is creating new flashmobs every five minutes
		// url += "?limit=10";
		new DownloadTask(this).execute(url);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(locationListener);
	};

	@Override
	protected void onResume() {
		super.onResume();
		// when our activity resumes, we want to register for location updates
		myLocationOverlay.enableMyLocation();
		mc.setZoom(zoomLevel);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// reset MapView width and height
		mapView.getController().zoomIn();
		mapView.getController().zoomOut();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// when our activity pauses, we want to remove listening for location
		// updates
		myLocationOverlay.disableMyLocation();
		zoomLevel = mapView.getZoomLevel();
	}

	/**
	 * This method zooms to the user's location with a zoom level of 10.
	 */
	private void zoomToMyLocation() {
		GeoPoint myLocationGeoPoint = myLocationOverlay.getMyLocation();
		if (myLocationGeoPoint != null) {
			mapView.getController().animateTo(myLocationGeoPoint);
		} else {
			Toast.makeText(this, "Cannot determine location",
					Toast.LENGTH_SHORT).show();
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

	private GeoPoint getPoint(double lat, double lon) {
		return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
	}

	private class FlashmobsOverlay extends BalloonItemizedOverlay<OverlayItem> {
		private ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
		private ArrayList<Flashmob> flashmobs = new ArrayList<Flashmob>();

		public FlashmobsOverlay(Drawable defaultMarker, MapView mapView) {
			super(boundCenter(defaultMarker), mapView);
		}

		public void addOverlay(OverlayItem overlay, Flashmob flashmob) {
			items.add(overlay);
			flashmobs.add(flashmob);
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return (items.get(i));
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);

			boundCenterBottom(marker);
		}

		@Override
		protected boolean onBalloonTap(int index, OverlayItem item) {
			// Password
			final String key = flashmobs.get(index).getKey();
			if (key != null && flashmobs.get(index).getSelectedRole() == null) {
				new PasswordDialog(MapActivity.this, flashmobs.get(index));
			} else {
				Intent intent = new Intent(getApplicationContext(),
						DetailsActivity.class);
				intent.putExtra("id", flashmobs.get(index).getId());
				startActivity(intent);
			}

			return true;
		}

		@Override
		public int size() {
			return (items.size());
		}

	}

	public class MyCustomLocationOverlay extends MyLocationOverlay {
		private Context mContext;
		private float mOrientation;

		public MyCustomLocationOverlay(Context context, MapView mapView) {
			super(context, mapView);
			mContext = context;
		}

		@Override
		protected void drawMyLocation(Canvas canvas, MapView mapView,
				Location lastFix, GeoPoint myLocation, long when) {
			// translate the GeoPoint to screen pixels
			Point screenPts = mapView.getProjection()
					.toPixels(myLocation, null);

			// create a rotated copy of the marker
			Bitmap arrowBitmap = BitmapFactory.decodeResource(
					mContext.getResources(), R.drawable.arrow_red);
			Matrix matrix = new Matrix();
			matrix.postRotate(mOrientation);

			Bitmap rotatedBmp = Bitmap.createBitmap(arrowBitmap, 0, 0,
					arrowBitmap.getWidth(), arrowBitmap.getHeight(), matrix,
					true);
			// add the rotated marker to the canvas
			canvas.drawBitmap(rotatedBmp, screenPts.x
					- (rotatedBmp.getWidth() / 2),
					screenPts.y - (rotatedBmp.getHeight() / 2), null);
		}

		public void setOrientation(float newOrientation) {
			mOrientation = newOrientation;
		}
	}

	class DownloadTask extends AsyncTask<String, Void, ArrayList<Flashmob>> {
		ProgressDialog progressDialog;

		public DownloadTask(Context context) {
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Loading flashmobs...");
		}

		@Override
		protected ArrayList<Flashmob> doInBackground(String... url) {
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(url[0]);
				HttpResponse response = client.execute(request);
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
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		@Override
		protected void onPostExecute(ArrayList<Flashmob> flashmobs) {
			super.onPostExecute(flashmobs);
			if (flashmobs != null) {
				// create new overlay
				itemizedOverlay = new FlashmobsOverlay(marker, mapView);
				itemizedOverlay.setShowClose(false);
				itemizedOverlay.setShowDisclosure(true);
				// List of Points (FMs) to display
				for (Flashmob f : flashmobs) {
					OverlayItem o;
					if (f.getKey() == null) {
						o = new OverlayItem(getPoint(f.getLocation()
								.getLatitudeE6() / 1e6, f.getLocation()
								.getLongitudeE6() / 1e6), f.getTitle(),
								f.getStreetAddress() + " \u00B7 " + f.getDate());
					} else {
						o = new OverlayItem(getPoint(f.getLocation()
								.getLatitudeE6() / 1e6, f.getLocation()
								.getLongitudeE6() / 1e6), f.getTitle()
								+ "  [private]", f.getStreetAddress()
								+ " \u00B7 " + f.getDate());
					}
					itemizedOverlay.addOverlay(o, f);
				}
				mapView.getOverlays().add(itemizedOverlay);
				if (getIntent().hasExtra("id")) {
					Flashmob f = ((Store) getApplicationContext())
							.getFlashmobById(getIntent().getStringExtra("id"));
					mapView.getController().setCenter(f.getLocation());
					mapView.getController().setZoom(17);
				}
				mapView.invalidate();
			} else {
				Toast.makeText(getApplicationContext(),
						"There is a problem with the Internet connection.",
						Toast.LENGTH_LONG).show();
			}
			progressDialog.dismiss();
		}

	}

	// avoid resuming activity on switch from portrait to landscape, etc.
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
