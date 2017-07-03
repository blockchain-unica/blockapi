package com._37coins.bcJsonRpc.events;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitcoinDListener extends Observable implements Runnable {
	public static Logger log = LoggerFactory.getLogger(BitcoinDListener.class);
	private final ServerSocket server;
	private int port;

	public BitcoinDListener(int port) throws IOException {
		server = new ServerSocket(port);
		this.port = port;
	}
	
	public BitcoinDListener(ServerSocket server) throws IOException {
		this.server = server;
		this.port = server.getLocalPort();
	}

	@Override
	public void run() {
		log.info("Thread " + Thread.currentThread().getName()
				+ "started lintening on " + port);
		while (!Thread.currentThread().isInterrupted()) {
			Socket connection = null;
			try {
				connection = server.accept();
			} catch (IOException e1) {
				e1.printStackTrace();
				break;
			}
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			    String line;
			    if ((line = reader.readLine()) != null) {
				    setChanged();
					notifyObservers(line);
					connection.close();
			    }
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// sockets are closed when complete.
				try {
					if (connection != null)
						connection.close();
				} catch (IOException e) {
				}
			}
		}
		log.warn("Thread " + Thread.currentThread().getName() + " exited");
	}
	
	@Override
	protected void finalize() throws Throwable {
		server.close();
		log.info("Thread " + Thread.currentThread().getName() + " shutting down.");
		super.finalize();
	}
	
}
