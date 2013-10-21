package ch.ethz.inf.vs.android.lukasbi.server;

import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;

public class Sensors extends Service {
	
	private SensorManager sensorManager;
    private List<Sensor> deviceSensors;
    private MediaPlayer mp;
    private Context mContext;
    
    @Override
	public void onCreate() {

    	this.sensorManager = (SensorManager) getSystemService(mContext.SENSOR_SERVICE);
        this.deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
	}
    
    public List<Sensor> getSensors(){
    	return deviceSensors;
    }

	
	// Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        Sensors getService() {
            // Return this instance of LocalService so clients can call public methods
            return Sensors.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }    
	
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


}
