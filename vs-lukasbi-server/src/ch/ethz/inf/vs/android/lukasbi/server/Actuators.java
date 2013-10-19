package ch.ethz.inf.vs.android.lukasbi.server;

import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.app.Service;
import android.content.Intent;

public class Actuators extends Service {
	
	// Binder given to clients
    private final IBinder mBinder = new LocalBinder();
	
	private MediaPlayer mp;
	
	public void vibrate(int duration) {
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(duration * 10);
	}
	
	public void playSound () {
		this.mp = MediaPlayer.create(this, R.raw.bells);
		mp.setLooping(true);
		mp.setVolume(1.0f, 1.0f);
		mp.start();
	}
	
	public void stopSound () {
		mp.stop();
	}
	
	/**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        Actuators getService() {
            // Return this instance of LocalService so clients can call public methods
            return Actuators.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}
