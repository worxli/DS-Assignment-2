package ch.ethz.inf.vs.android.lukasbi.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import android.content.Context;

public class ServerMainThread implements Runnable {
	
	private ServerSocket serverSocket;
	private final int SERVERPORT = 8081;
	private static Socket socket = null;
	private Context mContext;

	public ServerMainThread(Context appContext) {
		this.mContext = appContext;
	}

	@Override
	public void run() {
		
		//create a new server socket
		try {
			serverSocket = new ServerSocket(SERVERPORT);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		//while thread is not busy try to accept incoming connections
		while(!Thread.currentThread().isInterrupted()) {
			try {
				socket = serverSocket.accept();	
				
				//start a new thread to handle connection
				ServerHelperThread sth = new ServerHelperThread(socket, mContext);
				new Thread(sth).start();
				
				//empty socket
				socket = null;
			} catch (IOException e) {
                e.printStackTrace();
            }
		}	
	}
}
