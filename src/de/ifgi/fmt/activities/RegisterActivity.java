package de.ifgi.fmt.activities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;
import de.ifgi.fmt.network.NetworkRequest;

public class RegisterActivity extends SherlockActivity {
	public static final int STATUS_NOT_VALID = 12;
	public static final int STATUS_OK = 11;

	public static final int INVALID_CREDENTIALS = 11;

	public static final int REDIRECT_TO_START_ACTIVITY = 1;
	public static final int REDIRECT_TO_MY_FLASHMOBS_ACTIVITY = 2;
	// ...

	private EditText username, password;
	private Button register;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_activity);
		setTitle("Register");
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		username = (EditText) findViewById(R.id.username);

		// already in XML, specially for HTC in Java
		username.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		password = (EditText) findViewById(R.id.password);
		password.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == 0) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
					register.performClick();
				}
				return false;
			}
		});

		register = (Button) findViewById(R.id.register);
		register.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				registering();
			}
		});
	}

	private void registering() {
		if (username.getText().toString().equals("")
				|| password.getText().toString().equals("")) {
			Toast.makeText(getApplicationContext(),
					"Please enter a valid user name and password.", Toast.LENGTH_LONG)
					.show();
		} else {
			new RegisterTask(this).execute();
		}
	}

	class RegisterTask extends AsyncTask<String, Void, Integer> {
		ProgressDialog progressDialog;

		public RegisterTask(Context context) {
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Registering...");
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		
		@Override
		protected Integer doInBackground(String... url) {
		    // Create a new HttpClient and Post Header
		    HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost("http://giv-flashmob.uni-muenster.de/fmt/users");

		    try {
		        
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair("username", "stefan"));
		        nameValuePairs.add(new BasicNameValuePair("password", "wer"));
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		        // Execute HTTP Post Request
		        HttpResponse response = httpclient.execute(httppost);
		        Log.d("register-log", response.toString());
		        
		    } catch (ClientProtocolException e) {
		        // TODO Auto-generated catch block
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    }
			return 0;
		} 
		
//		@Override
//		protected Integer doInBackground(String... url) {
//			
//			NetworkRequest n = new NetworkRequest("http://giv-flashmob.uni-muenster.de/fmt/users");
//
//			n.setMethod(NetworkRequest.METHOD_POST);
//			ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
//
//			// normalerweise: username.getText().toString()   & password.getText ...
//			parameters.add(new BasicNameValuePair("username", "stefan"));
//			parameters.add(new BasicNameValuePair("password", "wer"));
//			parameters.add(new BasicNameValuePair("email", "user@test.de"));
//			
//			
//			n.setParameters(parameters);
//
//			n.send();
//			n.getResult();
//		
//			
//			return 0;
//		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			switch (result) {
			case NetworkRequest.NETWORK_PROBLEM:
				Toast.makeText(getApplicationContext(),
						"There is a problem with the Internet connection.",
						Toast.LENGTH_LONG).show();
				break;
			case INVALID_CREDENTIALS:
				Toast.makeText(getApplicationContext(),
						"This username already exists",
						Toast.LENGTH_LONG).show();
				password.setText("");
				break;
			default:
				redirect();
				break;
			}
			progressDialog.dismiss();
		}

	}

	public void redirect() {
		Intent intent = null;
		switch (getIntent().getExtras().getInt("startActivity")) {
		case REDIRECT_TO_START_ACTIVITY:
			intent = new Intent(this, StartActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			break;
		case REDIRECT_TO_MY_FLASHMOBS_ACTIVITY:
			intent = new Intent(this, StartActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			break;
		}
		startActivity(intent);
		Toast.makeText(this, "Thank you for registering " + username.getText(), Toast.LENGTH_LONG)
				.show();
		finish();
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
