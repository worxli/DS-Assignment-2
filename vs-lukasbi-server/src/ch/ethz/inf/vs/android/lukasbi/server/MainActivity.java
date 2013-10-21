package ch.ethz.inf.vs.android.lukasbi.server;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	
	private ServerMainThread sth;
	
	private Thread thread;
	private MediaPlayer mp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	/**
	 * Eventhandler for the togglebutton
	 */
	public void onRunClicked (View v) {
		
		if(((ToggleButton) v).isChecked()){
            sth = new ServerMainThread(this.getApplicationContext(),mp);
			thread = new Thread(sth);
			thread.start();
		} else {
			thread.interrupt();
			thread = null;
		}	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
