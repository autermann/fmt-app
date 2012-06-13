package de.ifgi.fmt.activities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;

public class AttributesActivity extends SherlockActivity {
	TextView search;
	Date startDate;
	TextView startDateTextView;
	int startYear;
	int startMonth;
	int startDay;
	Date endDate;
	TextView endDateTextView;
	int endYear;
	int endMonth;
	int endDay;
	CheckBox showPrivate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.attributes_activity);

		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

		startDateTextView = (TextView) findViewById(R.id.start_date);
		endDateTextView = (TextView) findViewById(R.id.end_date);

		// get the current date
		final Calendar c = Calendar.getInstance();
		startYear = c.get(Calendar.YEAR);
		startMonth = c.get(Calendar.MONTH);
		startDay = c.get(Calendar.DAY_OF_MONTH);

		endYear = c.get(Calendar.YEAR);
		endMonth = 11;
		endDay = 31;

		search = (TextView) findViewById(R.id.search);
		showPrivate = (CheckBox) findViewById(R.id.show_private);

		// display the current date (this method is below)
		updateStartDate();
		updateEndDate();

		findViewById(R.id.start_date_row).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						new DatePickerDialog(AttributesActivity.this,
								startDateSetListener, startYear, startMonth,
								startDay).show();
					}
				});

		findViewById(R.id.end_date_row).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						new DatePickerDialog(AttributesActivity.this,
								endDateSetListener, endYear, endMonth, endDay)
								.show();
					}
				});

		findViewById(R.id.submit).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SimpleDateFormat df = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss.SSSZ");

				Intent intent = new Intent(AttributesActivity.this,
						AttributesResultsActivity.class);
				String url = "http://giv-flashmob.uni-muenster.de/fmt/flashmobs";
				url += "?";
				try {
					url += "from="
							+ URLEncoder.encode(df.format(startDate), "UTF-8")
							+ "&" + "to="
							+ URLEncoder.encode(df.format(endDate), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (!showPrivate.isChecked()) {
					url += "&" + "show=" + "PUBLIC";
				}
				if (search.getText().toString().compareTo("") != 0) {
					url += "&" + "search=" + search.getText();
				}

				intent.putExtra("URL", url);
				Log.i("wichtig", "URL: " + intent.getExtras().getString("URL"));
				startActivity(intent);
			}
		});
	}

	private void updateStartDate() {
		try {
			startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
					.parse(startYear + "-" + (startMonth + 1) + "-" + startDay);
			DateFormat dateFormat = DateFormat
					.getDateInstance(DateFormat.MEDIUM);
			startDateTextView.setText(dateFormat.format(startDate));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private void updateEndDate() {
		try {
			endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
					.parse(endYear + "-" + (endMonth + 1) + "-" + endDay);
			DateFormat dateFormat = DateFormat
					.getDateInstance(DateFormat.MEDIUM);
			endDateTextView.setText(dateFormat.format(endDate));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private DatePickerDialog.OnDateSetListener startDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			startYear = year;
			startMonth = monthOfYear;
			startDay = dayOfMonth;
			updateStartDate();
		}

	};

	private DatePickerDialog.OnDateSetListener endDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			endYear = year;
			endMonth = monthOfYear;
			endDay = dayOfMonth;
			updateEndDate();
		}

	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
}