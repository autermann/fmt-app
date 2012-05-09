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
import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MyLocationOverlay;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.ifgi.fmt.R;

public class MapActivity extends SherlockMapActivity {
	private static final int MENU_LAYER_MAP = 1;
	private static final int MENU_LAYER_SATELLITE = 2;

	private MapView mapView = null;
	MapController mc;
	GeoPoint p;
	private MyLocationOverlay me = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		mapView = (MapView) findViewById(R.id.mapview);
		Drawable marker = getResources().getDrawable(R.drawable.location);

		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
				marker.getIntrinsicHeight());

		mapView.getOverlays().add(new SitesOverlay(marker));

		me = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(me);

		// start position when loading the map
		mc = mapView.getController();
		String coordinates[] = { "51.962956", "7.629592" };
		double lat = Double.parseDouble(coordinates[0]);
		double lng = Double.parseDouble(coordinates[1]);

		p = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));

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
		return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		me.enableCompass();
	}

	@Override
	public void onPause() {
		super.onPause();
		me.disableCompass();
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
		private List<OverlayItem> items = new ArrayList<OverlayItem>();
		private Drawable marker = null;
		private PopupPanel panel = new PopupPanel(R.layout.popup);

		public SitesOverlay(Drawable marker) {
			super(marker);
			this.marker=marker; 
			
			boundCenterBottom(marker);

			// List of Points (FMs) to display
			items.add(new OverlayItem(getPoint(51.940932, 7.609992),
					"Freeze Flashmob", " Come here on 23.05.2012"));
			items.add(new OverlayItem(getPoint(51.951195, 7.603297),
					"Freeze Flashmob", "Starting on 24.05.2012"));
			items.add(new OverlayItem(getPoint(51.962409, 7.631621),
					"Pillow Flashmob", "Party @ 25.05.2012"));
			items.add(new OverlayItem(getPoint(51.963995, 7.610507),
					"Pillow Flashmob", "Let's meet: 26.05.2012"));

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
		protected boolean onTap(int i) {
			OverlayItem item = getItem(i);
			GeoPoint geo = item.getPoint();
			Point pt = mapView.getProjection().toPixels(geo, null);

			View view = panel.getView();
			 
            ((TextView) view.findViewById(R.id.title)).setText(String.valueOf(item.getTitle()));
            ((TextView) view.findViewById(R.id.description)).setText(String.valueOf(item.getSnippet()));
            
            Button more = (Button) view.findViewById(R.id.more);
            more.setOnClickListener(new OnClickListener() {
            	public void onClick(View v) {
            			System.out.println("YAHOOOOOOO ");
            			//funktioniert!            
            			}
            	});
            
            
            
			panel.show(pt.y * 2 > mapView.getHeight());
			
			

			return (true);
		}

		@Override
		public int size() {
			return (items.size());
		}
		
	
	}

	class PopupPanel {
		View popup;
		boolean isVisible = false;

		PopupPanel(int layout) {
			ViewGroup parent = (ViewGroup) mapView.getParent();

			popup = getLayoutInflater().inflate(layout, parent, false);

			popup.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					hide();
				}
			});
			
			mapView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					isVisible = false;
					((ViewGroup) popup.getParent()).removeView(popup);			
				}
			});
			
		}
		
		View getView() {
			return (popup);
		}

		void show(boolean alignTop) {
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);

			if (alignTop) {
				lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				lp.setMargins(0, 20, 0, 0);
			} else {
				lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				lp.setMargins(0, 0, 0, 60);
			}

			hide();

			((ViewGroup) mapView.getParent()).addView(popup, lp);
			isVisible = true;
		}

		void hide() {
			if (isVisible) {
				isVisible = false;
				((ViewGroup) popup.getParent()).removeView(popup);
			}
		}
	}

}
