package com._37coins.bcJsonRpc;

import com.googlecode.jsonrpc4j.Base64;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.*;

//TODO se non ci sono altre differenze refactor con BitcoinD

/**This one could be omitted because it's almost identical to Bitcoind
 * It's for testing without modifying BitcoindClientFactory
 */

public class LitecoindClientFactory {

	public static ServerSocket blockSocket;
	public static ServerSocket walletSocket;
	public static ServerSocket alertSocket;

	private static Logger LOG = LoggerFactory.getLogger(LitecoindClientFactory.class);
	private static String OS = System.getProperty("os.name").toLowerCase();

	private static String convertStream(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	private static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	private final JsonRpcHttpClient client;

	/**
 	 *
 	 * for the listener to work litecoin has to be started like this:
 	 *
 	 * ./litecoind -blocknotify="echo '%s' | nc 127.0.0.1 4001"
 	 * -walletnotify="echo '%s' | nc 127.0.0.1 4002"
 	 * -alertnotify="echo '%s' | nc 127.0.0.1 4003"
 	 * -daemon
 	 *
 	 * @param url
 	 * @param username
 	 * @param password
 	 */
	public LitecoindClientFactory(URL url, String username, String password) {
			String cred = Base64
					.encodeBytes((username + ":" + password).getBytes());
			Map<String, String> headers = new HashMap<>(1);
			headers.put("Authorization", "Basic " + cred);
			client = new JsonRpcHttpClient(url, headers);
		}

	public LitecoindClientFactory(String path, final List<String> cmd) throws IOException{
		if (isWindows()){
			throw new RuntimeException("OS not supported");
		}
		//prepare sockets
		blockSocket = new ServerSocket(0);
		walletSocket = new ServerSocket(0);
		alertSocket = new ServerSocket(0);
		//prepare user
		String[] uuid = UUID.randomUUID().toString().split("-");
		String user = uuid[0]+uuid[2];
		String pw = uuid[4]+uuid[3]+uuid[1];
				//prepare command
		List<String> l = new ArrayList<String>();
			for (String s: cmd){
					if (s.contains("notify")){
							LOG.warn(s + " ommited from cmd due to possible collision");
						}else{
							l.add(s);
						}
					}
				l.add("-rpcuser="+user);
				l.add("-rpcpassword="+pw);
				l.add("-checklevel=2");
				l.add("-blocknotify=\"echo '%s' | nc 127.0.0.1 "+blockSocket.getLocalPort()+"\"");
				l.add("-walletnotify=\"echo '%s' | nc 127.0.0.1 "+walletSocket.getLocalPort()+"\"");
				l.add("-alertnotify=\"echo '%s' | nc 127.0.0.1 "+alertSocket.getLocalPort()+"\"");
				l.add("-daemon");
				l.add("-server");
				l.add("-txindex");
				//execute command
				ProcessBuilder pb = new ProcessBuilder(l);
				pb.directory(new File(path));
				Process p = pb.start();
				try {
					p.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new RuntimeException(convertStream(p.getErrorStream()));
				}
				//start client
				String cred = Base64.encodeBytes((user + ":" + pw).getBytes());
				Map<String, String> headers = new HashMap<>(1);
				headers.put("Authorization", "Basic " + cred);
				client = new JsonRpcHttpClient(new URL("http://localhost:9332/"), headers);
				//wait for node startup
				boolean success =false;
				for (int i = 10; i > 0; i--){
					try{
						ProxyUtil.createClientProxy(BitcoindInterface.class.getClassLoader(),
								LitecoindInterface.class, client).getinfo();
						success = true;
					}catch(Exception e){
						try {
							LOG.info("server not yet available, waiting another "+i+" secords.");
							Thread.sleep(1000);
						} catch (InterruptedException e1) {}
					}
					if (success){
						break;
					}
				}
				if (!success){
					throw new IOException("could not connect to litecoind");
				}
			}

			public LitecoindInterface getClient() {
				return ProxyUtil.createClientProxy(
						LitecoindInterface.class.getClassLoader(),
						LitecoindInterface.class, client);
			}

		}

