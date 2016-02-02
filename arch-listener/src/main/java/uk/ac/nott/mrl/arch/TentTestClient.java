package uk.ac.nott.mrl.tent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TentTestClient implements Runnable
{
	private static final Logger logger = Logger.getLogger(TentTestClient.class.getSimpleName());
	private final int port;
	private double max = 1;
	private double min = -1;
	private double downDuration = 3;
	private double upDuration = 3;
	private double outFreq = 30;
	private double delta = 1 / 30;
	private double range = 1;
	private long sleep = 33;
	private boolean running = false;

	public TentTestClient(final int port)
	{
		this.port = port;
		updateDelta();
		updateRange();
	}

	public static void main(final String args[])
	{
		// TODO Get port
		int port = 2258;
		if (args.length > 0)
		{
			try
			{
				port = Integer.parseInt(args[0]);
			}
			catch (Exception e)
			{
				logger.warning("Failed to parse port " + args[0]);
			}
		}

		logger.info("Sending sine wave to " + port);
		final TentTestClient client = new TentTestClient(port);
		client.setDownDuration(3);
		client.setOutFreq(10);
		client.start();
	}

	@Override
	public void run()
	{
		double time = 0;
		try
		{
			final DatagramSocket socket = new DatagramSocket();
			final InetAddress IPAddress = InetAddress.getByName("localhost");

			while (running)
			{
				time += delta;

				final double duration = downDuration + upDuration;
				time %= duration;

				double y;
				if (time < downDuration)
				{
					y = Math.cos(time / (downDuration / Math.PI));
				}
				else
				{
					y = Math.cos(time / (upDuration / Math.PI) + Math.PI * (1 - (downDuration / upDuration)));
				}

				try
				{
					final int out = (int) ((((y + 1) * range) + min) * 10000);
					final byte[] data = ByteBuffer.allocate(4).putInt(out).array();
					DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, port);
					socket.send(sendPacket);
					logger.info("Output " + out);
				}
				catch (IOException e)
				{
					logger.log(Level.WARNING, e.getMessage(), e);
				}

				try
				{
					Thread.sleep(sleep);
				}
				catch (final InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public void setDownDuration(final double downDuration)
	{
		this.downDuration = downDuration;
		updateDelta();
	}

	public void setOutFreq(final double outFreq)
	{
		final double oldFreq = this.outFreq;
		if (oldFreq != outFreq)
		{
			this.outFreq = outFreq;
			sleep = (long) (1000 / outFreq);

			updateDelta();
		}
	}

	public void start()
	{
		running = true;
		new Thread(this).start();
	}

	public void stop()
	{
		running = false;
	}

	private void updateDelta()
	{
		delta = 1 / outFreq;
	}

	private void updateRange()
	{
		range = (max - min) / 2;
	}
}
