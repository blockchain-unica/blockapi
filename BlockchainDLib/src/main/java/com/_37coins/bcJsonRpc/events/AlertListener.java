package com._37coins.bcJsonRpc.events;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import com._37coins.bcJsonRpc.BitcoindClientFactory;


public class AlertListener extends Observable implements Observer {

	final private Observable alertListener;
	public Thread listener = null;

	public AlertListener() throws IOException {
		if (BitcoindClientFactory.alertSocket!=null){
			alertListener = new BitcoinDListener(BitcoindClientFactory.alertSocket);
		}else{
			alertListener = new BitcoinDListener(4003);
		}
	}

	@Override
	public synchronized void addObserver(Observer o) {
		if (null == listener) {
			alertListener.addObserver(this);
			listener = new Thread((Runnable) alertListener, "alertListener");
			listener.start();
		}
		super.addObserver(o);
	}

	@Override
	public void update(Observable o, Object arg) {
		final String value = ((String) arg).trim();
		setChanged();
		notifyObservers(value);
	}
	
	public void stop(){
		listener.interrupt();
	}

}
