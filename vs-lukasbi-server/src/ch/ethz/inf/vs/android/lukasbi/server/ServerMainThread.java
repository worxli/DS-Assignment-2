package ch.ethz.inf.vs.android.lukasbi.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;

public class ServerMainThread implements Runnable {
	
	private ServerSocket serverSocket;
	
	private final int SERVERPORT = 8081;
	static Socket socket = null;
	private Context mContext;
	private MediaPlayer appmp;

	public ServerMainThread(Context appContext, MediaPlayer mp) {
		this.mContext = appContext;
		this.appmp = mp;
	}

	@Override
	public void run() {
		
		try {
			serverSocket = new ServerSocket(SERVERPORT);
			Log.d("socket", "socket created");
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		while(!Thread.currentThread().isInterrupted()) {
			try {
				Log.d("socket", "waiting for incoming connection");
				socket = serverSocket.accept();			    
			    Log.d("accepted", "incming connection was accepted");
				ServerHelperThread sth = new ServerHelperThread(socket, mContext, appmp);
				new Thread(sth).start();
				Log.d("thread", "helperthread started");
			} catch (IOException e) {
                e.printStackTrace();
            }
		}
		
	}

}
