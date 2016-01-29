package uk.ac.nott.mrl.arch;

import java.util.logging.Level;
import java.util.logging.Logger;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ArchListener implements SerialPortEventListener
{
	private static final Logger logger = Logger.getLogger("");
	private final SerialPort serialPort;
	private final OkHttpClient httpClient;

	public ArchListener(String port)
	{
		serialPort = new SerialPort("COM1");
		try
		{
			serialPort.openPort();//Open port
			serialPort.setParams(9600, 8, 1, 0);//Set params
			int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
			serialPort.setEventsMask(mask);//Set mask
			serialPort.addEventListener(this);//Add SerialPortEventListener
		}
		catch (SerialPortException e)
		{
			logger.log(Level.WARNING, e.getMessage(), e);
		}

		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		//builder.cache(new Cache(getCacheDir(), cacheSize));
		httpClient = builder.build();
	}

	public static void main(String[] args)
	{
		ArchListener archListener = new ArchListener("COM1");
	}

	StringBuilder message = new StringBuilder();

	public void serialEvent(SerialPortEvent event)
	{
		if (event.isRXCHAR() && event.getEventValue() > 0)
		{
			try
			{
				byte buffer[] = serialPort.readBytes();
				for (byte b : buffer)
				{
					if ((b == '\r' || b == '\n') && message.length() > 0)
					{
						String toProcess = message.toString();
						processMessage(toProcess);
						message.setLength(0);
					}
					else
					{
						message.append((char) b);
					}
				}
			}
			catch (SerialPortException e)
			{
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}

	private void processMessage(String message)
	{
		String string = message.toLowerCase();
		if(string.startsWith("state"))
		{
			String[] parts = string.split(" ");

			logger.info(string);
			if (parts.length >= 2)
			{
				final FormBody.Builder bodyBuilder = new FormBody.Builder()
						.add("state", parts[1]);
				if (parts[0].equals("stateleft"))
				{
					bodyBuilder.add("direction", "left");
				}
				else if (parts[0].equals("stateright"))
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
				try
				{
					final Response response = httpClient.newCall(request).execute();
				}
				catch (Exception e)
				{
					logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
	}
}