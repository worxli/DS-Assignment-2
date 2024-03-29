package ch.ethz.inf.vs.android.lukasbi.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
	 * Google chart service
	 */
	private final String GOOGLE_CHART = "https://chart.googleapis.com/chart";
	private final int CHART_WIDTH = 310, CHART_HEIGHT = 400;
	private final int Y_AXIS_SCALE = 40;
	private final int DATA_SIZE = 10;
	private final int TIMEOUT = 5;
	
	/**
	 * UI Elements
	 */
	TextView resultText;
	Button rawRequest, apacheRequest, jsonRetrieve, jsonParse;
	ImageView imgChart;
	
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
		public static final int MODE_CHART = 4;
		private int mode;
		
		/**
		 * JSON Tags
		 */
		private static final String TAG_NAME = "name";
		private static final String TAG_VALUE = "value";
		
		/**
		 * Chart
		 */
		private Bitmap bmp;
		public String data;
		public boolean retrieveWithLabelAndWrite = true;
		
		// constructor, set the mode here once
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
							case RESTWorker.MODE_RAW_REST:
								result = rawRequest(address, url);
								break;
							
							case RESTWorker.MODE_USE_APACHE_LIBRARY:
								result = apacheRequest(fullURL, RESTWorker.MODE_USE_APACHE_LIBRARY);
								break;
								
							case RESTWorker.MODE_NEGOTIATION_JSON:
								result = apacheRequest(fullURL, RESTWorker.MODE_NEGOTIATION_JSON);
								break;
								
							case RESTWorker.MODE_DISPLAY_JSON:
								String json = apacheRequest(fullURL, RESTWorker.MODE_DISPLAY_JSON);
								
								// parse the retrieved json string and extract the temperature
								JSONObject jobject = new JSONObject(json);
								String name = jobject.getString(TAG_NAME);
								double value = jobject.getDouble(TAG_VALUE);
								if (retrieveWithLabelAndWrite)
									result = String.format("%s: %2.2f", name, value);
								else
									result = Double.toString(value);
								break;
							
							case RESTWorker.MODE_CHART:
								result = chartRequest(url);
								break;
						}
					} else {
						result = getString(R.string.no_internet);
						break;
					}
				// if any exception occures, print it to the user screen
				} catch (UnknownHostException e) {
					result = e.getMessage();
				} catch (IOException e) {
					result = getString(R.string.io_error) + e.getMessage();
				} catch (JSONException e) {
					result = e.getMessage();
				}
			}

			return result;
		}
		
		/**
		 * This does a request to the Google Chart Service
		 */
		private String chartRequest (String url) throws UnknownHostException, IOException {
			HttpClient client = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);

			// chart labels
			String labelX = getString(R.string.time);
			String labelY = getString(R.string.temperature);
			
			// construct POST request
			try {
				Log.d(MainActivity.DEBUG_TAG, data);
				List<NameValuePair> postData = new ArrayList<NameValuePair>(2);
				postData.add(new BasicNameValuePair("chs", String.format("%dx%d", CHART_WIDTH, CHART_HEIGHT))); // chart size
				postData.add(new BasicNameValuePair("cht", "bvs")); // vertical bars
				postData.add(new BasicNameValuePair("chd", "t:" + data)); // vertical bars
				postData.add(new BasicNameValuePair("chxt", "x,x,y,y")); // vertical bars
				postData.add(new BasicNameValuePair("chf", "bg,s,FFFFFF")); // background color
				// scale axis, max 10 samples and y ranges from 0 to x=40 with steps of size 5
				postData.add(new BasicNameValuePair("chds", String.format("0,%d", Y_AXIS_SCALE))); // scale data
				postData.add(new BasicNameValuePair("chxr", String.format("0,1,10|2,0,%d,5", Y_AXIS_SCALE))); // scale axis
				postData.add(new BasicNameValuePair("chxl", String.format("1:|%s|3:|%s", labelX, labelY))); // labels
				postData.add(new BasicNameValuePair("chxp", "1,50")); // axis position
				httppost.setEntity(new UrlEncodedFormEntity(postData));
			
				// get the chart as a inputstream and then decode it into a bmp
				InputStream chart = client.execute(httppost).getEntity().getContent();
				bmp = BitmapFactory.decodeStream(chart);
				chart.close();
			} catch (ClientProtocolException e) {
				return e.getMessage();
			}
			
			// doesnt matter what we return, output text will be flushed anyway
			return "";
		}
		
		/**
		 * This does a request to the REST service with the usage of the Apache HTTP library
		 */
		private String apacheRequest (String url, int mode) throws UnknownHostException, IOException {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			request.setHeader("Connection", "close");
			if (mode == RESTWorker.MODE_NEGOTIATION_JSON || mode == RESTWorker.MODE_DISPLAY_JSON)
				request.setHeader("Accept", "application/json");
			
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
			if (result != null) {
				switch (mode) {
					default:
						// clear chart and write REST response
						imgChart.setImageResource(R.drawable.blank);
						if (retrieveWithLabelAndWrite) writeResult(result);
						break;
					case RESTWorker.MODE_CHART:
						// clear REST response and display chart
						writeResult("");
						imgChart.setImageBitmap(bmp);
						break;
				}
			}
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
		imgChart = (ImageView) findViewById(R.id.img_chart);
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
		// worker instance to get REST respone for the temperature of spot 1 using Apache HTTP library
		new RESTWorker(RESTWorker.MODE_USE_APACHE_LIBRARY).execute(PAGE[0]);
	}
	
	public void jsonRetrieve (View v) {
		// worker instance to get REST respone for the temperature of spot 1 using JSON for response
		new RESTWorker(RESTWorker.MODE_NEGOTIATION_JSON).execute(PAGE[0]);
	}

	public void jsonParse (View v) {
		// worker instance to get REST respone for the temperature of spot 1, displaying only the temperature
		new RESTWorker(RESTWorker.MODE_DISPLAY_JSON).execute(PAGE[0]);
	}
	
	public void visualize (View v) {
		// visualize the temperature of a few samples in a chart
		writeResult(getString(R.string.collection_data)); // because the 'get' method is blocking

		// collecting asynchron data
		String data = "";
		RESTWorker[] workers = new RESTWorker[DATA_SIZE];
		for (int i = 0; i < DATA_SIZE; i++) {
			workers[i] = new RESTWorker(RESTWorker.MODE_DISPLAY_JSON);
			workers[i].retrieveWithLabelAndWrite = false;
			workers[i].execute(PAGE[0]);
			try {
				/**
				 * Attention: Blocking method 'get'! Thats the reason we only wait
				 * for @TIMEOUT seconds for each worker to retrieve the data.
				 */
				data += (i == 0 ? "" : ",") + workers[i].get(TIMEOUT, TimeUnit.SECONDS);
			} catch (Exception e) {
				writeResult(e.getMessage());
			}
		}
		
		// display the chart
		RESTWorker chartWorker = new RESTWorker(RESTWorker.MODE_CHART);
		chartWorker.data = data;
		chartWorker.execute(GOOGLE_CHART);
	}
	
	/**
	 * Write to GUI
	 * @param result
	 */
	private void writeResult (String result) {
		resultText.setText(result);
	}
}
