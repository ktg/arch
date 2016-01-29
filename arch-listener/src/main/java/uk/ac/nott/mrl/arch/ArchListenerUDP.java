package uk.ac.nott.mrl.arch;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ArchListenerUDP implements Runnable
{
	public static void main(String[] args)
	{
		ArchListenerUDP archListener = new ArchListenerUDP(901);
		new Thread(archListener).start();
	}

	private static final Logger logger = Logger.getLogger("");

	private final int port;
	private transient boolean running = true;
	private final OkHttpClient httpClient;

	public ArchListenerUDP(int port)
	{
		this.port = port;
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		//builder.cache(new Cache(getCacheDir(), cacheSize));
		httpClient = builder.build();
	}

	@Override
	public synchronized void run()
	{
		boolean error = false;
		while (running)
		{
			try
			{
				if (error)
				{
					wait(1000);
				}

				final DatagramSocket serverSocket = new DatagramSocket(port);
				logger.info("Listening for input on port " + port);
				final byte[] receivedData = new byte[1024];
				while (running)
				{
					try
					{
						final DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
						serverSocket.receive(receivePacket);

						String string = new String(receivedData, 0, receivePacket.getLength());
						string = string.toLowerCase();
						String[] parts = string.split(" ");

						logger.info(string);
						if (parts.length >= 2)
						{
							final FormBody.Builder bodyBuilder = new FormBody.Builder()
									.add("state", parts[1]);
							if(parts[0].equals("stateleft"))
							{
								bodyBuilder.add("direction", "left");
							}
							else if(parts[0].equals("stateright"))
							{
								bodyBuilder.add("direction", "right");
							}

							if (parts.length >= 3)
							{
								bodyBuilder.add("height", parts[3]);
							}


							final Request.Builder builder = new Request.Builder()
									.post(bodyBuilder.build())
									.url("http://localhost/state");
							final Request request = builder.build();
							final Response response = httpClient.newCall(request).execute();
						}

					}
					catch (Throwable e)
					{
						logger.log(Level.SEVERE, e.getMessage(), e);
					}
				}
			}
			catch (Throwable e)
			{
				logger.log(Level.SEVERE, e.getMessage(), e);
				error = true;
			}
		}
	}
}
