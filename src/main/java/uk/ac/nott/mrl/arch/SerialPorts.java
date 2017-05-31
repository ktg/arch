package uk.ac.nott.mrl.arch;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class SerialPorts
{
	public interface Listener
	{
		void processMessage(String message);
	}

	private static final Logger logger = Logger.getLogger("");
	private final SerialPort serialPort;
	private StringBuilder message = new StringBuilder();

	private static final Map<String, SerialPorts> serialListeners = new HashMap<>();
	private final Listener listener;

	static void updateSerialPorts(Listener listener)
	{
		Set<String> ports = new HashSet<>(serialListeners.keySet());
		for (String portName : SerialPortList.getPortNames())
		{
			try
			{
				if (ports.contains(portName))
				{
					ports.remove(portName);
				}
				else
				{
					SerialPorts serialListener = new SerialPorts(portName, listener);
					serialListeners.put(portName, serialListener);
					serialListener.connect();
					logger.info("Listening on " + portName);
				}
			}
			catch (SerialPortException e)
			{
				logger.warning(e.getMessage());
			}
		}

		for(String portName: ports)
		{
			SerialPorts serialListener = serialListeners.get(portName);
			try
			{
				serialListener.close();
				serialListeners.remove(portName);
				logger.info("Stopped listening to " + portName);
			}
			catch (SerialPortException e)
			{
				e.printStackTrace();
			}

		}
	}

	private SerialPorts(String port, Listener listener)
	{
		serialPort = new SerialPort(port);
		this.listener = listener;
	}

	private void connect() throws SerialPortException
	{
		serialPort.openPort();//Open port
		serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, 1, 0);//Set params
		int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
		serialPort.setEventsMask(mask);
		serialPort.addEventListener(event ->
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
							listener.processMessage(toProcess);
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
		});
	}

	private void close() throws SerialPortException
	{
		serialPort.closePort();
	}
}
