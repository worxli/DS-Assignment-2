package ch.ethz.inf.vs.android.lukasbi.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;

public class ServerHelperThread implements Runnable, SensorEventListener   {
	
	private Socket msocket;
	private SensorManager sensorManager;
    private List<Sensor> deviceSensors;
    private MediaPlayer mp;
    private Context mContext;
    private Sensor mySensor;
    private float[] items;
    private boolean sensordata;

	public ServerHelperThread(Socket socket, Context appContext) {
		this.msocket = socket;
		this.mContext = appContext;
	}
	
	public List<Sensor> getSensors(){
		//return list of sensors
        return this.deviceSensors;
    }
 
	
	public void vibrate(int duration) {
		//vibrate phone for duration seconds
		Vibrator vibrator = (Vibrator) mContext.getSystemService(mContext.VIBRATOR_SERVICE);
		vibrator.vibrate(duration * 1000);
	}
	
	public void playSound () {
		//play the rining sound once
		mp = MediaPlayer.create(mContext, R.raw.bells);
		mp.setLooping(false);
		mp.setVolume(1.0f, 1.0f);
		mp.start();
	}

	@Override
	public void run() {
		
		//get the sensor manager
	    this.sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
	    
	    //get all sensors of the device
        this.deviceSensors = sensorManager.getSensorList(android.hardware.Sensor.TYPE_ALL);
		
		try {
			//get input stream of socket
			BufferedReader in = new BufferedReader(new InputStreamReader(msocket.getInputStream()));
		    
			//read first line
		    String req = in.readLine();

		    Log.d("debug: ","request: " + req);
			
		    //split request at spaces
			String[] spl = req.split("\\s+");
			
			String page;
			String var = "";
			String response = "";
			
			//spl[0] -> request type: GET or POST etc.
			//spl[1] -> request address: /.../...
			//spl[2] -> ?
			
			//check for / and split request address in page and var
			if(spl[1].indexOf("/",2)>0){
				page = spl[1].substring(1,spl[1].indexOf("/",2));
				var = spl[1].substring(spl[1].indexOf("/",2)+1);
			} else if(spl[1].length()>1) {
				page = spl[1].substring(1);
			} else {
				page = "";
			}
			
			//if request is GET
			if(spl[0].equals("GET")){	
				
				Log.d("page:" ,page);
				
				//switch over page
				if(page.equals("")){
					response += "<title>root directory</title>";
					response += "<h2><a href=\"sensors\">Sensors</a></h2>";
					response += "<h2><a href=\"actuators\">Actuators</a></h2>";
				}
				
				if(page.equals("sensors")){
					response += "<title>sensors</title>";
					
					//loop over sensor names
					for(Sensor s : getSensors()){
						response += "<h2><a href=\"sensor/"+s.hashCode()+"\">"+s.getName()+"</a></h2>";
					}
				}
					
				if(page.equals("sensor")){
					response += "<title>"+var+"</title>";
					
					//loop over sensor and check for hash code
					for(Sensor s : getSensors()){
						if (s.hashCode() == Integer.parseInt(var)){
	                        mySensor = s;
						} 
					}
					
					//if sensor was found
					if(mySensor!=null){
						
						response += "<h2><b>Sensor name:</b> "+mySensor.getName()+"</h2>";
						response += "<h2><b>Sensor type:</b> "+mySensor.getType()+"</h2>";
						response += "<h2><b>Sensor vendor:</b> "+mySensor.getVendor()+"</h2>";
						
						//register sensor listener
						this.sensorManager.registerListener(this, this.mySensor, SensorManager.SENSOR_DELAY_NORMAL);
						
						while(!sensordata){	/*wait for sensor data */ }
						
						//put raw sensor data
						response += "<h2>Raw data: </h2>";
						for(float s : items){
							if(s!=0.0f){
								response += "<h3>"+s+"</h3>";
							}
						}
						
						//unregister sensor listerner
						this.sensorManager.unregisterListener(this);
					} else {
						response += "<h3>bas sensor hash!</h3>";
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
				response += "<h2>bad request!</h2>";	
			}
		    
			//create output stream
		    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(msocket.getOutputStream()));
		    
		    //create response and send
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
	public void onAccuracyChanged(Sensor arg0, int arg1) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// Set new values
        this.items = event.values;
        //sensor data is valid
        this.sensordata = true;
	}
}
