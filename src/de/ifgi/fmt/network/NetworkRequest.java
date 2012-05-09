package de.ifgi.fmt.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public class NetworkRequest {
	public static final int METHOD_GET = 1;
	public static final int METHOD_POST = 2;

	public static final int RESULT_OK = 3;
	public static final int NETWORK_PROBLEM = 4;
	public static final int DATA_PROBLEM = 5;

	private String url;
	private int method = METHOD_GET;

	private ArrayList<NameValuePair> parameters;
	private String result;

	public NetworkRequest(String url, int method,
			ArrayList<NameValuePair> parameters) {
		this.url = url;
		if (method != 0) {
			this.method = method;
		}
		if (parameters != null) {
			this.parameters = parameters;
		}
	}

	public NetworkRequest(String url, int method) {
		this.url = url;
		if (method != 0) {
			this.method = method;
		}
	}

	public NetworkRequest(String url) {
		this.url = url;
	}

	public int send() {
		try {
			if (this.method == METHOD_GET) {
				InputStream in = new URL(url)
						.openStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in));
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				in.close();
				if (sb.toString().equals("")) {
					return DATA_PROBLEM;
				}
				setResult((sb.toString()));
			} else if (this.method == METHOD_POST) {
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost postMethod = new HttpPost(url);
				postMethod.setEntity(new UrlEncodedFormEntity(parameters));
				HttpResponse response = httpClient.execute(postMethod);
				String responseBody = EntityUtils
						.toString(response.getEntity());
				setResult(responseBody);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			return NETWORK_PROBLEM;
		}
		return RESULT_OK;
	}

	public static String getMessage(int statusCode) {
		switch (statusCode) {
		case NETWORK_PROBLEM:
			return "There is a problem with the network connection.";
		case DATA_PROBLEM:
			return "There is a problem with the data.";
		default:
			return null;
		}
	}
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		this.method = method;
	}

	public ArrayList<NameValuePair> getParameters() {
		return parameters;
	}

	public void setParameters(ArrayList<NameValuePair> parameters) {
		this.parameters = parameters;
	}
	
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
}
