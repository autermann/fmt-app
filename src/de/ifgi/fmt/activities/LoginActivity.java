package de.ifgi.fmt.activities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpEntity;


import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;


public class LoginActivity extends SherlockActivity
{
	public static final int STATUS_NOT_VALID = 12;
	public static final int STATUS_OK = 11;

	public static final int REDIRECT_TO_START_ACTIVITY = 1;
	public static final int REDIRECT_TO_MY_FLASHMOBS_ACTIVITY = 2;
	// ...

	private EditText username, password;
	private Button login;
	private ProgressDialog progressDialog;
	private String userpassEncoded;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		setTitle("Login");
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		username = (EditText) findViewById(R.id.username);

		// already in XML, specially for HTC in Java
		username.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		password = (EditText) findViewById(R.id.password);
		password.setOnEditorActionListener(new OnEditorActionListener()
		{
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if (actionId == 0)
				{
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
					login.performClick();
				}
				return false;
			}
		});

		login = (Button) findViewById(R.id.login);
		login.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				authenticate();
			}
		});

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Authenticating...");
	}

	private void authenticate()
	{
		if (username.getText().toString().equals("") || password.getText().toString().equals(""))
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Missing data");
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setMessage("Please enter your user name and password.").setNeutralButton("OK",
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.cancel();
						}
					});
			builder.create().show();
		}
		else
		{
			progressDialog.show();
			new Thread()
			{
				public void run()
				{

					// Login request (server) goes here...
					try
					{
						
						userpassEncoded = Base64.encodeToString(
								(username.getText() + ":" + password.getText()).getBytes("UTF-8"), Base64.NO_WRAP);
//						
//						NetworkRequest request = new NetworkRequest("http://giv-flashmob.uni-muenster.de/fmt?authentication:basic " + userpassEncoded);
//
//						request.send();
//						Log.d("FMT Login", userpassEncoded);
//						Log.d("FMT Login", request.getUrl());		
						
						HttpClient client = new DefaultHttpClient();  
			            String getURL = "http://giv-flashmob.uni-muenster.de/fmt/";
			            HttpGet get = new HttpGet(getURL);
			            get.setHeader("Authorization", "Basic "+ userpassEncoded);
			            HttpResponse responseGet = client.execute(get);      
			            HttpEntity resEntityGet = responseGet.getEntity();  
			            if (resEntityGet != null) {  
			                String response = EntityUtils.toString(resEntityGet);
			                Log.i("GET RESPONSE",response);			                        
			            }


			            
			            
			            
			             


						
					}
					catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					handler.sendEmptyMessage(0);

				};

				private Handler handler = new Handler()
				{
					@Override
					public void handleMessage(Message msg)
					{
						// work with the result
						switch (msg.what)
						{
						default:
							SharedPreferences preferences = PreferenceManager
									.getDefaultSharedPreferences(getApplicationContext());
							SharedPreferences.Editor editor = preferences.edit();
							editor.putString("user_id", "4fe09086e4b0662f088a4fa5");
							editor.putString("user_name", "markymark");
							editor.putString("user_email", "wahlberg@hollywood.com");
							editor.commit();
							break;
						}
						progressDialog.dismiss();
						redirect();
					}
				};
			}.start();
		}
	}

	public void redirect()
	{
		Intent intent = null;
		switch (getIntent().getExtras().getInt("startActivity"))
		{
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
		Toast.makeText(this, "Welcome " + username.getText(), Toast.LENGTH_LONG).show();
		finish();
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
