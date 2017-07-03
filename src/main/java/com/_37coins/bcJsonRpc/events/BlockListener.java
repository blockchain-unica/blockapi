package com._37coins.bcJsonRpc.events;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import com._37coins.bcJsonRpc.BitcoindClientFactory;
import com._37coins.bcJsonRpc.BitcoindInterface;
import com._37coins.bcJsonRpc.pojo.Block;


public class BlockListener extends Observable implements Observer {

	final private Observable blockListener;
	final private BitcoindInterface client;
	public Thread listener = null;

	public BlockListener(final BitcoindInterface client) throws IOException {
		if (BitcoindClientFactory.blockSocket!=null){
			blockListener = new BitcoinDListener(BitcoindClientFactory.blockSocket);
		}else{
			blockListener = new BitcoinDListener(4001);
		}
		this.client = client;
	}

	@Override
	public synchronized void addObserver(Observer o) {
		if (null == listener) {
			blockListener.addObserver(this);
			listener = new Thread((Runnable) blockListener, "blockListener");
			listener.start();
		}
		super.addObserver(o);
	}

	@Override
	public void update(Observable o, Object arg) {
		final String value = ((String) arg).trim();
		(new Thread() {
			public void run() {
				Block block = client.getblock(value);
				setChanged();
				notifyObservers(block);
			}
		}).start();
	}
	
	public void stop(){
		listener.interrupt();
	}

}
