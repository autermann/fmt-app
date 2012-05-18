package de.ifgi.fmt.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.ifgi.fmt.R;
import de.ifgi.fmt.objects.Flashmob;

public class FlashmobListAdapter extends ArrayAdapter<Flashmob> {
	Context context;
	ArrayList<Flashmob> flashmobs;
	Location location;

	public FlashmobListAdapter(Context context, ArrayList<Flashmob> flashmobs,
			Location location) {
		super(context, 0, flashmobs);
		this.context = context;
		this.flashmobs = flashmobs;
		this.location = location;
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
		((TextView) v.findViewById(R.id.flashmob_distance))
				.setText(new DecimalFormat("0.#").format(f
						.getDistanceInKilometersTo(location)) + " km");
		((TextView) v.findViewById(R.id.flashmob_place_time)).setText(f
				.getStreetAddress() + " \u00B7 " + f.getStartDate());
		return v;
	}

}
