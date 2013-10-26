package ch.ethz.inf.vs.android.nethz.ws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private String DebugTag = "SOAP";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		// Make the raw XML TextView scrollable
		TextView rawXMLTextView = (TextView) findViewById(R.id.rawXMLResponse);
		rawXMLTextView.setMovementMethod(new ScrollingMovementMethod());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void getTemp (View v) {
		Log.d(DebugTag, "Starting to get temperature normal way...");
		new SOAPWorker(SOAPWorker.MODE_SOAP_NORMAL).execute();
	}
	
	public void getRawXML (View v) {
		Log.d(DebugTag, "Starting to get temperature raw XML way...");
		new SOAPWorker(SOAPWorker.MODE_SOAP_RAW).execute();
	}
	
	/**
     * Handles the communication with the SOAP service 
     */
    private class SOAPWorker extends AsyncTask<String, Void, String> {
            /**
             * Soap configuration
             */
    		private final String METHOD_NAME = "getSpot";
    		private final String NAMESPACE = "http://webservices.vslecture.vs.inf.ethz.ch/";
    		private final String SOAP_ACTION = "http://webservices.vslecture.vs.inf.ethz.ch/SunSPOTWebservice/getSpotRequest";
    		private final String URL = "http://vslab.inf.ethz.ch:8080/SunSPOTWebServices/SunSPOTWebservice";
    		private final int SOAP_VERSION = SoapEnvelope.VER11;
            
            /**
             * Different modes (for each assignment). We use this
             * because we do not want to write a class for each
             * assignment.
             */
            public static final int MODE_SOAP_NORMAL = 0;
            public static final int MODE_SOAP_RAW = 1;
            private int mode;
            
            // constructor, set the mode here once
            public SOAPWorker (int mode) {
                    this.mode = mode;
            }
            
            @Override
            protected String doInBackground(String... urls) {
            	
            	// Set default response
            	String resultString = getString(R.string.io_error);
            	
            	// Check for internet connection
                if (!hasInternetConnection()) {
                	resultString = getString(R.string.no_internet);
                	return resultString;
                }
        		
                // Configure the SOAP request
        		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                request.addProperty("id", "Spot3");

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SOAP_VERSION);
                envelope.setOutputSoapObject(request);

                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                if (this.mode == MODE_SOAP_RAW)
                	androidHttpTransport.debug = true;

                // Try to perform the SOAP Request
                try {
					androidHttpTransport.call(SOAP_ACTION, envelope);
					
					// Depending on the mode we have to treat the response differently
					if (this.mode == MODE_SOAP_RAW) {
						
						// Simply return the raw XML as result
						resultString = androidHttpTransport.responseDump;
				        
					} else {
						// Attempt to unmarshal the response
						SoapObject result = null;
						try {
							result = (SoapObject)envelope.getResponse();
							
							// Read temperature from resultData and return as String
							resultString = getString(R.string.temperature) + " " + result.getPropertyAsString("temperature");
			                Log.d(DebugTag, "SOAP Response: "+result.toString());
							
						} catch (SoapFault e) {
							resultString = getString(R.string.unmarshal_error);
						}
					}
					
				} catch (IOException e) {
					Log.d(DebugTag, "HTTP Error msg: "+e.getMessage());
				} catch (XmlPullParserException e) {
					// Ignore, default is already io error
				}
                
                return resultString;
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
                    
                    // Depending on the mode we are in, update the TextViews
                    // this method is synchronized with the user main thread (handled by Android), so no problems occur...
                    
                    
                    if (result != null && this.mode == MODE_SOAP_NORMAL) {
                    	TextView responseView = (TextView)findViewById(R.id.tempTextView);
                    	responseView.setText(result);
                    } else if (this.mode == MODE_SOAP_RAW) {
                    	
                    	String extractedTemperature = null;
                    	
                    	// Parse Raw XML and extract temperature
						XmlPullParser parser = Xml.newPullParser();
						try {
							parser.setInput(new ByteArrayInputStream(result.getBytes()), null);
							parser.nextTag();
						
					        while (parser.next() != XmlPullParser.END_DOCUMENT) {
					            if (parser.getEventType() != XmlPullParser.START_TAG) {
					                continue;
					            }
					            String name = parser.getName();
					            
					            // We are only interest in the temperature tag
					            if (name.equals("temperature")) {
					            	parser.next();
					            	extractedTemperature = parser.getText();
					            }
					        }
						} catch (XmlPullParserException e) {
						} catch (IOException e) {
						}
                    	
                    	TextView rawXMLView = (TextView) findViewById(R.id.rawXMLResponse);
                    	rawXMLView.setText(result);
                    	
                    	TextView rawTempView = (TextView) findViewById(R.id.rawTempTextView);
                    	rawTempView.setText(getString(R.string.rawXMLTemp) + " " + extractedTemperature);
                    	
                    }
            }
            
            @Override
            protected void onCancelled() {
                    super.onCancelled();
            }
    }

}
