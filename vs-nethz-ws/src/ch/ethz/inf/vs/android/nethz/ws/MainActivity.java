package ch.ethz.inf.vs.android.nethz.ws;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Log.d("MAIN", "Started, hello!");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void getTemp (View v) throws IOException, XmlPullParserException {
		Log.d("MOIN", "Hello");
		
		final String METHOD_NAME = "getSpot";
		final String NAMESPACE = "http://webservices.vslecture.vs.inf.ethz.ch/SunSPOTWebservice";
		final String SOAP_ACTION = "http://webservices.vslecture.vs.inf.ethz.ch/SunSPOTWebservice/getSpotRequest";
		final String URL = "http://webservices.vslecture.vs.inf.ethz.ch/SunSPOTWebservice/getSpotRequest";
		final int SOAP_VERSION = SoapEnvelope.VER11;
		
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("id", "Spot3");

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SOAP_VERSION);
        //envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

        //androidHttpTransport.call("http://tempuri.org/IService1/Hello",envelope);
        androidHttpTransport.call(SOAP_ACTION, envelope);
        Object result = (Object)envelope.getResponse();

        String resultData = result.toString();
        Log.d("SOAP",resultData);
	}

}
