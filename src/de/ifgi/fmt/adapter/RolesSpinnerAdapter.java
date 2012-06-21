package de.ifgi.fmt.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import de.ifgi.fmt.R;
import de.ifgi.fmt.objects.Role;

public class RolesSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {
	Context context;
	ArrayList<Role> roles;

	public RolesSpinnerAdapter(Context context, ArrayList<Role> roles) {
		this.context = context;
		this.roles = roles;
	}

	@Override
	public int getCount() {
		return roles.size();
	}

	@Override
	public Object getItem(int position) {
		return roles.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater l = LayoutInflater.from(context);
			v = l.inflate(R.layout.spinner, null);
		}
		((TextView) v.findViewById(android.R.id.text1)).setText(roles.get(
				position).getTitle());
		return v;
	}

}