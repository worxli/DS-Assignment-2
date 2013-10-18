package ch.ethz.inf.vs.android.lukasbi.rest;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
		Button b = (Button) v;
		b.setText("1");
	}
	
	public void apacheRequest (View v) {
		
	}
	
	public void jsonRetrieve (View v) {
		
	}

	public void jsonParse (View v) {
		
	}
}
