package ch.ethz.inf.vs.android.lukasbi.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

import ch.ethz.inf.vs.android.lukasbi.server.Sensors.LocalBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;

public class ServerHelperThread implements Runnable, SensorEventListener   {
	
	private Socket msocket;
	private SensorManager sensorManager;
    private List<Sensor> deviceSensors;
    private MediaPlayer mp;
    private Context mContext;
    private Sensor mySensor;
    private SensorAdapter sensorAdapter;
    private float[] items;
    private boolean sensordata;

	public ServerHelperThread(Socket socket, Context appContext, MediaPlayer appmp) {
		this.msocket = socket;
		this.mContext = appContext;
		this.mp = appmp;
	}
	
	public List<Sensor> getSensors(){
        return this.deviceSensors;
    }
 
	
	public void vibrate(int duration) {
		Vibrator vibrator = (Vibrator) mContext.getSystemService(mContext.VIBRATOR_SERVICE);
		vibrator.vibrate(duration * 1000);
	}
	
	public void playSound () {
		this.mp = MediaPlayer.create(mContext, R.raw.bells);
		mp.setLooping(false);
		mp.setVolume(1.0f, 1.0f);
		mp.start();
	}

	@Override
	public void run() {
		
	    this.sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        this.deviceSensors = sensorManager.getSensorList(android.hardware.Sensor.TYPE_ALL);
		
		try {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(msocket.getInputStream()));
		    
		    String req = in.readLine();

		    Log.d("debug: ","request: " + req);
			
			String[] spl = req.split("\\s+");
			
			String page;
			String var = "";
			String response = "";
			
			if(spl[1].indexOf("/",2)>0){
				page = spl[1].substring(1,spl[1].indexOf("/",2));
				var = spl[1].substring(spl[1].indexOf("/",2)+1);
			} else if(spl[1].length()>1) {
				page = spl[1].substring(1);
			} else {
				page = "";
			}
			
			
			if(spl[0].equals("GET")){	
				
				Log.d("page:" ,page);
				
				if(page==""){
					response += "<title>root directory</title>";
					response += "<h2><a href=\"sensors\">Sensors</a></h2>";
					response += "<h2><a href=\"actuators\">Actuators</a></h2>";
				}
				
				if(page.equals("sensors")){
					response += "<title>sensors</title>";
					for(Sensor s : getSensors()){
						response += "<h2><a href=\"sensor/"+s.hashCode()+"\">"+s.getName()+"</a></h2>";
					}
				}
					
				if(page.equals("sensor")){
					response += "<title>"+var+"</title>";
					for(Sensor s : getSensors()){
						if (s.hashCode() == Integer.parseInt(var)){
	                        mySensor = s;
						} 
					}
					if(mySensor!=null){
						this.sensorManager.registerListener(this, this.mySensor, SensorManager.SENSOR_DELAY_NORMAL);
						while(!sensordata){
							//wait for sensor data
						}
						response += "<h3>"+this.items[0]+"</h3>";
						this.sensorManager.unregisterListener(this);
					}
					
				}
					
				if(page.equals("actuators")){
					response += "<title>actuators</title>";
					response += "<form action=\"play/\" method=\"GET\">";
					response += "<input type=\"submit\" value=\"Play sound!\"/>";
					response += "</form>";
					response += "<form method=\"GET\" action=\"vibrate/\">";
					response += "<input type=\"range\" min=\"1\" max=\"10\" name=\"slider\">";
					response += "<input type=\"submit\" value=\"Vibrate phone!\"/>";
					response += "</form>";
				}
				
				if(page.equals("play")){
					
					playSound();
					response += "<title>sound is playing</title>";
					response += "<form method=\"GET\" action=\"/play/\">";
					response += "<input type=\"submit\" value=\"Play sound again!\"/>";
					response += "</form>";
				}
					
					
				if(page.equals("vibrate")){
					
					var = spl[1].substring(spl[1].indexOf("=",1)+1);
					vibrate(Integer.parseInt(var));
					
					Log.d("debug: ",var);
					response += "<title>root directory</title>";
					response += "<h2><a href=\"/sensors\">Sensors</a></h2>";
					response += "<h2><a href=\"/actuators\">Actuators</a></h2>";	
				}
			} else {
				Log.d("debug: ","unknown request");
			}
		    
		    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(msocket.getOutputStream()));
		    
		    //Log.d("debug: ",response);
		    
		    out.write("HTTP/1.0 200 OK\r\n");
		    out.write("\r\n");
		    out.write(response);
		    out.flush();
		    out.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// Set new values
        this.items = event.values;
        this.sensordata = true;
	}
}
