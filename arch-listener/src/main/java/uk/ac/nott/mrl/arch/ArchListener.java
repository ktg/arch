/*
 *     Light Night Arch Control software
 *     Copyright (C) 2016 University of Nottingham
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.nott.mrl.arch;

import java.util.logging.Level;
import java.util.logging.Logger;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ArchListener implements SerialPortEventListener
{
	private static final Logger logger = Logger.getLogger("");
	private final SerialPort serialPort;
	private final OkHttpClient httpClient;
	StringBuilder message = new StringBuilder();

	public ArchListener(String port)
	{
		serialPort = new SerialPort(port);
		try
		{
			serialPort.openPort();//Open port
			serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, 1, 0);//Set params
			int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
			serialPort.setEventsMask(mask);//Set mask
			serialPort.addEventListener(this);//Add SerialPortEventListener
			logger.info("Listening on " + port);
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
		for (String portName : SerialPortList.getPortNames())
		{
			ArchListener archListener = new ArchListener(portName);
		}
	}

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
		String string = message.trim().toLowerCase();
		if (string.startsWith("state"))
		{
			String[] parts = string.split(" ");

			logger.fine(string);
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
					bodyBuilder.add("height", parts[2]);
				}

				final Request.Builder builder = new Request.Builder()
						.post(bodyBuilder.build())
						.url("http://127.0.0.1:8080/arch/state");
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