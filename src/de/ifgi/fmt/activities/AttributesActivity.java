package de.ifgi.fmt.activities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;

public class AttributesActivity extends SherlockActivity {
	TextView startDate;
	int startYear;
	int startMonth;
	int startDay;
	TextView endDate;
	int endYear;
	int endMonth;
	int endDay;
	static final int DATE_DIALOG_ID = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.attributes_activity);

		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

		startDate = (TextView) findViewById(R.id.start_date);
		endDate = (TextView) findViewById(R.id.end_date);

		// get the current date
		final Calendar c = Calendar.getInstance();
		startYear = c.get(Calendar.YEAR);
		startMonth = c.get(Calendar.MONTH);
		startDay = c.get(Calendar.DAY_OF_MONTH);

		endYear = c.get(Calendar.YEAR);
		endMonth = c.get(Calendar.MONTH);
		endDay = c.get(Calendar.DAY_OF_MONTH);

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
	}

	private void updateStartDate() {
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
					.parse(startYear + "-" + (startMonth + 1) + "-" + startDay);
			DateFormat dateFormat = DateFormat
					.getDateInstance(DateFormat.MEDIUM);
			startDate.setText(dateFormat.format(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private void updateEndDate() {
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
					.parse(endYear + "-" + (endMonth + 1) + "-" + endDay);
			DateFormat dateFormat = DateFormat
					.getDateInstance(DateFormat.MEDIUM);
			endDate.setText(dateFormat.format(date));
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