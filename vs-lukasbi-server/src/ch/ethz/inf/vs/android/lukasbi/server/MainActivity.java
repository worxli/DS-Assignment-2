package ch.ethz.inf.vs.android.lukasbi.server;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	
	private ServerMainThread sth;
	private Thread thread;
	
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
			//start a new thread with server logic
            sth = new ServerMainThread(this.getApplicationContext());
            thread = new Thread(sth);
            thread.start();

		} else {
			thread.interrupt();
		}	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
}
