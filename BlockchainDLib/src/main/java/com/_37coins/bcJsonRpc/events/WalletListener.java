package com._37coins.bcJsonRpc.events;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import com._37coins.bcJsonRpc.BitcoindClientFactory;
import com._37coins.bcJsonRpc.BitcoindInterface;
import com._37coins.bcJsonRpc.pojo.Transaction;


public class WalletListener extends Observable implements Observer {

	final private Observable walletListener;
	final private BitcoindInterface client;
	public Thread listener = null;

	public WalletListener(final BitcoindInterface client) throws IOException {
		if (BitcoindClientFactory.walletSocket!=null){
			walletListener = new BitcoinDListener(BitcoindClientFactory.walletSocket);
		}else{
			walletListener = new BitcoinDListener(4002);
		}
		this.client = client;
	}

	@Override
	public synchronized void addObserver(Observer o) {
		if (null == listener) {
			walletListener.addObserver(this);
			listener = new Thread((Runnable) walletListener, "walletListener");
			listener.start();
		}
		super.addObserver(o);
	}

	@Override
	public void update(Observable o, Object arg) {
		final String value = ((String) arg).trim();
		(new Thread() {
			public void run() {
				Transaction tx = client.gettransaction(value);
				setChanged();
				notifyObservers(tx);
			}
		}).start();
	}
	
	public void stop(){
		listener.interrupt();
	}

}
