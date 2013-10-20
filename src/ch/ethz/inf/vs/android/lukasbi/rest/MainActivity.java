package ch.ethz.inf.vs.android.lukasbi.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	/**
	 * Debugging
	 */
	private static final String DEBUG_TAG = "REST";
	
	/**
	 * WebService
	 */
	private final String URL = "vslab.inf.ethz.ch";
	private final int PORT = 8081;
	private final String[] PAGE = {
		"sunspots/Spot1/sensors/temperature" // temperature information of spot 1
	};
	
	/**
	 * UI Elements
	 */
	TextView resultText;
	Button rawRequest, apacheRequest, jsonRetrieve, jsonParse;
	
	/**
	 * Worker instance
	 */
	//RESTWorker worker;
	
	/**
	 * Handles the communicatoin with the REST service 
	 */
	private class RESTWorker extends AsyncTask<String, Void, String> {
		/**
		 * Socket fields
		 */
		private Socket socket;
		private PrintWriter out;
		private BufferedReader in;
		
		/**
		 * Different modes (for each assignment). We use this
		 * because we do not want to write a class for each
		 * assignment.
		 */
		public static final int MODE_RAW_REST = 0;
		public static final int MODE_USE_APACHE_LIBRARY = 1;
		public static final int MODE_NEGOTIATION_JSON = 2;
		public static final int MODE_DISPLAY_JSON = 3;
		private int mode;
		
		public RESTWorker (int mode) {
			this.mode = mode;
		}
		
		@Override
		protected String doInBackground(String... urls) {
			// use default message
			String result = getString(R.string.io_error);
			
			// process all urls
			for (String url : urls) {
				try {
					// check for internet connection
					if (hasInternetConnection()) {
						InetAddress address = InetAddress.getByName(URL);
						String host = address.getHostName();
						String fullURL = String.format("http://%s:%d/%s", host, PORT, url);
								
						// for each assignment one case
						switch (mode) {
							default:
							case 0:
								result = rawRequest(address, url);
								break;
							
							case 1:
								result = apacheRequest(fullURL);
								break;
								
							case 2:
								result = rawRequest(address, url);
								break;
								
							case 3:
								result = rawRequest(address, url);
								break;
						
						}
					} else {
						writeResult(getString(R.string.no_internet));
					}
				} catch (UnknownHostException e) {
					writeResult(e.getMessage());
				} catch (IOException e) {
					writeResult(getString(R.string.io_error) + e.getMessage());
				}
			}

			return result;
		}
		
		/**
		 * This does a request to the REST service with the usage of the Apache HTTP library
		 */
		private String apacheRequest (String url) throws UnknownHostException, IOException {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			request.setHeader("Connection", "close");
			
			// do the request
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			
			// check status code
			if (status.getStatusCode() != 200)
				throw new IOException(getString(R.string.invalid_response));
			
			// pull content
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line, result = "";
			while ((line = in.readLine()) != null) {
				result += line;
			}
			
			in.close();
			return result;
		}
		
		/**
		 * This does a raw HTTP request "by hand" with the usage of sockets
		 */
		private String rawRequest (InetAddress address, String url) throws UnknownHostException, IOException {
			// socket setup
			socket = new Socket(address, PORT);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			// call the REST service "by hand"
			out.write("GET " + url + " HTTP/1.0\r\n");
			out.write("Connection: close\r\n");
			out.write("\r\n");
			out.flush();
			
			// reset results and read the response
			String line, result = "";
			while ((line = in.readLine()) != null) {
				result += line;
			}
			
			// close socket streams
			in.close();
			out.close();
			return result;
		}
		
		/**
		 * Checks that the device is connected to the internet
		 */
		private boolean hasInternetConnection () {
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			return (networkInfo != null && networkInfo.isConnected());
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			// write result to textview.
			// this method is synchronized with the user main thread (handled by Android), so no problems occur...
			writeResult(result);
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// get UI elements
		resultText = (TextView) findViewById(R.id.txt_result);
		rawRequest = (Button) findViewById(R.id.btn_raw_request);
		apacheRequest = (Button) findViewById(R.id.btn_apache_request);
		jsonRetrieve = (Button) findViewById(R.id.btn_json_retrieve);
		jsonParse = (Button) findViewById(R.id.btn_json_parse);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Buttonlisteners
	 */
	public void rawRequest (View v) {	
		// worker instance to get REST respone for the temperature of spot 1 using raw mode
		new RESTWorker(RESTWorker.MODE_RAW_REST).execute(PAGE[0]);
	}
	
	public void apacheRequest (View v) {
		new RESTWorker(RESTWorker.MODE_USE_APACHE_LIBRARY).execute(PAGE[0]);
	}
	
	public void jsonRetrieve (View v) {
		
	}

	public void jsonParse (View v) {
		
	}
	
	private void writeResult (String result) {
		resultText.setText(result);
	}
}
