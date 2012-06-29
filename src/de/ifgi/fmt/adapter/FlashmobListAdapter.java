package de.ifgi.fmt.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import de.ifgi.fmt.R;
import de.ifgi.fmt.activities.ContentActivity;
import de.ifgi.fmt.objects.Flashmob;

public class FlashmobListAdapter extends ArrayAdapter<Flashmob> {
	Context context;
	ArrayList<Flashmob> flashmobs;
	Location location;
	boolean showDistance = false;
	boolean showPlayButton = false;

	public FlashmobListAdapter(Context context, ArrayList<Flashmob> flashmobs) {
		super(context, 0, flashmobs);
		this.context = context;
		this.flashmobs = flashmobs;
	}

	public FlashmobListAdapter(Context context, ArrayList<Flashmob> flashmobs,
			Location location, boolean showDistance, boolean showPlayButton) {
		super(context, 0, flashmobs);
		this.context = context;
		this.flashmobs = flashmobs;
		this.location = location;
		this.showDistance = showDistance;
		this.showPlayButton = showPlayButton;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater l = LayoutInflater.from(context);
			v = l.inflate(R.layout.flashmob_list_item, null);
		}
		Flashmob f = flashmobs.get(position);
		((TextView) v.findViewById(R.id.flashmob_title)).setText(f.getTitle());
		if (location != null && showDistance) {
			double distance = f.getDistanceInKilometersTo(location);
			String distanceText;
			if (distance < 1) {
				distance *= 10;
				distance = Math.round(distance);
				distance *= 100;
				distanceText = new DecimalFormat("0.#").format(distance);
				distanceText += " m";
			} else {
				distanceText = new DecimalFormat("0.#").format(distance);
				distanceText += " km";
			}
			((TextView) v.findViewById(R.id.flashmob_distance))
					.setText(distanceText);
		} else {
			((TextView) v.findViewById(R.id.flashmob_distance))
					.setVisibility(View.GONE);
		}
		((TextView) v.findViewById(R.id.flashmob_place_time)).setText(f
				.getStreetAddress() + " \u00B7 " + f.getStartDate());
		FrameLayout startButton = (FrameLayout) v
				.findViewById(R.id.start_flashmob_button);
		if (showPlayButton) {
			startButton.setTag(f.getId());
			startButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, ContentActivity.class);
					intent.putExtra("id", (String) v.getTag());
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
				}
			});
		} else {
			startButton.setVisibility(View.GONE);
		}
		return v;
	}

}
