package ch.ethz.inf.vs.android.lukasbi.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import ch.ethz.inf.vs.android.lukasbi.server.Sensors.LocalBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class ServerHelperThread implements Runnable {
	
	private Socket msocket;
	Sensors mSensor;
	private Context mContext;


	public ServerHelperThread(Socket socket, Intent sensors) {
		this.msocket = socket;
		this.mContext.bindService(sensors, mSeConn, Context.BIND_AUTO_CREATE);
	}
	
	ServiceConnection mSeConn = new ServiceConnection() {
		  
		  public void onServiceDisconnected(ComponentName name) {}
		  
		  public void onServiceConnected(ComponentName name, IBinder service) {
		   LocalBinder mLocalBinder = (LocalBinder)service;
		   mSensor = mLocalBinder.getService();
		  }
	};

	@Override
	public void run() {
		
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
				
				/*switch (page) {
				case "":
					response += "<title>root directory</title>";
					response += "<h2><a href=\"sensors\">Sensors</a></h2>";
					response += "<h2><a href=\"actuators\">Actuators</a></h2>";
					break;
					
				case "sensors":
					response += "<title>sensors</title>";
					
					//return sensor list
					
					break;
					
				case "sensor":
					
					break;
					
				case "actuators":
					response += "<title>actuators</title>";
					response += "<form action=\"play\">";
					response += "<input type=\"submit\" value=\"Play sound!\"/>";
					response += "</form>";
					response += "<form method=\"GET\" action=\"vibrate/\">";
					response += "<input type=\"range\" min=\"1\" max=\"10\" name=\"slider\">";
					response += "<input type=\"submit\" value=\"Vibrate phone!\"/>";
					response += "</form>";
					
					break;
				
				case "play":
					//service play sound
					response += "<title>sound is playing</title>";
					response += "<form method=\"POST\" action=\"stop\">";
					response += "<input type=\"submit\" value=\"Stop sound!\"/>";
					response += "</form>";
					break;
					
				case "stop":
					//service stop sound
					response += "<title>root directory</title>";
					response += "<h2><a href=\"sensors\">Sensors</a></h2>";
					response += "<h2><a href=\"actuators\">Actuators</a></h2>";
					break;
					
				case "vibrate":
					//vibrate phone
					var = spl[1].substring(spl[1].indexOf("=",1)+1);
					Log.d("debug: ",var);
					response += "<title>root directory</title>";
					response += "<h2><a href=\"sensors\">Sensors</a></h2>";
					response += "<h2><a href=\"actuators\">Actuators</a></h2>";	
						

				default:
					break;
				}
				*/
					
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
}
